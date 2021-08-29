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
import graphql.language.Definition;
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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
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
    private static final Method TYPE_REGISTRY_ADD;
    private static final Constructor<ObjectTypeDefinition> OBJECT_TYPE_DEFINITION_CONSTRUCTOR;
    private static final Method OBJECT_TYPE_DEFINITION_NEW_BUILDER;
    private static final Method OBJECT_TYPE_DEFINITION_BUILDER_NAME;
    private static final Method OBJECT_TYPE_DEFINITION_BUILDER_FIELD_DEFINITIONS;
    private static final Method OBJECT_TYPE_DEFINITION_BUILDER_BUILD;
    private static final Constructor<InterfaceTypeDefinition> INTERFACE_TYPE_DEFINITION_CONSTRUCTOR;
    private static final Method INTERFACE_TYPE_DEFINITION_NEW_BUILDER;
    private static final Method INTERFACE_TYPE_DEFINITION_BUILDER_NAME;
    private static final Method INTERFACE_TYPE_DEFINITION_BUILDER_FIELD_DEFINITIONS;
    private static final Method INTERFACE_TYPE_DEFINITION_BUILDER_BUILD;
    private static final Constructor<EnumTypeDefinition> ENUM_TYPE_DEFINITION_CONSTRUCTOR;
    private static final Method ENUM_TYPE_DEFINITION_NEW_BUILDER;
    private static final Method ENUM_TYPE_DEFINITION_BUILDER_NAME;
    private static final Method ENUM_TYPE_DEFINITION_BUILDER_FIELD_DEFINITIONS;
    private static final Method ENUM_TYPE_DEFINITION_BUILDER_BUILD;

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

        Method typeRegistryAdd = null;
        try {
            typeRegistryAdd = TypeDefinitionRegistry.class.getMethod("add", Class.forName("graphql.language.SDLDefinition"));
        } catch (Exception ex) {
            try {
                typeRegistryAdd = TypeDefinitionRegistry.class.getMethod("add", Class.forName("graphql.language.Definition"));
            } catch (Exception ex2) {
                RuntimeException runtimeException = new RuntimeException("Could not initialize accessors for graphql-java", ex2);
                runtimeException.addSuppressed(ex);
                throw runtimeException;
            }
        }
        TYPE_REGISTRY_ADD = typeRegistryAdd;
        OBJECT_TYPE_DEFINITION_CONSTRUCTOR = getConstructor(ObjectTypeDefinition.class, String.class, List.class, List.class, List.class);
        INTERFACE_TYPE_DEFINITION_CONSTRUCTOR = getConstructor(InterfaceTypeDefinition.class, String.class, List.class, List.class);
        ENUM_TYPE_DEFINITION_CONSTRUCTOR = getConstructor(EnumTypeDefinition.class, String.class, List.class, List.class);
        Method objectTypeDefinitionNewBuilder = null;
        Method objectTypeDefinitionBuilderName = null;
        Method objectTypeDefinitionBuilderFieldDefinitions = null;
        Method objectTypeDefinitionBuilderBuild = null;
        Method interfaceTypeDefinitionNewBuilder = null;
        Method interfaceTypeDefinitionBuilderName = null;
        Method interfaceTypeDefinitionBuilderFieldDefinitions = null;
        Method interfaceTypeDefinitionBuilderBuild = null;
        Method enumTypeDefinitionNewBuilder = null;
        Method enumTypeDefinitionBuilderName = null;
        Method enumTypeDefinitionBuilderFieldDefinitions = null;
        Method enumTypeDefinitionBuilderBuild = null;
        if (OBJECT_TYPE_DEFINITION_CONSTRUCTOR == null) {
            try {
                objectTypeDefinitionNewBuilder = ObjectTypeDefinition.class.getMethod("newObjectTypeDefinition");
                objectTypeDefinitionBuilderName = Class.forName("graphql.language.ObjectTypeDefinition$Builder").getMethod("name", String.class);
                objectTypeDefinitionBuilderFieldDefinitions = Class.forName("graphql.language.ObjectTypeDefinition$Builder").getMethod("fieldDefinitions", List.class);
                objectTypeDefinitionBuilderBuild = Class.forName("graphql.language.ObjectTypeDefinition$Builder").getMethod("build");

                interfaceTypeDefinitionNewBuilder = InterfaceTypeDefinition.class.getMethod("newInterfaceTypeDefinition");
                interfaceTypeDefinitionBuilderName = Class.forName("graphql.language.InterfaceTypeDefinition$Builder").getMethod("name", String.class);
                interfaceTypeDefinitionBuilderFieldDefinitions = Class.forName("graphql.language.InterfaceTypeDefinition$Builder").getMethod("definitions", List.class);
                interfaceTypeDefinitionBuilderBuild = Class.forName("graphql.language.InterfaceTypeDefinition$Builder").getMethod("build");

                enumTypeDefinitionNewBuilder = EnumTypeDefinition.class.getMethod("newEnumTypeDefinition");
                enumTypeDefinitionBuilderName = Class.forName("graphql.language.EnumTypeDefinition$Builder").getMethod("name", String.class);
                enumTypeDefinitionBuilderFieldDefinitions = Class.forName("graphql.language.EnumTypeDefinition$Builder").getMethod("enumValueDefinitions", List.class);
                enumTypeDefinitionBuilderBuild = Class.forName("graphql.language.EnumTypeDefinition$Builder").getMethod("build");
            } catch (Exception ex) {
                throw new RuntimeException("Could not initialize accessors for graphql-java", ex);
            }
        }
        OBJECT_TYPE_DEFINITION_NEW_BUILDER = objectTypeDefinitionNewBuilder;
        OBJECT_TYPE_DEFINITION_BUILDER_NAME = objectTypeDefinitionBuilderName;
        OBJECT_TYPE_DEFINITION_BUILDER_FIELD_DEFINITIONS = objectTypeDefinitionBuilderFieldDefinitions;
        OBJECT_TYPE_DEFINITION_BUILDER_BUILD = objectTypeDefinitionBuilderBuild;
        INTERFACE_TYPE_DEFINITION_NEW_BUILDER = interfaceTypeDefinitionNewBuilder;
        INTERFACE_TYPE_DEFINITION_BUILDER_NAME = interfaceTypeDefinitionBuilderName;
        INTERFACE_TYPE_DEFINITION_BUILDER_FIELD_DEFINITIONS = interfaceTypeDefinitionBuilderFieldDefinitions;
        INTERFACE_TYPE_DEFINITION_BUILDER_BUILD = interfaceTypeDefinitionBuilderBuild;
        ENUM_TYPE_DEFINITION_NEW_BUILDER = enumTypeDefinitionNewBuilder;
        ENUM_TYPE_DEFINITION_BUILDER_NAME = enumTypeDefinitionBuilderName;
        ENUM_TYPE_DEFINITION_BUILDER_FIELD_DEFINITIONS = enumTypeDefinitionBuilderFieldDefinitions;
        ENUM_TYPE_DEFINITION_BUILDER_BUILD = enumTypeDefinitionBuilderBuild;
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

    private static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes) {
        try {
            return clazz.getConstructor(String.class, List.class, List.class, List.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
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
            addObjectTypeDefinition(typeRegistry, typeNameToClass, managedView, newObjectTypeDefinition(typeName, fieldDefinitions));
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

    protected ObjectTypeDefinition newObjectTypeDefinition(String typeName, List<FieldDefinition> fieldDefinitions) {
        try {
            if (OBJECT_TYPE_DEFINITION_CONSTRUCTOR != null) {
//                new ObjectTypeDefinition(typeName, implementsTypes, directives, fieldDefinitions);
                return OBJECT_TYPE_DEFINITION_CONSTRUCTOR.newInstance(typeName, new ArrayList<>(0), new ArrayList<>(0), fieldDefinitions);
            } else {
//                ObjectTypeDefinition.newObjectTypeDefinition()
//                        .name(typeName)
//                        .fieldDefinitions(fieldDefinitions)
//                        .build()
                Object newObjectTypeDefinitionBuilder = OBJECT_TYPE_DEFINITION_NEW_BUILDER.invoke(null);
                OBJECT_TYPE_DEFINITION_BUILDER_NAME.invoke(newObjectTypeDefinitionBuilder, typeName);
                OBJECT_TYPE_DEFINITION_BUILDER_FIELD_DEFINITIONS.invoke(newObjectTypeDefinitionBuilder, fieldDefinitions);
                return (ObjectTypeDefinition) OBJECT_TYPE_DEFINITION_BUILDER_BUILD.invoke(newObjectTypeDefinitionBuilder);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not build object type definition", e);
        }
    }

    protected InterfaceTypeDefinition newInterfaceTypeDefinition(String name, List<FieldDefinition> fieldDefinitions) {
        try {
            if (INTERFACE_TYPE_DEFINITION_CONSTRUCTOR != null) {
//                return new InterfaceTypeDefinition(name, fieldDefinitions, new ArrayList<>(0));
                return INTERFACE_TYPE_DEFINITION_CONSTRUCTOR.newInstance(name, fieldDefinitions, new ArrayList<>(0));
            } else {
//                InterfaceTypeDefinition.newInterfaceTypeDefinition().name(name).definitions(fieldDefinitions).build()
                Object newInterfaceTypeDefinitionBuilder = INTERFACE_TYPE_DEFINITION_NEW_BUILDER.invoke(null);
                INTERFACE_TYPE_DEFINITION_BUILDER_NAME.invoke(newInterfaceTypeDefinitionBuilder, name);
                INTERFACE_TYPE_DEFINITION_BUILDER_FIELD_DEFINITIONS.invoke(newInterfaceTypeDefinitionBuilder, fieldDefinitions);
                return (InterfaceTypeDefinition) INTERFACE_TYPE_DEFINITION_BUILDER_BUILD.invoke(newInterfaceTypeDefinitionBuilder);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not build object type definition", e);
        }
    }

    protected EnumTypeDefinition newEnumTypeDefinition(String typeName, List<EnumValueDefinition> enumValueDefinitions) {
        try {
            if (ENUM_TYPE_DEFINITION_CONSTRUCTOR != null) {
//                return new EnumTypeDefinition(typeName, enumValueDefinitions, new ArrayList<>(0));
                return ENUM_TYPE_DEFINITION_CONSTRUCTOR.newInstance(typeName, enumValueDefinitions, new ArrayList<>(0));
            } else {
//                EnumTypeDefinition.newEnumTypeDefinition()
//                        .name(typeName)
//                        .enumValueDefinitions(enumValueDefinitions)
//                        .build()
                Object newEnumTypeDefinitionBuilder = ENUM_TYPE_DEFINITION_NEW_BUILDER.invoke(null);
                ENUM_TYPE_DEFINITION_BUILDER_NAME.invoke(newEnumTypeDefinitionBuilder, typeName);
                ENUM_TYPE_DEFINITION_BUILDER_FIELD_DEFINITIONS.invoke(newEnumTypeDefinitionBuilder, enumValueDefinitions);
                return (EnumTypeDefinition) ENUM_TYPE_DEFINITION_BUILDER_BUILD.invoke(newEnumTypeDefinitionBuilder);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not build object type definition", e);
        }
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
            ObjectTypeDefinition nodeType = newObjectTypeDefinition(objectTypeDefinition.getName() + "Node", objectTypeDefinition.getFieldDefinitions());

            if (!typeRegistry.getType("Node").isPresent() && isImplementRelayNode() && isDefineRelayNodeIfNotExist()) {
                List<FieldDefinition> nodeFields = new ArrayList<>(4);
                nodeFields.add(new FieldDefinition("id", new NonNullType(new TypeName("ID"))));
                addDefinition(typeRegistry, newInterfaceTypeDefinition("Node", nodeFields));
            }

            List<FieldDefinition> edgeFields = new ArrayList<>(2);
            edgeFields.add(new FieldDefinition("node", new NonNullType(new TypeName(nodeType.getName()))));
            edgeFields.add(new FieldDefinition("cursor", new NonNullType(new TypeName("String"))));
            ObjectTypeDefinition edgeType = newObjectTypeDefinition(objectTypeDefinition.getName() + "Edge", edgeFields);

            List<FieldDefinition> connectionFields = new ArrayList<>(2);
            connectionFields.add(new FieldDefinition("edges", new ListType(new TypeName(edgeType.getName()))));
            connectionFields.add(new FieldDefinition("pageInfo", new NonNullType(new TypeName("PageInfo"))));
            connectionFields.add(new FieldDefinition("totalCount", new NonNullType(new TypeName("Int"))));
            ObjectTypeDefinition connectionType = newObjectTypeDefinition(objectTypeDefinition.getName() + "Connection", connectionFields);

            if (!typeRegistry.getType("PageInfo").isPresent() && isDefineRelayNodeIfNotExist()) {
                List<FieldDefinition> pageInfoFields = new ArrayList<>(4);
                pageInfoFields.add(new FieldDefinition("hasNextPage", new NonNullType(new TypeName("Boolean"))));
                pageInfoFields.add(new FieldDefinition("hasPreviousPage", new NonNullType(new TypeName("Boolean"))));
                pageInfoFields.add(new FieldDefinition("startCursor", new TypeName("String")));
                pageInfoFields.add(new FieldDefinition("endCursor", new TypeName("String")));
                addDefinition(typeRegistry, newObjectTypeDefinition("PageInfo", pageInfoFields));
            }

            registerManagedViewType(typeRegistry, typeNameToClass, managedView, nodeType);
            registerManagedViewType(typeRegistry, typeNameToClass, managedView, edgeType);
            registerManagedViewType(typeRegistry, typeNameToClass, managedView, connectionType);
        }
    }

    protected void addDefinition(TypeDefinitionRegistry typeRegistry, Definition<?> definition) {
        try {
            TYPE_REGISTRY_ADD.invoke(typeRegistry, definition);
        } catch (Exception e) {
            throw new RuntimeException("Could not add definition", e);
        }
    }

    protected void registerManagedViewType(TypeDefinitionRegistry typeRegistry, Map<String, Class<?>> typeNameToClass, ManagedViewType<?> managedView, ObjectTypeDefinition objectTypeDefinition) {
        addDefinition(typeRegistry, objectTypeDefinition);
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
        addDefinition(typeRegistry, newObjectTypeDefinition(entryName, fields));
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

                    addDefinition(typeRegistry, newEnumTypeDefinition(typeName, enumValueDefinitions));
                }
            } else {
                typeName = "String";
            }
        }
        return new TypeName(typeName);
    }
}
