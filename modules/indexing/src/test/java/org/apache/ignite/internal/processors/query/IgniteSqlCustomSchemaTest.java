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

package org.apache.ignite.internal.processors.query;

import java.util.List;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.testframework.GridTestUtils;
import org.junit.Test;

/** Verifies custom sql schema through SqlFieldsQuery API. */
public class IgniteSqlCustomSchemaTest extends AbstractCustomSchemaTest {
    /** {@inheritDoc} */
    @Override protected List<List<?>> execSql(String qry) {
        return grid(0).context().query()
            .querySqlFields(new SqlFieldsQuery(qry).setLazy(true), false)
            .getAll();
    }

    /** {@inheritDoc} */
    @Test
    @SuppressWarnings("ThrowableNotThrown")
    @Override public void testCreateTblsInDiffSchemasForSameCache() {
        final String testCache = "cache1";

        execSql("CREATE TABLE " + t(SCHEMA_NAME_1, TBL_NAME)
            + " (s1_key INT PRIMARY KEY, s1_val INT) WITH \"cache_name=" + testCache + "\"");

        GridTestUtils.assertThrowsWithCause(
            () -> execSql("CREATE TABLE " + t(SCHEMA_NAME_2, TBL_NAME)
                + " (s1_key INT PRIMARY KEY, s2_val INT) WITH \"cache_name=" + testCache + "\""),
            IgniteSQLException.class
        );

        execSql("DROP TABLE " + t(SCHEMA_NAME_1, TBL_NAME));
    }
}
