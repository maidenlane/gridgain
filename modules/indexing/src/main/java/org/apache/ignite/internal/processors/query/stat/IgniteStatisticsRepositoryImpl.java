/*
 * Copyright 2020 GridGain Systems, Inc. and Contributors.
 *
 * Licensed under the GridGain Community Edition License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gridgain.com/products/software/community-edition/gridgain-community-edition-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ignite.internal.processors.query;

import org.apache.ignite.IgniteLogger;
import org.apache.ignite.internal.GridKernalContext;
import org.apache.ignite.internal.processors.cache.GridCacheUtils;
import org.apache.ignite.internal.processors.cache.query.QueryTable;
import org.apache.ignite.internal.processors.query.stat.ColumnStatistics;
import org.apache.ignite.internal.processors.query.stat.IgniteStatisticsManagerImpl;
import org.apache.ignite.internal.processors.query.stat.IgniteStatisticsRepository;
import org.apache.ignite.internal.processors.query.stat.IgniteStatisticsStore;
import org.apache.ignite.internal.processors.query.stat.IgniteStatisticsStoreImpl;
import org.apache.ignite.internal.processors.query.stat.ObjectPartitionStatistics;
import org.apache.ignite.internal.processors.query.stat.ObjectStatistics;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class IgniteStatisticsRepositoryImpl implements IgniteStatisticsRepository {
    /** Logger. */
    private IgniteLogger log;

    /** */
    private final IgniteStatisticsStore store;

    /** */
    private final GridKernalContext ctx;

    /** Table->Partition->Partition Statistics map, populated only on server nodes without persistence enabled.  */
    private final Map<QueryTable, Map<Integer, ObjectPartitionStatistics>> partsStats;

    /** Local (for current node) object statistics. */
    private final Map<QueryTable, ObjectStatistics> localStats;

    /** Global (for whole cluster) object statistics. */
    private final Map<QueryTable, ObjectStatistics> globalStats = new ConcurrentHashMap<>();


    public IgniteStatisticsRepositoryImpl(GridKernalContext ctx) {
        this.ctx = ctx;

        if (ctx.config().isClientMode() || ctx.isDaemon()) {
            // Cache only global statistics, no store
            store = null;
            partsStats = null;
            localStats = null;
        } else {
            if (GridCacheUtils.isPersistenceEnabled(ctx.config())) {
                store = new IgniteStatisticsStoreImpl(ctx, this);
                partsStats = null;
            } else {
                // Cache partitions statistics, no store
                store = null;
                partsStats = new ConcurrentHashMap<>();
            }
            localStats = new ConcurrentHashMap<>();
        }
        log = ctx.log(IgniteStatisticsRepositoryImpl.class);
    }

    @Override public void saveLocalPartitionsStatistics(QueryTable tbl, Collection<ObjectPartitionStatistics> statistics,
                                                        boolean fullStat) {
        if (partsStats != null) {
            Map<Integer, ObjectPartitionStatistics> statisticsMap = new ConcurrentHashMap<>();
            for (ObjectPartitionStatistics s : statistics) {
                if (statisticsMap.put(s.partId(), s) != null) {
                    log.warning(String.format("Trying to save more than one %s.%s partition statistics for partition %d",
                            tbl.schema(), tbl.table(), s.partId()));
                }
            }

            if (fullStat)
                partsStats.compute(tbl, (k, v) -> {
                    if (v == null)
                        v = statisticsMap;
                    else
                        v.putAll(statisticsMap);

                    return v;
                });
            else
                partsStats.compute(tbl, (k, v) -> {
                    if (v != null)
                        for (Map.Entry<Integer, ObjectPartitionStatistics> partStat : v.entrySet()) {
                            ObjectPartitionStatistics newStat = statisticsMap.get(partStat.getKey());
                            if (newStat != null) {
                                ObjectPartitionStatistics combinedStat = add(partStat.getValue(), newStat);
                                statisticsMap.put(partStat.getKey(), combinedStat);
                            }
                        }
                    return statisticsMap;
                });
        }
        if (store != null) {
            Map<Integer, ObjectPartitionStatistics> oldStatistics = store.getLocalPartitionsStatistics(tbl).stream()
                    .collect(Collectors.toMap(s -> s.partId(), s -> s));
            Collection<ObjectPartitionStatistics> combinedStats = new ArrayList<>(statistics.size());
            for (ObjectPartitionStatistics newPartStat : statistics) {
                ObjectPartitionStatistics oldPartStat = oldStatistics.get(newPartStat.partId());
                if (oldPartStat == null)
                    combinedStats.add(newPartStat);
                else {
                    ObjectPartitionStatistics combinedPartStats = add(oldPartStat, newPartStat);
                    combinedStats.add(combinedPartStats);
                }
            }
            store.saveLocalPartitionsStatistics(tbl, combinedStats);
        }
    }

    public Collection<ObjectPartitionStatistics> getLocalPartitionsStatistics(QueryTable tbl){
        if (partsStats != null) {
            Map<Integer, ObjectPartitionStatistics> objectStatisticsMap = partsStats.get(tbl);

            return objectStatisticsMap == null ? null : objectStatisticsMap.values();
        }
        if (store != null) {
            return store.getLocalPartitionsStatistics(tbl);
        }

        return Collections.emptyList();
    }

    @Override public void clearLocalPartitionsStatistics(QueryTable tbl, String... colNames) {
        if (colNames == null || colNames.length == 0) {
            if (partsStats != null) {
                partsStats.remove(tbl);
            }
            if (store != null)
                store.clearLocalPartitionsStatistics(tbl);
        } else {
            Map<Integer, ObjectPartitionStatistics> newPartStat = new HashMap<>();
            List<Integer> partsToRemove = new ArrayList<>();
            partsStats.computeIfPresent(tbl, (k,v) -> {
                for (Map.Entry<Integer,ObjectPartitionStatistics> partEntry: v.entrySet()) {
                    ObjectPartitionStatistics partStats = substract(partEntry.getValue(), colNames);
                    if (partStats.columnsStatistics().isEmpty())
                        partsToRemove.add(partEntry.getKey());
                    else
                        newPartStat.put(partEntry.getKey(), partStats);
                }
                return newPartStat.isEmpty() ? null : newPartStat;
            });
            if (store != null) {
                if (newPartStat.isEmpty())
                    store.clearLocalPartitionsStatistics(tbl);
                else
                    for (Integer remPartId : partsToRemove)
                        store.clearLocalPartitionStatistics(tbl, remPartId);
            }
        }
    }

    @Override public void saveLocalPartitionStatistics(QueryTable tbl, ObjectPartitionStatistics statistics) {
        if (partsStats != null) {
            partsStats.compute(tbl, (k,v) -> {
                if(v == null)
                    v = new ConcurrentHashMap<>();
                ObjectPartitionStatistics oldPartStat = v.get(statistics.partId());
                if (oldPartStat == null)
                    v.put(statistics.partId(), statistics);
                else {
                    ObjectPartitionStatistics combinedStats = add(oldPartStat, statistics);
                    v.put(statistics.partId(), combinedStats);
                }
                return v;
            });
        }
        if (store != null) {
            ObjectPartitionStatistics oldPartStat = store.getLocalPartitionStatistics(tbl, statistics.partId());
            if (oldPartStat == null)
                store.saveLocalPartitionStatistics(tbl, statistics);
            else {
                ObjectPartitionStatistics combinedStats = add(oldPartStat, statistics);
                store.saveLocalPartitionStatistics(tbl, combinedStats);
            }
        }
    }

    @Override public ObjectPartitionStatistics getLocalPartitionStatistics(QueryTable tbl, int partId) {
        if (partsStats != null) {
            Map<Integer, ObjectPartitionStatistics> objectPartStats = partsStats.get(tbl);
            return objectPartStats == null ? null : objectPartStats.get(partId);
        }
        if (store != null)
            return store.getLocalPartitionStatistics(tbl, partId);
        return null;
    }

    @Override public void clearLocalPartitionStatistics(QueryTable tbl, int partId) {
        if (partsStats != null) {
            partsStats.computeIfPresent(tbl, (k,v) -> {
                v.remove(partId);
                return v.isEmpty() ? null : v;
            });
        }
        if (store != null)
            store.clearLocalPartitionStatistics(tbl, partId);
    }

    @Override public void saveLocalStatistics(QueryTable tbl, ObjectStatistics statistics) {
        if (localStats != null)
            localStats.put(tbl, statistics);
    }

    @Override public void cacheLocalStatistics(QueryTable tbl, Collection<ObjectPartitionStatistics> statistics) {
        IgniteStatisticsManagerImpl statManager = (IgniteStatisticsManagerImpl)ctx.query().getIndexing().statsManager();
        if (localStats != null)
            localStats.put(tbl, statManager.aggregateLocalStatistics(tbl, statistics));
    }

    @Override public ObjectStatistics getLocalStatistics(QueryTable tbl) {
        return localStats == null ? null : localStats.get(tbl);
    }

    @Override public void clearLocalStatistics(QueryTable tbl, String... colNames) {
        if (localStats == null)
            return;

        if (colNames == null || colNames.length == 0)
            localStats.remove(tbl);
        else
            localStats.computeIfPresent(tbl, (k,v) -> substract(v, colNames));
    }

    @Override
    public void saveGlobalStatistics(QueryTable tbl, ObjectStatistics statistics) {
        globalStats.put(tbl, statistics);
    }

    public ObjectStatistics getGlobalStatistics(QueryTable tbl) {
        return globalStats.get(tbl);
    }

    @Override
    public void clearGlobalStatistics(QueryTable tbl, String... colNames) {
        if (colNames == null || colNames.length == 0)
            globalStats.remove(tbl);
        else
            globalStats.computeIfPresent(tbl, (k, v) -> substract(v, colNames));
    }

    /**
     * Add new statistics into base one (with overlapping of existing columns)
     * @param base
     * @param add
     * @param <T>
     * @return
     */
    private <T extends ObjectStatistics> T add(T base, T add) {
        T result = (T)add.clone();
        for (Map.Entry<String, ColumnStatistics> entry : add.columnsStatistics().entrySet()) {
            result.columnsStatistics().putIfAbsent(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Remove specified columns from clone of base ObjectStatistics object.
     *
     * @param base ObjectStatistics to remove columns from.
     * @param columns columns to remove.
     * @return cloned object without specified columns statistics.
     */
    private <T extends ObjectStatistics> T substract(T base, String[] columns) {
        T result = (T)base.clone();
        for (String col : columns) {
            result.columnsStatistics().remove(col);
        }
        return result;
    }
}
