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

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.QueryEntity;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.cache.query.annotations.QuerySqlField;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.junit.Test;

/** */
public class SqlPartOfComplexPkLookupTest extends GridCommonAbstractTest {
    /** {@inheritDoc} */
    @Override protected void afterTest() throws Exception {
        stopAllGrids();
    }

    /** */
    @Test
    public void testPartOfComplexPkLookupDdl() throws Exception {
        IgniteEx ign = startGrid(0);

        IgniteCache<Object, Object> cache = ign.getOrCreateCache(new CacheConfiguration<>(DEFAULT_CACHE_NAME));

        cache.query(new SqlFieldsQuery("" +
            "CREATE TABLE Person(\n" +
            "  id int,\n" +
            "  city_id int,\n" +
            "  name varchar,\n" +
            "  PRIMARY KEY (id, city_id)\n" +
            ");"));

        cache.query(new SqlFieldsQuery("INSERT INTO Person (id, city_id, name) VALUES (1, 3, 'John Doe');"));
        cache.query(new SqlFieldsQuery("INSERT INTO Person (id, city_id, name) VALUES (1, 4, 'John Dean');"));

        checkPartialPkLookup(cache);
    }

    /** */
    @Test
    public void testPartOfComplexPkLookupQueryEntity() throws Exception {
        IgniteEx ign = startGrid(0);

        IgniteCache<Object, Object> cache = ign.getOrCreateCache(new CacheConfiguration<>(DEFAULT_CACHE_NAME)
            .setQueryEntities(Collections.singleton(new QueryEntity(TestPk.class, String.class)
                .setTableName("Person")
                .addQueryField("name", String.class.getName(), "name")
                .setValueFieldName("name"))));

        cache.put(new TestPk(1, 3), "John Doe");
        cache.put(new TestPk(1, 4), "John Dean");

        checkPartialPkLookup(cache);
    }

    @Test
    public void test() throws Exception {
        IgniteEx ign = startGrid(0);

        IgniteCache<Object, Object> cache = ign.getOrCreateCache(DEFAULT_CACHE_NAME);

        cache.query(new SqlFieldsQuery("create table Person (id Int, city_id Int, name VarChar, primary key (id, city_id)) WITH \"cache_name=Person,key_type=" + TestPk.class.getName() + "\"")).getAll();
        cache.query(new SqlFieldsQuery("insert into Person (id, city_id, name)  values(1, 2, 'John Doe')")).getAll();

        assertEquals("John Doe", ign.cache("Person").get(new TestPk(1, 3)));
    }

    /** */
    private void checkPartialPkLookup(IgniteCache<Object, Object> cache) {
        assertTrue(cache.query(new SqlFieldsQuery("SELECT name FROM Person WHERE id = 1 and city_id is null")).getAll()
            .isEmpty());

        assertTrue(cache.query(new SqlFieldsQuery("SELECT name FROM Person WHERE id = 1 and city_id = null")).getAll()
            .isEmpty());

        List<List<?>> rows = cache.query(new SqlFieldsQuery("SELECT name FROM Person WHERE id = 1")).getAll();

        assertEquals(2, rows.size());
        assertEquals(
            ImmutableSet.of("John Doe", "John Dean"),
            rows.stream().map(row -> (String)row.get(0)).collect(Collectors.toSet()));

        List<List<?>> rows2 = cache.query(new SqlFieldsQuery("SELECT name FROM Person WHERE id = 1 AND city_id = 3"))
            .getAll();

        assertEquals(1, rows2.size());
        assertEquals("John Doe", rows2.get(0).get(0));
    }

    /** */
    public static class TestPk {
        /** */
        @QuerySqlField(name = "id")
        private final int id;

        /** */
        @QuerySqlField(name = "city_id")
        private final int cityId;

        /** */
        public TestPk(int id, int cityId) {
            this.id = id;
            this.cityId = cityId;
        }
    }
}
