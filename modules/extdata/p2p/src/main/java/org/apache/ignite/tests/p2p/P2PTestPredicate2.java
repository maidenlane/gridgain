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
package org.apache.ignite.tests.p2p;

import java.io.Serializable;
import org.apache.ignite.internal.processors.cache.GridCacheDeployable;
import org.apache.ignite.internal.processors.cache.GridCacheIdMessage;
import org.apache.ignite.lang.IgniteBiPredicate;

public class P2PTestPredicate2 extends GridCacheIdMessage implements GridCacheDeployable, IgniteBiPredicate, Serializable {
    @Override public boolean addDeploymentInfo() {
        return true;
    }

    @Override public boolean apply(Object o, Object o2) {
        return false;
    }

    @Override public short directType() {
        return 0;
    }
}
