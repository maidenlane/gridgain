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

package org.apache.ignite.internal.processors.query.h2.database;

import java.util.List;
import org.apache.ignite.IgniteException;
import org.apache.ignite.internal.processors.cache.GridCacheContext;
import org.apache.ignite.internal.processors.query.IgniteSQLException;
import org.apache.ignite.internal.processors.query.h2.opt.GridH2IndexBase;
import org.apache.ignite.internal.processors.query.h2.opt.GridH2Table;
import org.apache.ignite.internal.processors.query.h2.opt.H2CacheRow;
import org.apache.ignite.spi.indexing.IndexingQueryCacheFilter;
import org.gridgain.internal.h2.command.dml.AllColumnsForPlan;
import org.gridgain.internal.h2.engine.Session;
import org.gridgain.internal.h2.index.Cursor;
import org.gridgain.internal.h2.index.IndexCondition;
import org.gridgain.internal.h2.index.IndexType;
import org.gridgain.internal.h2.result.SearchRow;
import org.gridgain.internal.h2.result.SortOrder;
import org.gridgain.internal.h2.table.Column;
import org.gridgain.internal.h2.table.IndexColumn;
import org.gridgain.internal.h2.table.TableFilter;

/**
 *
 */
public class H2PkHashClientIndex extends GridH2IndexBase {
    /** */
    private final GridCacheContext cctx;

    /**
     * @param cctx Cache context.
     * @param tbl Table.
     * @param name Index name.
     * @param colsList Index columns.
     */
    @SuppressWarnings("ZeroLengthArrayAllocation")
    public H2PkHashClientIndex(
        GridCacheContext<?, ?> cctx,
        GridH2Table tbl,
        String name,
        List<IndexColumn> colsList
    ) {
        super(
            tbl,
            name,
            GridH2IndexBase.columnsArray(tbl, colsList),
            IndexType.createPrimaryKey(false, true));

        this.cctx = cctx;
    }

    /** {@inheritDoc} */
    @Override public int segmentsCount() {
        throw unsupported();
    }

    /** {@inheritDoc} */
    @Override public Cursor find(Session ses, final SearchRow lower, final SearchRow upper) {
        throw unsupported();
    }

    /** {@inheritDoc} */
    @Override public boolean canScan() {
        return false;
    }

    /** {@inheritDoc} */
    @Override public H2CacheRow put(H2CacheRow row) {
        throw unsupported();
    }

    /** {@inheritDoc} */
    @Override public boolean putx(H2CacheRow row) {
        throw unsupported();
    }

    /** {@inheritDoc} */
    @Override public boolean removex(SearchRow row) {
        throw unsupported();
    }

    /** {@inheritDoc} */
    @Override public double getCost(Session ses, int[] masks, TableFilter[] filters, int filter,
        SortOrder sortOrder, AllColumnsForPlan allColsSet) {
        if (masks == null)
            return Long.MAX_VALUE;

        for (Column column : columns) {
            int index = column.getColumnId();
            int mask = masks[index];

            if ((mask & IndexCondition.EQUALITY) != IndexCondition.EQUALITY)
                return Long.MAX_VALUE;
        }

        return 2;
    }

    /** {@inheritDoc} */
    @Override public long getRowCount(Session ses) {
        throw unsupported();
    }

    /** {@inheritDoc} */
    @Override public boolean canGetFirstOrLast() {
        return false;
    }

    /** {@inheritDoc} */
    @Override public Cursor findFirstOrLast(Session ses, boolean b) {
        throw  unsupported();
    }

    /** {@inheritDoc} */
    @Override public long totalRowCount(IndexingQueryCacheFilter partsFilter) {
        throw unsupported();
    }

    /**
     * @return Exception about unsupported operation.
     */
    private static IgniteException unsupported() {
        return new IgniteSQLException("Shouldn't be invoked on non-affinity node.");
    }
}