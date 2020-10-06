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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.internal.processors.query.h2.twostep.msg.GridH2ValueMessage;
import org.apache.ignite.internal.processors.query.h2.twostep.msg.GridH2ValueMessageFactory;
import org.apache.ignite.internal.processors.query.stat.messages.StatsColumnData;
import org.apache.ignite.internal.processors.query.stat.messages.StatsObjectData;
import org.apache.ignite.internal.processors.query.stat.messages.StatsPropagationMessage;
import org.gridgain.internal.h2.value.Value;

/**
 * Utilities to convert statistics from/to messages.
 */
public class StatisticsUtils {

    public static StatsColumnData toMessage(ColumnStatistics stat) throws IgniteCheckedException {
        GridH2ValueMessage msgMin = stat.min() == null ? null : GridH2ValueMessageFactory.toMessage(stat.min());
        GridH2ValueMessage msgMax = stat.max() == null ? null : GridH2ValueMessageFactory.toMessage(stat.max());

        return new StatsColumnData(msgMin, msgMax, stat.nulls(), stat.cardinality(), stat.total(), stat.size(), stat.raw());
    }

    public static ColumnStatistics toColumnStatistics(StatsColumnData data) throws IgniteCheckedException {
        Value min = data.min().value(null);
        Value max = data.max().value(null);

        return new ColumnStatistics(min, max, data.nulls(), data.cardinality(), data.total(), data.size(), data.rawData());
    }

    /**
     * Build StatsObjectData message.
     *
     * @param schemaName schema name.
     * @param objectName object name.
     * @param type statistics type.
     * @param stat statistics.
     * @return builded StatsObjectData object.
     * @throws IgniteCheckedException
     */
    public static StatsObjectData toMessage(String schemaName, String objectName, StatsType type, ObjectStatistics stat)
            throws IgniteCheckedException {
        // TODO: pass partId and updateCounter if needed
        Map<String, StatsColumnData> columnData = new HashMap<>(stat.columnsStatistics().size());

        for(Map.Entry<String, ColumnStatistics> ts : stat.columnsStatistics().entrySet())
            columnData.put(ts.getKey(), toMessage(ts.getValue()));

        StatsObjectData data;
        if (stat instanceof ObjectPartitionStatistics) {
            ObjectPartitionStatistics partStats = (ObjectPartitionStatistics) stat;
            data = new StatsObjectData(schemaName, objectName, stat.rowCount(), type, partStats.partId(),
                    partStats.updCnt(), columnData);
        } else
            data = new StatsObjectData(schemaName, objectName, stat.rowCount(), type, 0,0, columnData);
        return data;
    }


    public static StatsPropagationMessage toMessage(UUID reqId, String schemaName, String objectName, StatsType type,
                                                    ObjectStatistics stat) throws IgniteCheckedException {

        StatsObjectData data = toMessage(schemaName, objectName, type, stat);
        return new StatsPropagationMessage(reqId, Collections.singletonList(data));
    }


    public static ObjectPartitionStatistics toObjectPartitionStatistics(StatsObjectData objData) throws IgniteCheckedException {
        if (objData == null)
            return null;

        assert objData.type == StatsType.PARTITION;

        Map<String, ColumnStatistics> colNameToStat = new HashMap<>(objData.data.size());

        for (Map.Entry<String, StatsColumnData> cs : objData.data.entrySet()) {
            colNameToStat.put(cs.getKey(), toColumnStatistics(cs.getValue()));
        }

        return new ObjectPartitionStatistics(objData.partId, true, objData.rowsCnt, objData.updCnt, colNameToStat);
    }

    public static ObjectStatistics toObjectStatistics(StatsPropagationMessage data) throws IgniteCheckedException {
        if (data == null)
            return null;

        assert data.data().size() == 1;

        StatsObjectData objData = data.data().get(0);
        Map<String, ColumnStatistics> colNameToStat = new HashMap<>(objData.data.size());

        for (Map.Entry<String, StatsColumnData> cs : objData.data.entrySet()) {
            colNameToStat.put(cs.getKey(), toColumnStatistics(cs.getValue()));
        }

        return new ObjectStatistics(objData.rowsCnt, colNameToStat);
    }

}
