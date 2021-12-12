/*
 * Copyright 2014 - 2021 Blazebit.
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

package com.blazebit.persistence.integration.jsonb;

import com.blazebit.persistence.integration.jsonb.jsonstructure.JsonStructureToParserAdapter;
import com.blazebit.persistence.integration.jsonb.jsonstructure.JsonValueToParserAdapter;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;
import org.eclipse.yasson.internal.JsonbRiParser;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.spi.JsonbProvider;
import javax.json.stream.JsonParser;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.6.4
 */
public class EntityViewReferenceDeserializer {

    private static final ThreadLocal<EntityViewReferenceDeserializer> STACK = new ThreadLocal<>();
    private static final boolean IS_YASSON = "org.eclipse.yasson".equals(JsonbProvider.provider().getClass().getPackage().getName());

    private final EntityViewManager entityViewManager;
    private final EntityViewIdValueAccessor entityViewIdValueAccessor;
    private final Class<?> entityViewClass;
    private final String idAttributeName;
    private final MethodAttribute<?, ?> idAttribute;
    private final Class<?> idType;
    private final Map<String, AbstractMethodAttribute<?, ?>> attributes;
    private final boolean deserializeIdFromJson;
    private final boolean updatable;
    private final boolean creatable;

    public EntityViewReferenceDeserializer(EntityViewManager entityViewManager, ManagedViewType<?> view, EntityViewIdValueAccessor entityViewIdValueAccessor) {
        this.entityViewManager = entityViewManager;
        this.entityViewClass = view.getJavaType();
        this.entityViewIdValueAccessor = entityViewIdValueAccessor;
        if (view instanceof ViewType<?>) {
            this.idAttribute = ((ViewType<?>) view).getIdAttribute();
            this.deserializeIdFromJson = true;
            JsonbProperty jsonbProperty = idAttribute.getJavaMethod().getAnnotation(JsonbProperty.class);
            String name;
            if (jsonbProperty == null) {
                name = idAttribute.getName();
            } else {
                name = jsonbProperty.value();
            }
            this.idAttributeName = name;
            this.idType = idAttribute.getConvertedJavaType();
        } else {
            this.idAttribute = null;
            this.idAttributeName = null;
            this.idType = null;
            this.deserializeIdFromJson = false;
        }
        this.updatable = view.isUpdatable();
        this.creatable = view.isCreatable();
        Map<String, AbstractMethodAttribute<?, ?>> attributes = new HashMap<>(view.getAttributes().size());
        for (MethodAttribute<?, ?> attribute : view.getAttributes()) {
            if (attribute.getJavaMethod().getAnnotation(JsonbTransient.class) != null) {
                continue;
            }
            JsonbProperty jsonbProperty = attribute.getJavaMethod().getAnnotation(JsonbProperty.class);
            String name;
            if (jsonbProperty == null) {
                name = attribute.getName();
            } else {
                name = jsonbProperty.value();
            }
            attributes.put(name, (AbstractMethodAttribute<?, ?>) attribute);
        }
        this.attributes = attributes;
    }

    public <T> T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        boolean isRoot = STACK.get() == null;
        try {
            if (isRoot) {
                STACK.set(this);
            }
            Object reference = null;
            JsonObject jsonStructure = deserializationContext.deserialize(JsonObject.class, jsonParser);
            boolean idConsumed = false;
            Object id;
            if (idAttributeName == null || idType == null) {
                id = null;
            } else {
                JsonValue jsonNode;
                if (deserializeIdFromJson && (jsonNode = jsonStructure.get(idAttributeName)) != null) {
                    if (jsonNode.getValueType() == JsonValue.ValueType.NULL) {
                        id = null;
                    } else {
                        id = deserializationContext.deserialize(idType, parser(jsonNode));
                        // Consume (i.e. remove from the payload json tree) the id if we are going to use getReference
                        if (!creatable || updatable) {
                            idConsumed = true;
                        }
                    }
                } else if (isRoot && entityViewIdValueAccessor != null) {
                    id = entityViewIdValueAccessor.getValue(jsonParser, deserializationContext, idType);
                } else {
                    id = null;
                }
            }

            // We create also creatable & updatable views if no id is given
            // If an id is given in such a case, we create a reference for updates
            if (creatable && (!updatable || id == null)) {
                reference = entityViewManager.create(entityViewClass);
            } else if (id != null) {
                reference = entityViewManager.getReference(entityViewClass, id);
            }

            if (reference == null) {
                return null;
            }

            try {
                for (Map.Entry<String, JsonValue> entry : jsonStructure.entrySet()) {
                    AbstractMethodAttribute<?, ?> attribute = attributes.get(entry.getKey());
                    if (attribute == null) {
                        throw new RuntimeException("Unknown attribute [" + entry.getKey() + "]");
                    }
                    if (attribute == idAttribute) {
                        if (idConsumed) {
                            continue;
                        } else if (id != null) {
                            attribute.getSetterMethod().invoke(reference, id);
                            continue;
                        }
                    }
                    JsonValue jsonValue = entry.getValue();
                    if (attribute.isCollection()) {
                        Type elementType = attribute.getElementType().getConvertedType();
                        if (elementType == null) {
                            elementType = attribute.getElementType().getJavaType();
                        }
                        Object container = attribute.getJavaMethod().invoke(reference);
                        JsonArray jsonValues = jsonValue.asJsonArray();
                        if (container instanceof Map<?, ?>) {
                            Map<Object, Object> map = (Map<Object, Object>) container;
                            for (JsonValue entryValue : jsonValues) {
                                JsonObject entryObject = entryValue.asJsonObject();
                                if (entryObject.size() != 1) {
                                    throw new RuntimeException("Unexpected non-entry like element [" + entryObject + "]");
                                }
                                Map.Entry<String, JsonValue> entryValueEntry = entryObject.entrySet().iterator().next();
                                map.put(entryValueEntry.getKey(), deserializationContext.deserialize(elementType, parser(entryValueEntry.getValue())));
                            }
                        } else {
                            Collection<Object> collection = (Collection<Object>) container;
                            for (JsonValue value : jsonValues) {
                                collection.add(deserializationContext.deserialize(elementType, parser(value)));
                            }
                        }
                    } else {
                        if (attribute.getSetterMethod() == null) {
                            throw new RuntimeException("Unexpected non-updatable attribute [" + entry.getKey() + "]");
                        }
                        Type elementType = attribute.getElementType().getConvertedType();
                        if (elementType == null) {
                            elementType = attribute.getElementType().getJavaType();
                        }
                        Object value = deserializationContext.deserialize(elementType, parser(jsonValue));
                        attribute.getSetterMethod().invoke(reference, value);
                    }
                }
            } catch (Exception ex) {
                throw new RuntimeException("Couldn't deserialize: " + jsonStructure, ex);
            }

            return (T) reference;
        } finally {
            if (isRoot) {
                STACK.remove();
            }
        }
    }

    private static JsonParser parser(JsonValue jsonValue) {
        JsonParser newParser;
        if (jsonValue instanceof JsonStructure) {
            newParser = new JsonStructureToParserAdapter((JsonStructure) jsonValue);
        } else {
            newParser = new JsonValueToParserAdapter(jsonValue);
        }
        if (IS_YASSON) {
            return new JsonbRiParser(newParser);
        }
        return newParser;
    }
}
