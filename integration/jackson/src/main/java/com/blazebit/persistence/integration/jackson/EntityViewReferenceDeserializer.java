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

package com.blazebit.persistence.integration.jackson;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public class EntityViewReferenceDeserializer extends JsonDeserializer {

    private final EntityViewManager entityViewManager;
    private final EntityViewIdValueAccessor entityViewIdValueAccessor;
    private final Class<?> entityViewClass;
    private final MethodAttribute<?, ?> idAttribute;
    private final JavaType idType;
    private final boolean deserializeIdFromJson;
    private final boolean updatable;
    private final boolean creatable;

    public EntityViewReferenceDeserializer(EntityViewManager entityViewManager, ManagedViewType<?> view, ObjectMapper objectMapper, Set<String> ignoredProperties, EntityViewIdValueAccessor entityViewIdValueAccessor) {
        this.entityViewManager = entityViewManager;
        this.entityViewClass = view.getJavaType();
        this.entityViewIdValueAccessor = entityViewIdValueAccessor;
        if (view instanceof ViewType<?>) {
            MethodAttribute<?, ?> idAttribute = ((ViewType<?>) view).getIdAttribute();
            this.deserializeIdFromJson = !ignoredProperties.contains(idAttribute.getName());
            this.idAttribute = idAttribute;
            JavaType idType = objectMapper.getTypeFactory().constructType(this.idAttribute.getConvertedJavaType());
            if (!objectMapper.canDeserialize(idType)) {
                throw new IllegalArgumentException("Can't create entity view reference deserializer for entity view '" + entityViewClass.getName() + "' because id attribute '" + this.idAttribute.getName() + "' has an unsupported id type: " + this.idAttribute.getJavaType().getName());
            }
            this.idType = idType;
        } else {
            this.idAttribute = null;
            this.idType = null;
            this.deserializeIdFromJson = false;
        }
        this.updatable = view.isUpdatable();
        this.creatable = view.isCreatable();
    }

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        Object reference = null;
        ObjectCodec codec = jsonParser.getCodec();
        JsonNode treeNode = codec.readTree(jsonParser);
        // Consume (i.e. remove from the payload json tree) the id if we are going to use getReference
        Object id = retrieveId(jsonParser, codec, treeNode, !creatable || updatable);

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

        jsonParser = codec.treeAsTokens(treeNode);
        jsonParser.nextToken();
        return deserializationContext.findNonContextualValueDeserializer(deserializationContext.constructType(reference.getClass()))
                .deserialize(jsonParser, deserializationContext, reference);
    }

    private Object retrieveId(JsonParser rootJsonParser, ObjectCodec codec, JsonNode treeNode, boolean consume) throws IOException {
        Object id;
        if (idAttribute == null || idType == null) {
            id = null;
        } else {
            String idAttributeName = idAttribute.getName();
            JsonNode jsonNode;
            if (deserializeIdFromJson && (jsonNode = treeNode.get(idAttributeName)) != null) {
                if (jsonNode.isNull()) {
                    id = null;
                } else {
                    id = codec.readValue(codec.treeAsTokens(jsonNode), idType);
                    if (consume) {
                        ((ObjectNode) treeNode).without(idAttributeName);
                    }
                }
            } else if (entityViewIdValueAccessor != null) {
                id = entityViewIdValueAccessor.getValue(rootJsonParser, idType.getRawClass());
            } else {
                id = null;
            }
        }

        return id;
    }
}
