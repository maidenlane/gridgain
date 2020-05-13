/*
 * Copyright 2020 GridGain Systems, Inc. and Contributors.
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
import org.apache.ignite.internal.dto.IgniteDataTransferObject;
import org.apache.ignite.internal.processors.tracing.Scope;
import org.apache.ignite.internal.processors.tracing.Span;
import org.apache.ignite.internal.util.typedef.internal.S;
import org.apache.ignite.internal.util.typedef.internal.U;

/**
 * Data transfer object that contains scope, label, sampling rate and set of supported scopes.
 */
@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
public class VisorTracingConfigurationItem extends IgniteDataTransferObject {
    /** */
    private static final long serialVersionUID = 0L;

    /**
     * Specifies the {@link Scope} of a trace's root span to which some specific tracing configuration will be applied.
     * It's a mandatory attribute.
     */
    private Scope scope;

    /**
     * Specifies the label of a traced operation. It's an optional attribute.
     */
    private String lb;

    /**
     * Number between 0 and 1 that more or less reflects the probability of sampling specific trace. 0 and 1 have
     * special meaning here, 0 means never 1 means always. Default value is 0 (never).
     */
    private Double samplingRate;

    /**
     * Set of {@link Scope} that defines which sub-traces will be included in given trace. In other words, if child's
     * span scope is equals to parent's scope or it belongs to the parent's span supported scopes, then given child span
     * will be attached to the current trace, otherwise it'll be skipped. See {@link
     * Span#isChainable(org.apache.ignite.internal.processors.tracing.Scope)} for more details.
     */
    private Set<Scope> supportedScopes;

    /**
     * Default constructor.
     */
    public VisorTracingConfigurationItem() {
        // No-op.
    }

    /**
     * Constructor.
     *
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
    public VisorTracingConfigurationItem(
        Scope scope,
        String lb,
        Double samplingRate,
        Set<Scope> supportedScopes)
    {
        this.scope = scope;
        this.lb = lb;
        this.samplingRate = samplingRate;
        this.supportedScopes = supportedScopes;
    }

    /**
     * @return Specifies the  of a trace's root span to which some specific tracing configuration will be applied. It's
     * a mandatory attribute.
     */
    public Scope scope() {
        return scope;
    }

    /**
     * @return Specifies the label of a traced operation. It's an optional attribute.
     */
    public String label() {
        return lb;
    }

    /**
     * @return Number between 0 and 1 that more or less reflects the probability of sampling specific trace. 0 and 1
     * have special meaning here, 0 means never 1 means always. Default value is 0 (never).
     */
    public Double samplingRate() {
        return samplingRate;
    }

    /**
     * @return Set of  that defines which sub-traces will be included in given trace. In other words, if child's span
     * scope is equals to parent's scope or it belongs to the parent's span supported scopes, then given child span will
     * be attached to the current trace, otherwise it'll be skipped. See  for more details.
     */
    public Set<Scope> supportedScopes() {
        return supportedScopes;
    }

    /** {@inheritDoc} */
    @Override protected void writeExternalData(ObjectOutput out) throws IOException {
        out.writeBoolean(scope != null);

        if (scope != null)
            out.writeShort(scope.idx());

        U.writeString(out, label());

        out.writeObject(samplingRate);

        U.writeCollection(out, supportedScopes);
    }

    /** {@inheritDoc} */
    @Override protected void readExternalData(byte protoVer,
        ObjectInput in) throws IOException, ClassNotFoundException {

        if (in.readBoolean())
            scope = Scope.fromIndex(in.readShort());

        lb = U.readString(in);

        samplingRate = (Double)in.readObject();

        supportedScopes = U.readSet(in);
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(VisorTracingConfigurationItem.class, this);
    }

    /** {@inheritDoc} */
    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        VisorTracingConfigurationItem that = (VisorTracingConfigurationItem)o;

        if (scope != that.scope)
            return false;
        if (lb != null ? !lb.equals(that.lb) : that.lb != null)
            return false;
        if (samplingRate != null ? !samplingRate.equals(that.samplingRate) : that.samplingRate != null)
            return false;
        return supportedScopes != null ? supportedScopes.equals(that.supportedScopes) : that.supportedScopes == null;
    }
}
