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

package com.blazebit.persistence.integration.graphql;

import com.blazebit.lang.StringUtils;
import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.reflection.ReflectionUtils;
import graphql.language.Directive;
import graphql.language.EnumTypeDefinition;
import graphql.language.EnumValueDefinition;
import graphql.language.FieldDefinition;
import graphql.language.InterfaceTypeDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.ObjectTypeDefinition;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A factory for creating a support class for using entity views in a GraphQL environment.
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class GraphQLEntityViewSupportFactory {

    private static final Map<Class<?>, String> TYPES;

    static {
        Map<Class<?>, String> types = new HashMap<>();
        types.put(boolean.class, "Boolean");
        types.put(Boolean.class, "Boolean");
        types.put(byte.class, "Byte");
        types.put(Byte.class, "Byte");
        types.put(short.class, "Short");
        types.put(Short.class, "Short");
        types.put(int.class, "Int");
        types.put(Integer.class, "Int");
        types.put(long.class, "Long");
        types.put(Long.class, "Long");
        types.put(float.class, "Float");
        types.put(Float.class, "Float");
        types.put(double.class, "Float");
        types.put(Double.class, "Float");
        types.put(BigInteger.class, "BigInteger");
        types.put(BigDecimal.class, "BigDecimal");
        types.put(char.class, "Char");
        types.put(Character.class, "Char");
        types.put(String.class, "String");
        TYPES = types;
    }

    private boolean defineNormalTypes;
    private boolean defineRelayTypes;
    private Boolean implementRelayNode;
    private boolean defineRelayNodeIfNotExist = false;

    /**
     * Creates a new entity view support factory with the given configuration.
     *
     * @param defineNormalTypes If <code>true</code>, generates normal types for managed view types
     * @param defineRelayTypes If <code>true</code>, generates relay types for managed views
     */
    public GraphQLEntityViewSupportFactory(boolean defineNormalTypes, boolean defineRelayTypes) {
        this.defineNormalTypes = defineNormalTypes;
        this.defineRelayTypes = defineRelayTypes;
    }

    /**
     * Returns <code>true</code> if normal types should be defined.
     *
     * @return <code>true</code> if normal types should be defined
     */
    public boolean isDefineNormalTypes() {
        return defineNormalTypes;
    }

    /**
     * Sets whether normal types should be defined.
     *
     * @param defineNormalTypes Whether normal types should be defined
     */
    public void setDefineNormalTypes(boolean defineNormalTypes) {
        this.defineNormalTypes = defineNormalTypes;
    }

    /**
     * Returns <code>true</code> if Relay types should be defined.
     *
     * @return <code>true</code> if Relay types should be defined
     */
    public boolean isDefineRelayTypes() {
        return defineRelayTypes;
    }

    /**
     * Sets whether Relay types should be defined.
     *
     * @param defineRelayTypes Whether Relay types should be defined
     */
    public void setDefineRelayTypes(boolean defineRelayTypes) {
        this.defineRelayTypes = defineRelayTypes;
    }

    /**
     * Returns <code>true</code> if node types should implement the Relay Node interface.
     *
     * @return <code>true</code> if node types should implement the Relay Node interface
     */
    public boolean isImplementRelayNode() {
        return implementRelayNode == null ? defineRelayNodeIfNotExist : implementRelayNode;
    }

    /**
     * Sets whether Relay Node type should be implemented by node types.
     *
     * @param implementRelayNode Whether Relay Node type should be implemented by node types
     */
    public void setImplementRelayNode(boolean implementRelayNode) {
        this.implementRelayNode = implementRelayNode;
    }

    /**
     * Returns <code>true</code> if the Relay Node interface should be created if not found in the type registry.
     *
     * @return <code>true</code> if the Relay Node interface should be created if not found in the type registry
     */
    public boolean isDefineRelayNodeIfNotExist() {
        return defineRelayNodeIfNotExist;
    }

    /**
     * Sets whether the Relay Node interface should be defined if not found in the type registry.
     *
     * @param defineRelayNodeIfNotExist Whether the Relay Node interface should be defined if not found in the type registry
     */
    public void setDefineRelayNodeIfNotExist(boolean defineRelayNodeIfNotExist) {
        this.defineRelayNodeIfNotExist = defineRelayNodeIfNotExist;
    }

    /**
     * Returns a new {@link GraphQLEntityViewSupport} after registering the entity view types from {@link EntityViewManager}
     * on the given {@link TypeDefinitionRegistry}.
     *
     * @param typeRegistry The registry to register types
     * @param entityViewManager The entity view manager
     * @return a new {@link GraphQLEntityViewSupport}
     */
    public GraphQLEntityViewSupport create(TypeDefinitionRegistry typeRegistry, EntityViewManager entityViewManager) {
        Map<String, Class<?>> typeNameToClass = new HashMap<>();
        for (ManagedViewType<?> managedView : entityViewManager.getMetamodel().getManagedViews()) {
            String typeName = getObjectTypeName(managedView);
            List<FieldDefinition> fieldDefinitions = new ArrayList<>();
            for (MethodAttribute<?, ?> attribute : managedView.getAttributes()) {
                Type type;
                if (attribute instanceof SingularAttribute<?, ?>) {
                    SingularAttribute<?, ?> singularAttribute = (SingularAttribute<?, ?>) attribute;
                    if (singularAttribute.isSubview() && singularAttribute.isId()) {
                        // EmbeddedId
                        type = getElementType(typeRegistry, singularAttribute);
                    } else if (singularAttribute.isId()) {
                        // Usual numeric ID
                        type = getIdType(typeRegistry, singularAttribute);
                    } else {
                        type = getElementType(typeRegistry, singularAttribute);
                    }
                } else if (attribute instanceof MapAttribute<?, ?, ?>) {
                    MapAttribute<?, ?, ?> mapAttribute = (MapAttribute<?, ?, ?>) attribute;
                    type = getEntryType(typeRegistry, attribute, getKeyType(typeRegistry, mapAttribute), getElementType(typeRegistry, mapAttribute));
                } else {
                    type = new ListType(getElementType(typeRegistry, (PluralAttribute<?, ?, ?>) attribute));
                }
                FieldDefinition fieldDefinition = new FieldDefinition(attribute.getName(), type);
                fieldDefinitions.add(fieldDefinition);
            }
            List<Type> implementsTypes = new ArrayList<>(0);
            List<Directive> directives = new ArrayList<>(0);
            addObjectTypeDefinition(
                typeRegistry,
                typeNameToClass,
                managedView,
                ObjectTypeDefinition.newObjectTypeDefinition()
                  .name(typeName)
                  .implementz(implementsTypes)
                  .directives(directives)
                  .fieldDefinitions(fieldDefinitions)
                .build()
            );
        }

        Set<String> serializableBasicTypes = new HashSet<>();
        for (javax.persistence.metamodel.Type<?> basicType : entityViewManager.getService(EntityMetamodel.class).getBasicTypes()) {
            for (Class<?> superType : ReflectionUtils.getSuperTypes(basicType.getJavaType())) {
                serializableBasicTypes.add(superType.getName());
            }

            serializableBasicTypes.add(basicType.getJavaType().getName());
        }

        serializableBasicTypes.add(Serializable[].class.getName());
        serializableBasicTypes.add(GraphQLCursor.class.getName());
        return new GraphQLEntityViewSupport(typeNameToClass, serializableBasicTypes);
    }

    protected void addObjectTypeDefinition(TypeDefinitionRegistry typeRegistry, Map<String, Class<?>> typeNameToClass, ManagedViewType<?> managedView, ObjectTypeDefinition objectTypeDefinition) {
        if (isDefineNormalTypes()) {
            registerManagedViewType(typeRegistry, typeNameToClass, managedView, objectTypeDefinition);
        }
        if (isDefineRelayTypes()) {
            List<Type> implementTypes = new ArrayList<>(objectTypeDefinition.getImplements());
            if (isImplementRelayNode()) {
                implementTypes.add(new TypeName("Node"));
            }
            ObjectTypeDefinition nodeType = ObjectTypeDefinition.newObjectTypeDefinition()
                .name(objectTypeDefinition.getName() + "Node")
                .implementz(implementTypes)
                .directives(objectTypeDefinition.getDirectives())
                .fieldDefinitions(objectTypeDefinition.getFieldDefinitions())
                .build();

            if (!typeRegistry.getType("Node").isPresent() && isImplementRelayNode() && isDefineRelayNodeIfNotExist()) {
                List<FieldDefinition> nodeFields = new ArrayList<>(4);
                nodeFields.add(new FieldDefinition("id", new NonNullType(new TypeName("ID"))));
                typeRegistry.add(InterfaceTypeDefinition.newInterfaceTypeDefinition().name("Node").definitions(nodeFields).build());
            }

            List<FieldDefinition> edgeFields = new ArrayList<>(2);
            edgeFields.add(new FieldDefinition("node", new NonNullType(new TypeName(nodeType.getName()))));
            edgeFields.add(new FieldDefinition("cursor", new NonNullType(new TypeName("String"))));
            ObjectTypeDefinition edgeType = ObjectTypeDefinition.newObjectTypeDefinition()
                .name(objectTypeDefinition.getName() + "Edge")
                .fieldDefinitions(edgeFields)
              .build();

            List<FieldDefinition> connectionFields = new ArrayList<>(2);
            connectionFields.add(new FieldDefinition("edges", new ListType(new TypeName(edgeType.getName()))));
            connectionFields.add(new FieldDefinition("pageInfo", new NonNullType(new TypeName("PageInfo"))));
            connectionFields.add(new FieldDefinition("totalCount", new NonNullType(new TypeName("Int"))));
            ObjectTypeDefinition connectionType =  ObjectTypeDefinition.newObjectTypeDefinition()
                .name(objectTypeDefinition.getName() + "Connection")
                .fieldDefinitions(connectionFields)
              .build();

            if (!typeRegistry.getType("PageInfo").isPresent() && isDefineRelayNodeIfNotExist()) {
                List<FieldDefinition> pageInfoFields = new ArrayList<>(4);
                pageInfoFields.add(new FieldDefinition("hasNextPage", new NonNullType(new TypeName("Boolean"))));
                pageInfoFields.add(new FieldDefinition("hasPreviousPage", new NonNullType(new TypeName("Boolean"))));
                pageInfoFields.add(new FieldDefinition("startCursor", new TypeName("String")));
                pageInfoFields.add(new FieldDefinition("endCursor", new TypeName("String")));
                typeRegistry.add(ObjectTypeDefinition.newObjectTypeDefinition()
                  .name("PageInfo")
                  .fieldDefinitions(pageInfoFields)
                  .build());
            }

            registerManagedViewType(typeRegistry, typeNameToClass, managedView, nodeType);
            registerManagedViewType(typeRegistry, typeNameToClass, managedView, edgeType);
            registerManagedViewType(typeRegistry, typeNameToClass, managedView, connectionType);
        }
    }

    protected void registerManagedViewType(TypeDefinitionRegistry typeRegistry, Map<String, Class<?>> typeNameToClass, ManagedViewType<?> managedView, ObjectTypeDefinition objectTypeDefinition) {
        typeRegistry.add(objectTypeDefinition);
        Class<?> old;
        if ((old = typeNameToClass.put(objectTypeDefinition.getName(), managedView.getJavaType())) != null) {
            throw new IllegalArgumentException("Type with name '" + objectTypeDefinition.getName() + "' is registered multiple times: [" + old.getName() + ", " + managedView.getJavaType().getName() + "]!");
        }
    }

    /**
     * Return the GraphQL id type for the given singular attribute.
     *
     * @param typeRegistry The type registry
     * @param singularAttribute The singular attribute
     * @return The type
     */
    protected Type getIdType(TypeDefinitionRegistry typeRegistry, SingularAttribute<?, ?> singularAttribute) {
        return new NonNullType(new TypeName("ID"));
    }

    /**
     * Return the GraphQL entry type for the given map attribute with the given key and value types.
     *
     * @param typeRegistry The type registry
     * @param attribute The map attribute
     * @param key The key type
     * @param value The value type
     * @return The type
     */
    protected Type getEntryType(TypeDefinitionRegistry typeRegistry, MethodAttribute<?, ?> attribute, Type key, Type value) {
        String entryName = getObjectTypeName(attribute.getDeclaringType()) + StringUtils.firstToLower(attribute.getName()) + "Entry";
        List<FieldDefinition> fields = new ArrayList<>();
        fields.add(new FieldDefinition("key", key));
        fields.add(new FieldDefinition("value", value));
        typeRegistry.add(ObjectTypeDefinition.newObjectTypeDefinition()
            .name(entryName)
            .fieldDefinitions(fields)
            .build()
        );
        return new ListType(new TypeName(entryName));
    }

    /**
     * Returns the GraphQL type name for the given managed view type.
     *
     * @param type The managed view type
     * @return The GraphQL type name
     */
    protected String getObjectTypeName(ManagedViewType type) {
        return type.getJavaType().getSimpleName();
    }

    /**
     * Returns the GraphQL type name for the given java type.
     *
     * @param type The java type
     * @return The GraphQL type name
     */
    protected String getTypeName(Class<?> type) {
        return type.getSimpleName();
    }

    /**
     * Return the GraphQL type for the given managed view type.
     *
     * @param type The managed view type
     * @return The type
     */
    protected Type getObjectType(ManagedViewType type) {
        return new TypeName(getObjectTypeName(type));
    }

    /**
     * Return the GraphQL type for the given singular attribute.
     *
     * @param typeRegistry The type registry
     * @param singularAttribute The singular attribute
     * @return The type
     */
    protected Type getElementType(TypeDefinitionRegistry typeRegistry, SingularAttribute<?, ?> singularAttribute) {
        com.blazebit.persistence.view.metamodel.Type elementType = singularAttribute.getType();
        if (elementType.getMappingType() == com.blazebit.persistence.view.metamodel.Type.MappingType.BASIC) {
            return getScalarType(typeRegistry, elementType.getJavaType());
        } else {
            return getObjectType((ManagedViewType<?>) elementType);
        }
    }

    /**
     * Return the GraphQL type for the given plural attribute.
     *
     * @param typeRegistry The type registry
     * @param pluralAttribute The plural attribute
     * @return The type
     */
    protected Type getElementType(TypeDefinitionRegistry typeRegistry, PluralAttribute<?, ?, ?> pluralAttribute) {
        com.blazebit.persistence.view.metamodel.Type elementType = pluralAttribute.getElementType();
        if (elementType.getMappingType() == com.blazebit.persistence.view.metamodel.Type.MappingType.BASIC) {
            return getScalarType(typeRegistry, elementType.getJavaType());
        } else {
            return getObjectType((ManagedViewType<?>) elementType);
        }
    }

    /**
     * Return the GraphQL type for the key of the given map attribute.
     *
     * @param typeRegistry The type registry
     * @param mapAttribute The map attribute
     * @return The type
     */
    protected Type getKeyType(TypeDefinitionRegistry typeRegistry, MapAttribute<?, ?, ?> mapAttribute) {
        com.blazebit.persistence.view.metamodel.Type elementType = mapAttribute.getKeyType();
        if (elementType.getMappingType() == com.blazebit.persistence.view.metamodel.Type.MappingType.BASIC) {
            return getScalarType(typeRegistry, elementType.getJavaType());
        } else {
            return getObjectType((ManagedViewType<?>) elementType);
        }
    }

    /**
     * Return the GraphQL type for the given scalar java type.
     *
     * @param typeRegistry The type registry
     * @param javaType The java type
     * @return The type
     */
    protected Type getScalarType(TypeDefinitionRegistry typeRegistry, Class<?> javaType) {
        String typeName = TYPES.get(javaType);
        if (typeName == null) {
            if (javaType.isEnum()) {
                typeName = getTypeName(javaType);
                if (!typeRegistry.getType(typeName).isPresent()) {
                    List<EnumValueDefinition> enumValueDefinitions = new ArrayList<>();
                    for (Enum<?> enumConstant : (Enum<?>[]) javaType.getEnumConstants()) {
                        enumValueDefinitions.add(new EnumValueDefinition(enumConstant.name(), new ArrayList<>(0)));
                    }

                    typeRegistry.add(EnumTypeDefinition.newEnumTypeDefinition()
                        .name(typeName)
                        .enumValueDefinitions(enumValueDefinitions)
                      .build()
                    );
                }
            } else {
                typeName = "String";
            }
        }
        return new TypeName(typeName);
    }
}
