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

package org.apache.ignite.internal.client.thin;

import static org.apache.ignite.internal.client.thin.ProtocolVersion.V1_1_0;
import static org.apache.ignite.internal.client.thin.ProtocolVersion.V1_2_0;
import static org.apache.ignite.internal.client.thin.ProtocolVersion.V1_4_0;
import static org.apache.ignite.internal.client.thin.ProtocolVersion.V1_5_0;
import static org.apache.ignite.internal.client.thin.ProtocolVersion.V1_6_0;
import static org.apache.ignite.internal.client.thin.ProtocolVersion.V1_7_0;
import static org.apache.ignite.internal.client.thin.ProtocolVersion.V1_7_1;

/**
 * Thin client feature that was introduced by introducing new protocol version.
 * Legacy approach. No new features of this kind should be added without strong justification. Use
 * {@link ProtocolBitmaskFeature} for all newly introduced features.
 */
public class ProtocolVersionFeature {
    /** Authorization feature. */
    public static final ProtocolVersionFeature AUTHORIZATION = new ProtocolVersionFeature(V1_1_0);

    /** Query entity precision and scale feature. */
    public static final ProtocolVersionFeature QUERY_ENTITY_PRECISION_AND_SCALE = new ProtocolVersionFeature(V1_2_0);

    /** Partition awareness feature. */
    public static final ProtocolVersionFeature PARTITION_AWARENESS = new ProtocolVersionFeature(V1_4_0);

    /** Transactions feature. */
    public static final ProtocolVersionFeature TRANSACTIONS = new ProtocolVersionFeature(V1_5_0);

    /** Expiry policy feature. */
    public static final ProtocolVersionFeature EXPIRY_POLICY = new ProtocolVersionFeature(V1_6_0);

    /** Bitmap features introduced. */
    public static final ProtocolVersionFeature BITMAP_FEATURES = new ProtocolVersionFeature(V1_7_0);

    /** Bitmap features introduced. */
    public static final ProtocolVersionFeature USER_ATTRIBUTES = new ProtocolVersionFeature(V1_7_1);

    /** Version in which the feature was introduced. */
    private final ProtocolVersion ver;

    /**
     * @param ver Version in which the feature was introduced.
     */
    ProtocolVersionFeature(ProtocolVersion ver) {
        this.ver = ver;
    }

    /**
     * @return Protocol version in which this feature was introduced.
     */
    public ProtocolVersion verIntroduced() {
        return ver;
    }
}
