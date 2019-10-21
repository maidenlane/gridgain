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

package org.gridgain.utils;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import org.apache.ignite.lang.IgniteUuid;
import org.gridgain.dto.action.Request;
import org.gridgain.dto.action.RequestDeserializer;

/**
 * Custom agent object mapper.
 */
public class AgentObjectMapperFactory {
    /** Custom serializer for {@link IgniteUuid} */
    private static final JsonSerializer<IgniteUuid> IGNITE_UUID_SERIALIZER = new JsonSerializer<IgniteUuid>() {
        /** {@inheritDoc} */
        @Override public void serialize(IgniteUuid uid, JsonGenerator gen, SerializerProvider ser) throws IOException {
            gen.writeString(uid.toString());
        }
    };

    /** Json mapper. */
    private static final ObjectMapper JSON_MAPPER = createJsonMapper();

    /** Binary mapper. */
    private static final ObjectMapper BINARY_MAPPER = createBinaryMapper();

    /**
     * @return Cached json mapper instance.
     */
    public static ObjectMapper jsonMapper() {
        return JSON_MAPPER;
    }

    /**
     * @return Cached binary mapper instance.
     */
    public static ObjectMapper binaryMapper() {
        return BINARY_MAPPER;
    }

    /**
     * New json mapper instance.
     */
    private static ObjectMapper createJsonMapper() {
        return createMapper(new MappingJsonFactory());
    }

    /**
     * @return New binary mapper instance.
     */
    private static ObjectMapper createBinaryMapper() {
        return createMapper(new SmileFactory());
    }

    /**
     * @param jf Json factory.
     */
    private static ObjectMapper createMapper(JsonFactory jf) {
        return applyConfig(applyModule(new ObjectMapper(jf)));
    }

    /**
     * @param mapper Mapper.
     */
    private static ObjectMapper applyModule(ObjectMapper mapper) {
        return mapper.registerModule(
            new SimpleModule()
                .addSerializer(IgniteUuid.class, IGNITE_UUID_SERIALIZER)
                .addDeserializer(Request.class, new RequestDeserializer())
        );
    }

    /**
     * @param mapper Mapper.
     */
    private static ObjectMapper applyConfig(ObjectMapper mapper) {
        return mapper
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
