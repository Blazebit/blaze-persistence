/*
 * Copyright 2014 - 2019 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.integration.jackson;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public class EntityViewReferenceDeserializer extends JsonDeserializer {

    private static final Map<Class<?>, IdType> ID_TYPES;

    static {
        Map<Class<?>, IdType> idTypes = new HashMap<>();
        idTypes.put(byte.class, IdType.BYTE);
        idTypes.put(Byte.class, IdType.BYTE);
        idTypes.put(short.class, IdType.SHORT);
        idTypes.put(Short.class, IdType.SHORT);
        idTypes.put(int.class, IdType.INTEGER);
        idTypes.put(Integer.class, IdType.INTEGER);
        idTypes.put(long.class, IdType.LONG);
        idTypes.put(Long.class, IdType.LONG);
        idTypes.put(float.class, IdType.FLOAT);
        idTypes.put(Float.class, IdType.FLOAT);
        idTypes.put(double.class, IdType.DOUBLE);
        idTypes.put(Double.class, IdType.DOUBLE);
        idTypes.put(char.class, IdType.CHARACTER);
        idTypes.put(Character.class, IdType.CHARACTER);
        idTypes.put(String.class, IdType.STRING);
        ID_TYPES = idTypes;
    }

    private final EntityViewManager entityViewManager;
    private final JsonDeserializer<Object> defaultDeserializer;
    private final Class<?> entityViewClass;
    private final String idAttribute;
    private final IdType idType;
    private final boolean updatable;

    public EntityViewReferenceDeserializer(EntityViewManager entityViewManager, ViewType<?> view, JsonDeserializer<Object> defaultDeserializer) {
        this.entityViewManager = entityViewManager;
        this.defaultDeserializer = defaultDeserializer;
        this.entityViewClass = view.getJavaType();
        MethodAttribute<?, ?> idAttribute = view.getIdAttribute();
        this.idAttribute = idAttribute.getName();
        IdType idType = ID_TYPES.get(idAttribute.getJavaType());
        if (idType == null) {
            throw new IllegalArgumentException("Can't create entity view reference deserializer for entity view '" + entityViewClass.getName() + "' because id attribute '" + idAttribute.getName() + "' has an unsupported id type: " + idAttribute.getJavaType().getName());
        }
        this.idType = idType;
        this.updatable = view.isUpdatable();
    }

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        Object reference = null;
        ObjectCodec codec = jsonParser.getCodec();
        JsonNode treeNode = codec.readTree(jsonParser);
        JsonNode jsonNode = treeNode.get(idAttribute);
        Object id;
        if (jsonNode != null && !jsonNode.isNull()) {
            switch (idType) {
                case BYTE:
                    id = jsonNode.numberValue().byteValue();
                    break;
                case SHORT:
                    id = jsonNode.numberValue().shortValue();
                    break;
                case INTEGER:
                    id = jsonNode.numberValue().intValue();
                    break;
                case LONG:
                    id = jsonNode.numberValue().longValue();
                    break;
                case FLOAT:
                    id = jsonNode.numberValue().floatValue();
                    break;
                case DOUBLE:
                    id = jsonNode.numberValue().doubleValue();
                    break;
                case CHARACTER:
                    String s = jsonNode.textValue();
                    if (s.length() != 1) {
                        throw new IllegalArgumentException("Expected id value to be exactly one character but was: " + s);
                    }
                    id = s.charAt(0);
                    break;
                case STRING:
                    id = jsonNode.textValue();
                    break;
                default:
                    id = null;
                    break;
            }

            reference = entityViewManager.getReference(entityViewClass, id);
            if (updatable) {
                jsonParser = codec.treeAsTokens(treeNode);
                jsonParser.nextToken();
                return deserializationContext.findNonContextualValueDeserializer(deserializationContext.constructType(reference.getClass()))
                        .deserialize(jsonParser, deserializationContext, reference);
            }
        }

        return reference;
    }

    /**
     * @author Christian Beikov
     * @since 1.4.0
     */
    enum IdType {
        BYTE,
        SHORT,
        INTEGER,
        LONG,
        FLOAT,
        DOUBLE,
        CHARACTER,
        STRING
    }
}
