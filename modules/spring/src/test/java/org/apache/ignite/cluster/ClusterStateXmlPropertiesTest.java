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

package org.apache.ignite.cluster;

import java.lang.reflect.Field;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.IgnitionEx;
import org.apache.ignite.internal.util.typedef.internal.A;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.junit.Test;

/**
 * Checks that internal logic of {@link IgniteConfiguration#isActiveOnStart()} and
 * {@link IgniteConfiguration#isAutoActivationEnabled()} works correctly with xml configuration.
 */
public class ClusterStateXmlPropertiesTest extends GridCommonAbstractTest {
    /**
     * Checks that internal flags will be setted in case of xml configuration.
     *
     * @throws Exception If failed.
     */
    @Test
    public void testXmlConfiguration() throws Exception {
        IgniteConfiguration defaultCfg = new IgniteConfiguration();

        assertFalse(getBooleanFieldFromConfig(defaultCfg, "activeOnStartPropSetFlag"));
        assertFalse(getBooleanFieldFromConfig(defaultCfg, "autoActivationPropSetFlag"));
        assertTrue(defaultCfg.isActiveOnStart());
        assertTrue(defaultCfg.isAutoActivationEnabled());

        IgniteConfiguration cfg = IgnitionEx.loadConfiguration(U.resolveIgniteUrl("modules/spring/src/test/config/state/cluster-state.xml")).get1();

        assertTrue(getBooleanFieldFromConfig(cfg, "activeOnStartPropSetFlag"));
        assertTrue(getBooleanFieldFromConfig(cfg, "autoActivationPropSetFlag"));
        assertFalse(cfg.isActiveOnStart());
        assertFalse(cfg.isAutoActivationEnabled());
    }

    /**
     * Gets from given config {@code cfg} field with name {@code fieldName} and type boolean.
     *
     * @param cfg Config.
     * @param fieldName Name of field.
     * @return Value of field.
     */
    private boolean getBooleanFieldFromConfig(IgniteConfiguration cfg, String fieldName) throws IllegalAccessException {
        A.notNull(cfg, "cfg");
        A.notNull(fieldName, "fieldName");

        Field field = U.findField(IgniteConfiguration.class, fieldName);
        field.setAccessible(true);

        return field.getBoolean(cfg);
    }
}
