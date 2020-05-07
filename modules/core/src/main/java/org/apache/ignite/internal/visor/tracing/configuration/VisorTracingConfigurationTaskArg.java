/*
 * Copyright 2019 GridGain Systems, Inc. and Contributors.
 *
 * Licensed under the GridGain Community Edition License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gridgain.com/products/software/community-edition/gridgain-community-edition-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.visor.tracing.configuration;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Set;
import org.apache.ignite.internal.processors.tracing.Scope;
import org.apache.ignite.internal.processors.tracing.Span;
import org.apache.ignite.internal.util.typedef.internal.S;
import org.apache.ignite.internal.util.typedef.internal.U;

/**
 * Argument for {@link VisorTracingConfigurationTask}.
 */
public class VisorTracingConfigurationTaskArg extends VisorTracingConfigurationItem {
    /** */
    private static final long serialVersionUID = 0L;

    /** */
    private VisorTracingConfigurationOperation op;

    /**
     * Default constructor.
     */
    public VisorTracingConfigurationTaskArg() {
        // No-op.
    }

    /**
     * Constructor.
     *
     * @param op Operation.
     * @param scope Specifies the {@link Scope} of a trace's root span to which some specific tracing configuration will be applied.
     * @param lb Specifies the label of a traced operation. It's an optional attribute.
     * @param samplingRate Number between 0 and 1 that more or less reflects the probability of sampling specific trace.
     *  0 and 1 have special meaning here, 0 means never 1 means always. Default value is 0 (never).
     * @param supportedScopes Set of {@link Scope} that defines which sub-traces will be included in given trace.
     *  In other words, if child's span scope is equals to parent's scope
     *  or it belongs to the parent's span supported scopes, then given child span will be attached to the current trace,
     *  otherwise it'll be skipped.
     *  See {@link Span#isChainable(org.apache.ignite.internal.processors.tracing.Scope)} for more details.
     */
    public VisorTracingConfigurationTaskArg(
        VisorTracingConfigurationOperation op,
        Scope scope,
        String lb,
        Double samplingRate,
        Set<Scope> supportedScopes)
    {
        super(scope,
            lb,
            samplingRate,
            supportedScopes);

        this.op = op;
    }

    /**
     * @return Operation.
     */
    public VisorTracingConfigurationOperation operation() {
        return op;
    }

    /** {@inheritDoc} */
    @Override protected void writeExternalData(ObjectOutput out) throws IOException {
        U.writeEnum(out, op);

        super.writeExternalData(out);
    }

    /** {@inheritDoc} */
    @Override protected void readExternalData(
        byte protoVer,
        ObjectInput in) throws IOException, ClassNotFoundException
    {
        op = VisorTracingConfigurationOperation.fromOrdinal(in.readByte());

        super.readExternalData(protoVer, in);
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(VisorTracingConfigurationTaskArg.class, this);
    }
}
