package org.apache.ignite.internal.processors.cache.distributed;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.IgniteTransactions;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMetrics;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.cache.affinity.rendezvous.RendezvousAffinityFunction;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.NearCacheConfiguration;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.internal.IgniteInternalFuture;
import org.apache.ignite.internal.TestRecordingCommunicationSpi;
import org.apache.ignite.internal.processors.cache.distributed.near.GridNearTxFinishResponse;
import org.apache.ignite.internal.processors.cache.transactions.IgniteTxManager;
import org.apache.ignite.internal.util.future.GridCompoundFuture;
import org.apache.ignite.internal.util.lang.GridAbsPredicate;
import org.apache.ignite.internal.util.typedef.G;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.plugin.extensions.communication.Message;
import org.apache.ignite.testframework.GridTestUtils;
import org.apache.ignite.testframework.MvccFeatureChecker;
import org.apache.ignite.testframework.junits.WithSystemProperty;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.apache.ignite.transactions.Transaction;
import org.apache.ignite.transactions.TransactionConcurrency;
import org.apache.ignite.transactions.TransactionIsolation;
import org.junit.Test;
import java.util.concurrent.CountDownLatch;

import static org.apache.ignite.IgniteSystemProperties.IGNITE_DUMP_TX_COLLISIONS_INTERVAL;
import static org.apache.ignite.transactions.TransactionConcurrency.OPTIMISTIC;
import static org.apache.ignite.transactions.TransactionConcurrency.PESSIMISTIC;
import static org.apache.ignite.transactions.TransactionIsolation.READ_COMMITTED;
import static org.apache.ignite.transactions.TransactionIsolation.REPEATABLE_READ;

/** Tests tx key contention detection functional. */
public class TxWaitTest extends GridCommonAbstractTest {
    /** Client flag. */
    private boolean client;

    /** Near cache flag. */
    private boolean nearCache;

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String name) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(name);

        cfg.setConsistentId("NODE_" + name.substring(name.length() - 1));

        if (client)
            cfg.setClientMode(true);

        cfg.setDataStorageConfiguration(
                new DataStorageConfiguration()
                        .setDefaultDataRegionConfiguration(
                                new DataRegionConfiguration()
                                        .setMaxSize(20 * 1024 * 1024)
                        )
        );

        TestRecordingCommunicationSpi commSpi = new TestRecordingCommunicationSpi();

        cfg.setCommunicationSpi(commSpi);

        cfg.setCacheConfiguration(getCacheConfiguration(DEFAULT_CACHE_NAME));

        if (client) {
            cfg.setConsistentId("Client");

            cfg.setClientMode(client);
        }

        return cfg;
    }

    /** */
    protected CacheConfiguration<?, ?> getCacheConfiguration(String name) {
        CacheConfiguration<Object, Object> ccfg = new CacheConfiguration<>(name)
                .setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL)
                .setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC)
                .setAffinity(new RendezvousAffinityFunction(false, 16))
                .setBackups(2)
                .setStatisticsEnabled(true);

        if (nearCache)
            ccfg.setNearConfiguration(new NearCacheConfiguration<>());

        return ccfg;
    }

    /** {@inheritDoc} */
    @Override protected void beforeTest() throws Exception {
        super.beforeTest();

        stopAllGrids();
    }

    /** {@inheritDoc} */
    @Override protected void afterTest() throws Exception {
        super.afterTest();

        stopAllGrids();
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    @WithSystemProperty(key = IGNITE_DUMP_TX_COLLISIONS_INTERVAL, value = "30000")
    public void testOptimisticReadCommittedCheckContentionTxMetricNear() throws Exception {
        nearCache = true;

        runKeyCollisionsMetric(OPTIMISTIC, READ_COMMITTED);
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    @WithSystemProperty(key = IGNITE_DUMP_TX_COLLISIONS_INTERVAL, value = "30000")
    public void testOptimisticRepeatableReadCheckContentionTxMetric() throws Exception {
        runKeyCollisionsMetric(OPTIMISTIC, REPEATABLE_READ);
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    @WithSystemProperty(key = IGNITE_DUMP_TX_COLLISIONS_INTERVAL, value = "30000")
    public void testOptimisticRepeatableReadCheckContentionTxMetricNear() throws Exception {
        nearCache = true;

        runKeyCollisionsMetric(OPTIMISTIC, REPEATABLE_READ);
    }

    /** Tests metric correct results while tx collisions occured.
     *
     * @param concurrency Concurrency level.
     * @param isolation Isolation level.
     * @throws Exception If failed.
     */
    private void runKeyCollisionsMetric(TransactionConcurrency concurrency, TransactionIsolation isolation) throws Exception {
        if (MvccFeatureChecker.forcedMvcc())
            return; // Not supported.

        Ignite ig = startGridsMultiThreaded(3);

        int contCnt = (int)U.staticField(IgniteTxManager.class, "COLLISIONS_QUEUE_THRESHOLD") * 5;

        CountDownLatch txLatch = new CountDownLatch(contCnt);

        ig.cluster().active(true);

        client = true;

        Ignite cl = startGrid();

        IgniteTransactions txMgr = cl.transactions();

        IgniteCache<Integer, Integer> cache = ig.cache(DEFAULT_CACHE_NAME);

        IgniteCache<Integer, Integer> cache0 = cl.cache(DEFAULT_CACHE_NAME);

        final Integer keyId = primaryKey(cache);

        CountDownLatch blockOnce = new CountDownLatch(1);

        for (Ignite ig0 : G.allGrids()) {
            if (ig0.configuration().isClientMode())
                continue;

            TestRecordingCommunicationSpi commSpi0 =
                    (TestRecordingCommunicationSpi)ig0.configuration().getCommunicationSpi();

            commSpi0.blockMessages(new IgniteBiPredicate<ClusterNode, Message>() {
                @Override public boolean apply(ClusterNode node, Message msg) {
                    if (msg instanceof GridNearTxFinishResponse && blockOnce.getCount() > 0) {
                        blockOnce.countDown();

                        return true;
                    }

                    return false;
                }
            });
        }

        IgniteInternalFuture f = GridTestUtils.runAsync(() -> {
            try (Transaction tx = txMgr.txStart(concurrency, isolation)) {
                cache0.put(keyId, 0);
                tx.commit();
            }
        });

        blockOnce.await();

        GridCompoundFuture<?, ?> finishFut = new GridCompoundFuture<>();

        for (int i = 0; i < contCnt; ++i) {
            IgniteInternalFuture f0 = GridTestUtils.runAsync(() -> {
                try (Transaction tx = txMgr.txStart(concurrency, isolation)) {
                    cache0.put(keyId, 0);

                    tx.commit();

                    txLatch.countDown();
                }
            });

            finishFut.add(f0);
        }



        grid(0).close();


        finishFut.markInitialized();


        awaitPartitionMapExchange();

        f.get();

        //grid(0).close();

        //awaitPartitionMapExchange();

        finishFut.get();

        txLatch.await();
    }
}
