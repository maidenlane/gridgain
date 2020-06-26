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

package org.apache.ignite.internal;

import java.io.Externalizable;
import org.apache.ignite.IgniteCheckedException;
import org.jetbrains.annotations.Nullable;

/**
 * When log debug mode is disabled this exception should be logged in short form - without stack trace.
 */
public class GridInternalException extends IgniteCheckedException {
    /** */
    private static final long serialVersionUID = 0L;

    /**
     * {@link Externalizable} support.
     */
    public GridInternalException() {
        // No-op.
    }

    /**
     * Creates new internal exception with given error message.
     *
     * @param msg Error message.
     */
    public GridInternalException(String msg) {
        super(msg);
    }

    /**
     * Creates new internal exception given throwable as a cause and
     * source of error message.
     *
     * @param cause Non-null throwable cause.
     */
    public GridInternalException(Throwable cause) {
        super(cause.getMessage(), cause);
    }

    /**
     * Creates new internal exception with given error message and
     * optional nested exception.
     *
     * @param msg Exception message.
     * @param cause Optional cause.
     */
    public GridInternalException(String msg, @Nullable Throwable cause) {
        super(msg, cause);
    }
}
