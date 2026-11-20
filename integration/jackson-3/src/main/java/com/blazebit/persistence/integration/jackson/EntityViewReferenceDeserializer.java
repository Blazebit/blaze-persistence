/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.jackson;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.node.ObjectNode;

import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public class EntityViewReferenceDeserializer extends ValueDeserializer<Object> {

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
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws JacksonException {
        JsonNode treeNode = deserializationContext.readTree(jsonParser);
        Object id = retrieveId(jsonParser, deserializationContext, treeNode, !creatable || updatable);
        Object reference;

        if (creatable && (!updatable || id == null)) {
            reference = entityViewManager.create(entityViewClass);
        } else if (id != null) {
            reference = entityViewManager.getReference(entityViewClass, id);
        } else {
            reference = null;
        }

        if (reference == null) {
            return null;
        }

        jsonParser = deserializationContext.treeAsTokens(treeNode);
        jsonParser.nextToken();
        return deserializationContext.findContextualValueDeserializer(deserializationContext.constructType(reference.getClass()), null)
                .deserialize(jsonParser, deserializationContext, reference);
    }

    private Object retrieveId(JsonParser rootJsonParser, DeserializationContext deserializationContext, JsonNode treeNode, boolean consume) throws JacksonException {
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
                    id = deserializationContext.readValue(deserializationContext.treeAsTokens(jsonNode), idType);
                    if (consume) {
                        ((ObjectNode) treeNode).without(idAttributeName);
                    }
                }
            } else if (rootJsonParser.streamReadContext().inRoot() && entityViewIdValueAccessor != null) {
                id = entityViewIdValueAccessor.getValue(rootJsonParser, idType.getRawClass());
            } else {
                id = null;
            }
        }

        return id;
    }
}
