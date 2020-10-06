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
package org.apache.ignite.internal.processors.query.stat;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * All statistics by some object (table or index)
 */
public class ObjectStatistics implements Serializable, Cloneable {
    /** Total number of rows in object. */
    private final long rowsCnt;

    /** Map columnKey to its statistic. */
    private final Map<String, ColumnStatistics> colNameToStat;

    /**
     * Constructor.
     *
     * @param rowsCnt rows count.
     * @param colNameToStat columns to column statistics map.
     */
    public ObjectStatistics(long rowsCnt, Map<String, ColumnStatistics> colNameToStat) {
        assert rowsCnt >= 0: "rowsCnt >= 0";
        assert colNameToStat != null: "colNameToStat != null";

        this.rowsCnt = rowsCnt;
        this.colNameToStat = Collections.unmodifiableMap(colNameToStat);
    }

    /**
     * @return object rows count.
     */
    public long rowCount() {
        return rowsCnt;
    }

    /**
     * Get column statistics.
     *
     * @param colName column name.
     * @return column statistics or {@code null} if there are no statistics for specified column.
     */
    public ColumnStatistics columnStatistics(String colName) {
        return colNameToStat.get(colName);
    }

    /**
     * Clone object.
     *
     * @return clone.
     */
    public ObjectStatistics clone() {
        return new ObjectStatistics(rowsCnt, new HashMap<>(colNameToStat));
    }

    /**
     * @return column name to column statistics map.
     */
    public Map<String, ColumnStatistics> columnsStatistics() {
        return colNameToStat;
    }
}


