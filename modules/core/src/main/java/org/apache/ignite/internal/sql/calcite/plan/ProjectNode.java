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
package org.apache.ignite.internal.sql.calcite.plan;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Collections;

/**
 * TODO: Add class description.
 */
public class ProjectNode implements PlanNode {
    private PlanNode input;

    private int[] prjIdx; // TODO other types of projections (RexNode)

    public ProjectNode(int[] prjIdx, PlanNode input) {
        this.prjIdx = prjIdx;
        this.input = input;
    }

    @Override public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(prjIdx);
        out.writeObject(input);
    }

    @Override public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        prjIdx = (int[])in.readObject();
        input = (PlanNode)in.readObject();
    }


    @Override public String toString(int level) {
        String margin = String.join("", Collections.nCopies(level, "  "));

        StringBuilder sb = new StringBuilder("\n");

        sb.append(margin)
            .append("ProjectNode [prjIdx=")
            .append(Arrays.toString(prjIdx))
            .append("]")
            .append(input.toString(level + 1));

        return sb.toString();
    }

    @Override public String toString() {
        return toString(0);
    }
}
