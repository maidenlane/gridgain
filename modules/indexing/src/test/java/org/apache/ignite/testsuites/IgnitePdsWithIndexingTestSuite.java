/*
 * Copyright 2019 GridGain Systems, Inc. and Contributors.
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

package org.apache.ignite.testsuites;

import org.apache.ignite.internal.processors.cache.IgnitePdsSingleNodeWithIndexingAndGroupPutGetPersistenceSelfTest;
import org.apache.ignite.internal.processors.cache.IgnitePdsSingleNodeWithIndexingPutGetPersistenceTest;
import org.apache.ignite.internal.processors.cache.persistence.db.IgniteTcBotInitNewPageTest;
import org.apache.ignite.internal.processors.cache.persistence.db.IndexingMultithreadedLoadContinuousRestartTest;
import org.apache.ignite.internal.processors.cache.persistence.db.LongDestroyDurableBackgroundTaskTest;
import org.apache.ignite.internal.processors.database.IgniteDbMultiNodeWithIndexingPutGetTest;
import org.apache.ignite.internal.processors.database.IgniteDbSingleNodeWithIndexingPutGetTest;
import org.apache.ignite.internal.processors.database.IgniteDbSingleNodeWithIndexingWalRestoreTest;
import org.apache.ignite.internal.processors.database.IgnitePersistentStoreQueryWithMultipleClassesPerCacheTest;
import org.apache.ignite.internal.processors.database.IgnitePersistentStoreSchemaLoadTest;
import org.apache.ignite.internal.processors.database.IgniteTwoRegionsRebuildIndexTest;
import org.apache.ignite.internal.processors.database.RebuildIndexTest;
import org.apache.ignite.internal.processors.database.RebuildIndexWithHistoricalRebalanceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/** */
@RunWith(Suite.class)
@Suite.SuiteClasses({})
public class IgnitePdsWithIndexingTestSuite {
}
