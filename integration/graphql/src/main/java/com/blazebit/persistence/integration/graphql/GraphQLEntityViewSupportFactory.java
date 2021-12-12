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
import com.blazebit.persistence.impl.ExpressionUtils;
import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.impl.metamodel.AbstractAttribute;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.MappingAttribute;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.reflection.ReflectionUtils;
import graphql.language.Definition;
import graphql.language.Description;
import graphql.language.EnumTypeDefinition;
import graphql.language.EnumValueDefinition;
import graphql.language.FieldDefinition;
import graphql.language.InputObjectTypeDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.InterfaceTypeDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.ObjectTypeDefinition;
import graphql.language.Type;
import graphql.language.TypeDefinition;
import graphql.language.TypeName;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A factory for creating a support class for using entity views in a GraphQL environment.
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class GraphQLEntityViewSupportFactory {

    private static final Map<Class<?>, String> TYPES;
    private static final Map<Class<?>, String> MP_TYPES;
    private static final Method TYPE_REGISTRY_ADD;
    private static final Constructor<ObjectTypeDefinition> OBJECT_TYPE_DEFINITION_CONSTRUCTOR;
    private static final Method OBJECT_TYPE_DEFINITION_NEW_BUILDER;
    private static final Method OBJECT_TYPE_DEFINITION_SET_DESCRIPTION;
    private static final Method OBJECT_TYPE_DEFINITION_BUILDER_NAME;
    private static final Method OBJECT_TYPE_DEFINITION_BUILDER_DESCRIPTION;
    private static final Method OBJECT_TYPE_DEFINITION_BUILDER_IMPLEMENTS;
    private static final Method OBJECT_TYPE_DEFINITION_BUILDER_FIELD_DEFINITIONS;
    private static final Method OBJECT_TYPE_DEFINITION_BUILDER_BUILD;
    private static final Constructor<InputObjectTypeDefinition> INPUT_OBJECT_TYPE_DEFINITION_CONSTRUCTOR;
    private static final Method INPUT_OBJECT_TYPE_DEFINITION_NEW_BUILDER;
    private static final Method INPUT_OBJECT_TYPE_DEFINITION_SET_DESCRIPTION;
    private static final Method INPUT_OBJECT_TYPE_DEFINITION_BUILDER_NAME;
    private static final Method INPUT_OBJECT_TYPE_DEFINITION_BUILDER_DESCRIPTION;
    private static final Method INPUT_OBJECT_TYPE_DEFINITION_BUILDER_VALUE_DEFINITIONS;
    private static final Method INPUT_OBJECT_TYPE_DEFINITION_BUILDER_BUILD;
    private static final Constructor<InterfaceTypeDefinition> INTERFACE_TYPE_DEFINITION_CONSTRUCTOR;
    private static final Method INTERFACE_TYPE_DEFINITION_NEW_BUILDER;
    private static final Method INTERFACE_TYPE_DEFINITION_SET_DESCRIPTION;
    private static final Method INTERFACE_TYPE_DEFINITION_BUILDER_NAME;
    private static final Method INTERFACE_TYPE_DEFINITION_BUILDER_DESCRIPTION;
    private static final Method INTERFACE_TYPE_DEFINITION_BUILDER_FIELD_DEFINITIONS;
    private static final Method INTERFACE_TYPE_DEFINITION_BUILDER_BUILD;
    private static final Constructor<EnumTypeDefinition> ENUM_TYPE_DEFINITION_CONSTRUCTOR;
    private static final Method ENUM_TYPE_DEFINITION_NEW_BUILDER;
    private static final Method ENUM_TYPE_DEFINITION_SET_DESCRIPTION;
    private static final Method ENUM_TYPE_DEFINITION_BUILDER_NAME;
    private static final Method ENUM_TYPE_DEFINITION_BUILDER_DESCRIPTION;
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
        Map<Class<?>, String> mpTypes = new HashMap<>();
        mpTypes.put(boolean.class, "Boolean");
        mpTypes.put(Boolean.class, "Boolean");
        mpTypes.put(byte.class, "Int");
        mpTypes.put(Byte.class, "Int");
        mpTypes.put(short.class, "Int");
        mpTypes.put(Short.class, "Int");
        mpTypes.put(int.class, "Int");
        mpTypes.put(Integer.class, "Int");
        mpTypes.put(long.class, "BigInteger");
        mpTypes.put(Long.class, "BigInteger");
        mpTypes.put(float.class, "Float");
        mpTypes.put(Float.class, "Float");
        mpTypes.put(double.class, "Float");
        mpTypes.put(Double.class, "Float");
        mpTypes.put(BigInteger.class, "BigInteger");
        mpTypes.put(BigDecimal.class, "BigDecimal");
        mpTypes.put(char.class, "String");
        mpTypes.put(Character.class, "String");
        mpTypes.put(String.class, "String");
        mpTypes.put(LocalDate.class, "Date");
        mpTypes.put(LocalTime.class, "Time");
        mpTypes.put(OffsetTime.class, "Time");
        mpTypes.put(LocalDateTime.class, "DateTime");
        mpTypes.put(OffsetDateTime.class, "DateTime");
        mpTypes.put(ZonedDateTime.class, "DateTime");
        MP_TYPES = mpTypes;

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
        INPUT_OBJECT_TYPE_DEFINITION_CONSTRUCTOR = getConstructor(InputObjectTypeDefinition.class, String.class, List.class, List.class);
        INTERFACE_TYPE_DEFINITION_CONSTRUCTOR = getConstructor(InterfaceTypeDefinition.class, String.class, List.class, List.class);
        ENUM_TYPE_DEFINITION_CONSTRUCTOR = getConstructor(EnumTypeDefinition.class, String.class, List.class, List.class);
        Method objectTypeDefinitionNewBuilder = null;
        Method objectTypeDefinitionSetDescription = null;
        Method objectTypeDefinitionBuilderName = null;
        Method objectTypeDefinitionBuilderDescription = null;
        Method objectTypeDefinitionBuilderImplements = null;
        Method objectTypeDefinitionBuilderFieldDefinitions = null;
        Method objectTypeDefinitionBuilderBuild = null;
        Method inputObjectTypeDefinitionNewBuilder = null;
        Method inputObjectTypeDefinitionSetDescription = null;
        Method inputObjectTypeDefinitionBuilderName = null;
        Method inputObjectTypeDefinitionBuilderDescription = null;
        Method inputObjectTypeDefinitionBuilderValueDefinitions = null;
        Method inputObjectTypeDefinitionBuilderBuild = null;
        Method interfaceTypeDefinitionNewBuilder = null;
        Method interfaceTypeDefinitionSetDescription = null;
        Method interfaceTypeDefinitionBuilderName = null;
        Method interfaceTypeDefinitionBuilderDescription = null;
        Method interfaceTypeDefinitionBuilderFieldDefinitions = null;
        Method interfaceTypeDefinitionBuilderBuild = null;
        Method enumTypeDefinitionNewBuilder = null;
        Method enumTypeDefinitionSetDescription = null;
        Method enumTypeDefinitionBuilderName = null;
        Method enumTypeDefinitionBuilderDescription = null;
        Method enumTypeDefinitionBuilderFieldDefinitions = null;
        Method enumTypeDefinitionBuilderBuild = null;
        if (OBJECT_TYPE_DEFINITION_CONSTRUCTOR == null) {
            try {
                objectTypeDefinitionNewBuilder = ObjectTypeDefinition.class.getMethod("newObjectTypeDefinition");
                objectTypeDefinitionBuilderName = Class.forName("graphql.language.ObjectTypeDefinition$Builder").getMethod("name", String.class);
                objectTypeDefinitionBuilderDescription = Class.forName("graphql.language.ObjectTypeDefinition$Builder").getMethod("description", Description.class);
                objectTypeDefinitionBuilderImplements = Class.forName("graphql.language.ObjectTypeDefinition$Builder").getMethod("implementz", List.class);
                objectTypeDefinitionBuilderFieldDefinitions = Class.forName("graphql.language.ObjectTypeDefinition$Builder").getMethod("fieldDefinitions", List.class);
                objectTypeDefinitionBuilderBuild = Class.forName("graphql.language.ObjectTypeDefinition$Builder").getMethod("build");

                inputObjectTypeDefinitionNewBuilder = InputObjectTypeDefinition.class.getMethod("newInputObjectDefinition");
                inputObjectTypeDefinitionBuilderName = Class.forName("graphql.language.InputObjectTypeDefinition$Builder").getMethod("name", String.class);
                inputObjectTypeDefinitionBuilderDescription = Class.forName("graphql.language.InputObjectTypeDefinition$Builder").getMethod("description", Description.class);
                inputObjectTypeDefinitionBuilderValueDefinitions = Class.forName("graphql.language.InputObjectTypeDefinition$Builder").getMethod("inputValueDefinitions", List.class);
                inputObjectTypeDefinitionBuilderBuild = Class.forName("graphql.language.InputObjectTypeDefinition$Builder").getMethod("build");

                interfaceTypeDefinitionNewBuilder = InterfaceTypeDefinition.class.getMethod("newInterfaceTypeDefinition");
                interfaceTypeDefinitionBuilderName = Class.forName("graphql.language.InterfaceTypeDefinition$Builder").getMethod("name", String.class);
                interfaceTypeDefinitionBuilderDescription = Class.forName("graphql.language.InterfaceTypeDefinition$Builder").getMethod("description", Description.class);
                interfaceTypeDefinitionBuilderFieldDefinitions = Class.forName("graphql.language.InterfaceTypeDefinition$Builder").getMethod("definitions", List.class);
                interfaceTypeDefinitionBuilderBuild = Class.forName("graphql.language.InterfaceTypeDefinition$Builder").getMethod("build");

                enumTypeDefinitionNewBuilder = EnumTypeDefinition.class.getMethod("newEnumTypeDefinition");
                enumTypeDefinitionBuilderName = Class.forName("graphql.language.EnumTypeDefinition$Builder").getMethod("name", String.class);
                enumTypeDefinitionBuilderDescription = Class.forName("graphql.language.EnumTypeDefinition$Builder").getMethod("description", Description.class);
                enumTypeDefinitionBuilderFieldDefinitions = Class.forName("graphql.language.EnumTypeDefinition$Builder").getMethod("enumValueDefinitions", List.class);
                enumTypeDefinitionBuilderBuild = Class.forName("graphql.language.EnumTypeDefinition$Builder").getMethod("build");
            } catch (Exception ex) {
                throw new RuntimeException("Could not initialize accessors for graphql-java", ex);
            }
        } else {
            try {
                objectTypeDefinitionSetDescription = ObjectTypeDefinition.class.getMethod("setDescription", Description.class);
                inputObjectTypeDefinitionSetDescription = InputObjectTypeDefinition.class.getMethod("setDescription", Description.class);
                interfaceTypeDefinitionSetDescription = InterfaceTypeDefinition.class.getMethod("setDescription", Description.class);
                enumTypeDefinitionSetDescription = EnumTypeDefinition.class.getMethod("setDescription", Description.class);
            } catch (Exception ex) {
                throw new RuntimeException("Could not initialize accessors for graphql-java", ex);
            }
        }
        OBJECT_TYPE_DEFINITION_NEW_BUILDER = objectTypeDefinitionNewBuilder;
        OBJECT_TYPE_DEFINITION_SET_DESCRIPTION = objectTypeDefinitionSetDescription;
        OBJECT_TYPE_DEFINITION_BUILDER_NAME = objectTypeDefinitionBuilderName;
        OBJECT_TYPE_DEFINITION_BUILDER_DESCRIPTION = objectTypeDefinitionBuilderDescription;
        OBJECT_TYPE_DEFINITION_BUILDER_IMPLEMENTS = objectTypeDefinitionBuilderImplements;
        OBJECT_TYPE_DEFINITION_BUILDER_FIELD_DEFINITIONS = objectTypeDefinitionBuilderFieldDefinitions;
        OBJECT_TYPE_DEFINITION_BUILDER_BUILD = objectTypeDefinitionBuilderBuild;
        INPUT_OBJECT_TYPE_DEFINITION_NEW_BUILDER = inputObjectTypeDefinitionNewBuilder;
        INPUT_OBJECT_TYPE_DEFINITION_SET_DESCRIPTION = inputObjectTypeDefinitionSetDescription;
        INPUT_OBJECT_TYPE_DEFINITION_BUILDER_NAME = inputObjectTypeDefinitionBuilderName;
        INPUT_OBJECT_TYPE_DEFINITION_BUILDER_DESCRIPTION = inputObjectTypeDefinitionBuilderDescription;
        INPUT_OBJECT_TYPE_DEFINITION_BUILDER_VALUE_DEFINITIONS = inputObjectTypeDefinitionBuilderValueDefinitions;
        INPUT_OBJECT_TYPE_DEFINITION_BUILDER_BUILD = inputObjectTypeDefinitionBuilderBuild;
        INTERFACE_TYPE_DEFINITION_NEW_BUILDER = interfaceTypeDefinitionNewBuilder;
        INTERFACE_TYPE_DEFINITION_SET_DESCRIPTION = interfaceTypeDefinitionSetDescription;
        INTERFACE_TYPE_DEFINITION_BUILDER_NAME = interfaceTypeDefinitionBuilderName;
        INTERFACE_TYPE_DEFINITION_BUILDER_DESCRIPTION = interfaceTypeDefinitionBuilderDescription;
        INTERFACE_TYPE_DEFINITION_BUILDER_FIELD_DEFINITIONS = interfaceTypeDefinitionBuilderFieldDefinitions;
        INTERFACE_TYPE_DEFINITION_BUILDER_BUILD = interfaceTypeDefinitionBuilderBuild;
        ENUM_TYPE_DEFINITION_NEW_BUILDER = enumTypeDefinitionNewBuilder;
        ENUM_TYPE_DEFINITION_SET_DESCRIPTION = enumTypeDefinitionSetDescription;
        ENUM_TYPE_DEFINITION_BUILDER_NAME = enumTypeDefinitionBuilderName;
        ENUM_TYPE_DEFINITION_BUILDER_DESCRIPTION = enumTypeDefinitionBuilderDescription;
        ENUM_TYPE_DEFINITION_BUILDER_FIELD_DEFINITIONS = enumTypeDefinitionBuilderFieldDefinitions;
        ENUM_TYPE_DEFINITION_BUILDER_BUILD = enumTypeDefinitionBuilderBuild;
    }

    private boolean defineNormalTypes;
    private boolean defineRelayTypes;
    private Boolean implementRelayNode;
    private boolean defineRelayNodeIfNotExist = false;
    private Pattern typeFilterPattern;
    private Map<String, GraphQLScalarType> scalarTypeMap;

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
     * Returns the scalar types as map with the type name as key.
     *
     * @return the scalar types as map with the type name as key
     * @since 1.6.2
     */
    public Map<String, GraphQLScalarType> getScalarTypeMap() {
        return scalarTypeMap;
    }

    /**
     * Sets the scalar types as map with the type name as key.
     *
     * @param scalarTypeMap the scalar type map
     * @since 1.6.2
     */
    public void setScalarTypeMap(Map<String, GraphQLScalarType> scalarTypeMap) {
        this.scalarTypeMap = scalarTypeMap;
    }

    /**
     * Returns the type filter pattern to use during {@code GraphQLEntityViewSupportFactory.create}.
     *
     * @return the type filter pattern
     * @since 1.6.3
     */
    public Pattern getTypeFilterPattern() {
        return typeFilterPattern;
    }

    /**
     * Sets the type filter pattern to use during {@code GraphQLEntityViewSupportFactory.create}.
     *
     * @param typeFilterPattern the type filter pattern
     * @since 1.6.3
     */
    public void setTypeFilterPattern(Pattern typeFilterPattern) {
        this.typeFilterPattern = typeFilterPattern;
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
        EntityMetamodel entityMetamodel = entityViewManager.getService(EntityMetamodel.class);
        Map<String, Class<?>> typeNameToClass = new HashMap<>();
        Map<String, Map<String, String>> typeNameToFieldMapping = new HashMap<>();
        for (ManagedViewType<?> managedView : entityViewManager.getMetamodel().getManagedViews()) {
            if (typeFilterPattern != null && !typeFilterPattern.matcher(managedView.getJavaType().getName()).matches()) {
                continue;
            }
            String typeName = getObjectTypeName(managedView);
            String inputTypeName = typeName + "Input";
            String description = getDescription(managedView.getJavaType());
            List<FieldDefinition> fieldDefinitions = new ArrayList<>(managedView.getAttributes().size());
            List<InputValueDefinition> valueDefinitions = new ArrayList<>(managedView.getAttributes().size());
            for (MethodAttribute<?, ?> attribute : managedView.getAttributes()) {
                if (isIgnored(attribute.getJavaMethod())) {
                    continue;
                }
                Type type;
                Type inputType;
                if (attribute instanceof SingularAttribute<?, ?>) {
                    SingularAttribute<?, ?> singularAttribute = (SingularAttribute<?, ?>) attribute;
                    if (singularAttribute.isId() && !singularAttribute.isSubview()) {
                        // Usual numeric ID
                        type = getIdType(typeRegistry, singularAttribute);
                        inputType = getInputIdType(typeRegistry, singularAttribute);
                    } else {
                        type = getElementType(typeRegistry, singularAttribute, entityMetamodel);
                        inputType = getInputElementType(typeRegistry, singularAttribute, entityMetamodel);
                    }
                } else if (attribute instanceof MapAttribute<?, ?, ?>) {
                    MapAttribute<?, ?, ?> mapAttribute = (MapAttribute<?, ?, ?>) attribute;
                    type = getEntryType(typeRegistry, attribute, getKeyType(typeRegistry, mapAttribute), getElementType(typeRegistry, mapAttribute));
                    inputType = getInputEntryType(typeRegistry, attribute, getInputKeyType(typeRegistry, mapAttribute), getInputElementType(typeRegistry, mapAttribute));
                } else {
                    type = new ListType(getElementType(typeRegistry, (PluralAttribute<?, ?, ?>) attribute));
                    inputType = new ListType(getInputElementType(typeRegistry, (PluralAttribute<?, ?, ?>) attribute));
                }
                String fieldName = getFieldName(attribute);
                FieldDefinition fieldDefinition = new FieldDefinition(fieldName, type);
                fieldDefinitions.add(fieldDefinition);
                addFieldMapping(typeNameToFieldMapping, typeName, attribute, fieldName);
                valueDefinitions.add(new InputValueDefinition(fieldName, inputType));
                addFieldMapping(typeNameToFieldMapping, inputTypeName, attribute, fieldName);
            }
            addObjectTypeDefinition(typeRegistry, typeNameToClass, managedView, newObjectTypeDefinition(typeName, fieldDefinitions, description), newInputObjectTypeDefinition(inputTypeName, valueDefinitions, description));
        }

        Set<String> serializableBasicTypes = new HashSet<>();
        for (javax.persistence.metamodel.Type<?> basicType : entityMetamodel.getBasicTypes()) {
            for (Class<?> superType : ReflectionUtils.getSuperTypes(basicType.getJavaType())) {
                serializableBasicTypes.add(superType.getName());
            }

            serializableBasicTypes.add(basicType.getJavaType().getName());
        }

        serializableBasicTypes.add(Serializable[].class.getName());
        serializableBasicTypes.add(GraphQLCursor.class.getName());
        return new GraphQLEntityViewSupport(typeNameToClass, typeNameToFieldMapping, serializableBasicTypes);
    }

    /**
     * Returns a new {@link GraphQLEntityViewSupport} after initializing entity view types from {@link EntityViewManager}
     * that are available in the given schema.
     *
     * @param schema The existing schema
     * @param entityViewManager The entity view manager
     * @return a new {@link GraphQLEntityViewSupport}
     */
    public GraphQLEntityViewSupport create(GraphQLSchema schema, EntityViewManager entityViewManager) {
        boolean defineNormalTypes = this.defineNormalTypes;
        boolean defineRelayTypes = this.defineRelayTypes;
        Boolean implementRelayNode = this.implementRelayNode;
        boolean defineRelayNodeIfNotExist = this.defineRelayNodeIfNotExist;
        try {
            // Set all to false so that we don't try to register anything in the null schema builder
            this.defineNormalTypes = false;
            this.defineRelayTypes = false;
            this.implementRelayNode = false;
            this.defineRelayNodeIfNotExist = false;
            // For now, we scan all entity view types. Using the schema can be done later as optimization
            return create((GraphQLSchema.Builder) null, entityViewManager);
        } finally {
            this.defineNormalTypes = defineNormalTypes;
            this.defineRelayTypes = defineRelayTypes;
            this.implementRelayNode = implementRelayNode;
            this.defineRelayNodeIfNotExist = defineRelayNodeIfNotExist;
        }
    }

    /**
     * Returns a new {@link GraphQLEntityViewSupport} after registering the entity view types from {@link EntityViewManager}
     * on the given {@link TypeDefinitionRegistry}.
     *
     * @param schemaBuilder The registry to register types
     * @param entityViewManager The entity view manager
     * @return a new {@link GraphQLEntityViewSupport}
     */
    public GraphQLEntityViewSupport create(GraphQLSchema.Builder schemaBuilder, EntityViewManager entityViewManager) {
        Set<GraphQLType> additionalTypes = isDefineNormalTypes() ? getAndClearAdditionalTypes(schemaBuilder) : Collections.emptySet();

        EntityMetamodel entityMetamodel = entityViewManager.getService(EntityMetamodel.class);
        Map<String, Class<?>> typeNameToClass = new HashMap<>();
        Map<String, Map<String, String>> typeNameToFieldMapping = new HashMap<>();
        Map<Class<?>, String> registeredTypeNames = new HashMap<>();
        for (ManagedViewType<?> managedView : entityViewManager.getMetamodel().getManagedViews()) {
            if (typeFilterPattern != null && !typeFilterPattern.matcher(managedView.getJavaType().getName()).matches()) {
                continue;
            }
            String typeName = getObjectTypeName(managedView);
            String inputTypeName = getInputObjectTypeName(managedView);
            String description = getDescription(managedView.getJavaType());
            GraphQLObjectType.Builder builder = GraphQLObjectType.newObject().name(typeName);
            GraphQLInputObjectType.Builder inputBuilder = GraphQLInputObjectType.newInputObject().name(inputTypeName);
            if (description != null) {
                builder.description(description);
                inputBuilder.description(description);
            }
            for (MethodAttribute<?, ?> attribute : managedView.getAttributes()) {
                if (isIgnored(attribute.getJavaMethod())) {
                    continue;
                }
                GraphQLFieldDefinition.Builder fieldBuilder = GraphQLFieldDefinition.newFieldDefinition();
                String fieldName = getFieldName(attribute);
                fieldBuilder.name(fieldName);
                GraphQLOutputType type;
                GraphQLInputType inputType;
                if (attribute instanceof SingularAttribute<?, ?>) {
                    SingularAttribute<?, ?> singularAttribute = (SingularAttribute<?, ?>) attribute;
                    if (singularAttribute.isId() && !singularAttribute.isSubview()) {
                        type = getIdType(singularAttribute);
                        inputType = getInputIdType(singularAttribute);
                    } else {
                        type = getElementType(schemaBuilder, singularAttribute, registeredTypeNames, entityMetamodel);
                        inputType = getInputElementType(schemaBuilder, singularAttribute, registeredTypeNames, entityMetamodel);
                    }
                } else if (attribute instanceof MapAttribute<?, ?, ?>) {
                    MapAttribute<?, ?, ?> mapAttribute = (MapAttribute<?, ?, ?>) attribute;
                    type = getEntryType(schemaBuilder, attribute, getKeyType(schemaBuilder, mapAttribute, registeredTypeNames), getElementType(schemaBuilder, mapAttribute, registeredTypeNames));
                    inputType = getInputEntryType(schemaBuilder, attribute, getInputKeyType(schemaBuilder, mapAttribute, registeredTypeNames), getInputElementType(schemaBuilder, mapAttribute, registeredTypeNames));
                } else {
                    type = new GraphQLList(getElementType(schemaBuilder, (PluralAttribute<?, ?, ?>) attribute, registeredTypeNames));
                    inputType = new GraphQLList(getInputElementType(schemaBuilder, (PluralAttribute<?, ?, ?>) attribute, registeredTypeNames));
                }
                fieldBuilder.type(type);
                builder.field(fieldBuilder);
                addFieldMapping(typeNameToFieldMapping, typeName, attribute, fieldName);
                inputBuilder.field(GraphQLInputObjectField.newInputObjectField().name(fieldName).type(inputType).build());
                addFieldMapping(typeNameToFieldMapping, inputTypeName, attribute, fieldName);
            }
            addObjectTypeDefinition(schemaBuilder, typeNameToClass, managedView, builder.build(), inputBuilder.build());
        }

        Set<String> serializableBasicTypes = new HashSet<>();
        for (javax.persistence.metamodel.Type<?> basicType : entityMetamodel.getBasicTypes()) {
            for (Class<?> superType : ReflectionUtils.getSuperTypes(basicType.getJavaType())) {
                serializableBasicTypes.add(superType.getName());
            }

            serializableBasicTypes.add(basicType.getJavaType().getName());
        }

        serializableBasicTypes.add(Serializable[].class.getName());
        serializableBasicTypes.add(GraphQLCursor.class.getName());
        for (GraphQLType additionalType : additionalTypes) {
            String typeName;
            if (additionalType instanceof GraphQLObjectType) {
                typeName = ((GraphQLObjectType) additionalType).getName();
            } else if (additionalType instanceof GraphQLInterfaceType) {
                typeName = ((GraphQLInterfaceType) additionalType).getName();
            } else {
                typeName = null;
            }
            if (typeName == null || typeNameToClass.get(typeName) == null) {
                schemaBuilder.additionalType(additionalType);
            }
        }
        return new GraphQLEntityViewSupport(typeNameToClass, typeNameToFieldMapping, serializableBasicTypes);
    }

    private void addFieldMapping(Map<String, Map<String, String>> typeNameToFieldMapping, String typeName, MethodAttribute<?, ?> attribute, String fieldName) {
        if (!fieldName.equals(attribute.getName())) {
            Map<String, String> fieldMapping = typeNameToFieldMapping.get(typeName);
            if (fieldMapping == null) {
                typeNameToFieldMapping.put(typeName, fieldMapping = new HashMap<>());
            }
            fieldMapping.put(fieldName, attribute.getName());
        }
    }

    private <T> T getAnnotationValue(Annotation annotation, String memberName) {
        try {
            return (T) annotation.annotationType().getMethod(memberName).invoke(annotation);
        } catch (Exception e) {
            throw new RuntimeException("Can't access annotation member", e);
        }
    }

    private Set<GraphQLType> getAndClearAdditionalTypes(GraphQLSchema.Builder schemaBuilder) {
        // Option 1: Break into the builder and extract the additional types through reflection
        try {
            Field f = GraphQLSchema.Builder.class.getDeclaredField("additionalTypes");
            f.setAccessible(true);
            Set<GraphQLType> graphQLTypes = (Set<GraphQLType>) f.get(schemaBuilder);
            Set<GraphQLType> copy = new HashSet<>(graphQLTypes);
            graphQLTypes.clear();
            return copy;
        } catch (Exception e) {
            try {
                // Option 2: Build an intermediate schema to access the additional types
                //  Building this intermediate schema though only works since 1.3.1,
                //  because a GraphQL interface type is used by default for Java interfaces which wouldn't build before 1.3.1
                GraphQLSchema intermediateSchema = schemaBuilder.build();
                Set<GraphQLType> graphQLTypes = intermediateSchema.getAdditionalTypes();
                Method m = GraphQLSchema.Builder.class.getMethod("clearAdditionalTypes");
                m.invoke(schemaBuilder);
                return graphQLTypes;
            } catch (Exception e2) {
                RuntimeException runtimeException = new RuntimeException("Could not extract the additional types", e2);
                runtimeException.addSuppressed(e);
                throw runtimeException;
            }
        }
    }

    protected ObjectTypeDefinition newObjectTypeDefinition(String typeName, List<FieldDefinition> fieldDefinitions, String description) {
        return newObjectTypeDefinition(typeName, new ArrayList<>(0), fieldDefinitions, description);
    }

    protected ObjectTypeDefinition newObjectTypeDefinition(String typeName, List<Type> implementsTypes, List<FieldDefinition> fieldDefinitions, String description) {
        try {
            if (OBJECT_TYPE_DEFINITION_CONSTRUCTOR != null) {
//                new ObjectTypeDefinition(typeName, implementsTypes, directives, fieldDefinitions);
                ObjectTypeDefinition typeDefinition = OBJECT_TYPE_DEFINITION_CONSTRUCTOR.newInstance(typeName, implementsTypes, new ArrayList<>(0), fieldDefinitions);
                if (description != null) {
                    OBJECT_TYPE_DEFINITION_SET_DESCRIPTION.invoke(typeDefinition, new Description(description, null, false));
                }
                return typeDefinition;
            } else {
//                ObjectTypeDefinition.newObjectTypeDefinition()
//                        .name(typeName)
//                        .fieldDefinitions(fieldDefinitions)
//                        .build()
                Object newObjectTypeDefinitionBuilder = OBJECT_TYPE_DEFINITION_NEW_BUILDER.invoke(null);
                OBJECT_TYPE_DEFINITION_BUILDER_NAME.invoke(newObjectTypeDefinitionBuilder, typeName);
                if (description != null) {
                    OBJECT_TYPE_DEFINITION_BUILDER_DESCRIPTION.invoke(newObjectTypeDefinitionBuilder, new Description(description, null, false));
                }
                OBJECT_TYPE_DEFINITION_BUILDER_IMPLEMENTS.invoke(newObjectTypeDefinitionBuilder, implementsTypes);
                OBJECT_TYPE_DEFINITION_BUILDER_FIELD_DEFINITIONS.invoke(newObjectTypeDefinitionBuilder, fieldDefinitions);
                return (ObjectTypeDefinition) OBJECT_TYPE_DEFINITION_BUILDER_BUILD.invoke(newObjectTypeDefinitionBuilder);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not build object type definition", e);
        }
    }

    protected InputObjectTypeDefinition newInputObjectTypeDefinition(String typeName, List<InputValueDefinition> valueDefinitions, String description) {
        try {
            if (INPUT_OBJECT_TYPE_DEFINITION_CONSTRUCTOR != null) {
                //                new InputObjectTypeDefinition(typeName, directives, valueDefinitions);
                InputObjectTypeDefinition typeDefinition = INPUT_OBJECT_TYPE_DEFINITION_CONSTRUCTOR.newInstance(typeName, new ArrayList<>(0), valueDefinitions);
                if (description != null) {
                    INPUT_OBJECT_TYPE_DEFINITION_SET_DESCRIPTION.invoke(typeDefinition, new Description(description, null, false));
                }
                return typeDefinition;
            } else {
                //                InputObjectTypeDefinition.newInputObjectTypeDefinition()
                //                        .name(typeName)
                //                        .valueDefinitions(valueDefinitions)
                //                        .build()
                Object newInputObjectTypeDefinitionBuilder = INPUT_OBJECT_TYPE_DEFINITION_NEW_BUILDER.invoke(null);
                INPUT_OBJECT_TYPE_DEFINITION_BUILDER_NAME.invoke(newInputObjectTypeDefinitionBuilder, typeName);
                if (description != null) {
                    INPUT_OBJECT_TYPE_DEFINITION_BUILDER_DESCRIPTION.invoke(newInputObjectTypeDefinitionBuilder, new Description(description, null, false));
                }
                INPUT_OBJECT_TYPE_DEFINITION_BUILDER_VALUE_DEFINITIONS.invoke(newInputObjectTypeDefinitionBuilder, valueDefinitions);
                return (InputObjectTypeDefinition) INPUT_OBJECT_TYPE_DEFINITION_BUILDER_BUILD.invoke(newInputObjectTypeDefinitionBuilder);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not build input object type definition", e);
        }
    }

    protected InterfaceTypeDefinition newInterfaceTypeDefinition(String name, List<FieldDefinition> fieldDefinitions, String description) {
        try {
            if (INTERFACE_TYPE_DEFINITION_CONSTRUCTOR != null) {
//                return new InterfaceTypeDefinition(name, fieldDefinitions, new ArrayList<>(0));
                InterfaceTypeDefinition typeDefinition = INTERFACE_TYPE_DEFINITION_CONSTRUCTOR.newInstance(name, fieldDefinitions, new ArrayList<>(0));
                if (description != null) {
                    INTERFACE_TYPE_DEFINITION_SET_DESCRIPTION.invoke(typeDefinition, new Description(description, null, false));
                }
                return typeDefinition;
            } else {
//                InterfaceTypeDefinition.newInterfaceTypeDefinition().name(name).definitions(fieldDefinitions).build()
                Object newInterfaceTypeDefinitionBuilder = INTERFACE_TYPE_DEFINITION_NEW_BUILDER.invoke(null);
                INTERFACE_TYPE_DEFINITION_BUILDER_NAME.invoke(newInterfaceTypeDefinitionBuilder, name);
                if (description != null) {
                    INTERFACE_TYPE_DEFINITION_BUILDER_DESCRIPTION.invoke(newInterfaceTypeDefinitionBuilder, new Description(description, null, false));
                }
                INTERFACE_TYPE_DEFINITION_BUILDER_FIELD_DEFINITIONS.invoke(newInterfaceTypeDefinitionBuilder, fieldDefinitions);
                return (InterfaceTypeDefinition) INTERFACE_TYPE_DEFINITION_BUILDER_BUILD.invoke(newInterfaceTypeDefinitionBuilder);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not build object type definition", e);
        }
    }

    protected EnumTypeDefinition newEnumTypeDefinition(String typeName, List<EnumValueDefinition> enumValueDefinitions, String description) {
        try {
            if (ENUM_TYPE_DEFINITION_CONSTRUCTOR != null) {
//                return new EnumTypeDefinition(typeName, enumValueDefinitions, new ArrayList<>(0));
                EnumTypeDefinition typeDefinition = ENUM_TYPE_DEFINITION_CONSTRUCTOR.newInstance(typeName, enumValueDefinitions, new ArrayList<>(0));
                if (description != null) {
                    ENUM_TYPE_DEFINITION_SET_DESCRIPTION.invoke(typeDefinition, new Description(description, null, false));
                }
                return typeDefinition;
            } else {
//                EnumTypeDefinition.newEnumTypeDefinition()
//                        .name(typeName)
//                        .enumValueDefinitions(enumValueDefinitions)
//                        .build()
                Object newEnumTypeDefinitionBuilder = ENUM_TYPE_DEFINITION_NEW_BUILDER.invoke(null);
                ENUM_TYPE_DEFINITION_BUILDER_NAME.invoke(newEnumTypeDefinitionBuilder, typeName);
                if (description != null) {
                    ENUM_TYPE_DEFINITION_BUILDER_DESCRIPTION.invoke(newEnumTypeDefinitionBuilder, new Description(description, null, false));
                }
                ENUM_TYPE_DEFINITION_BUILDER_FIELD_DEFINITIONS.invoke(newEnumTypeDefinitionBuilder, enumValueDefinitions);
                return (EnumTypeDefinition) ENUM_TYPE_DEFINITION_BUILDER_BUILD.invoke(newEnumTypeDefinitionBuilder);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not build object type definition", e);
        }
    }

    protected void addObjectTypeDefinition(TypeDefinitionRegistry typeRegistry, Map<String, Class<?>> typeNameToClass, ManagedViewType<?> managedView, ObjectTypeDefinition objectTypeDefinition, InputObjectTypeDefinition inputObjectTypeDefinition) {
        registerManagedViewType(typeRegistry, typeNameToClass, managedView, objectTypeDefinition);
        if (isDefineNormalTypes()) {
            addDefinition(typeRegistry, objectTypeDefinition);
        }
        if (inputObjectTypeDefinition != null) {
            registerManagedViewType(typeRegistry, typeNameToClass, managedView, inputObjectTypeDefinition);
            if (isDefineNormalTypes()) {
                addDefinition(typeRegistry, inputObjectTypeDefinition);
            }
        }
        List<Type> implementTypes = new ArrayList<>(objectTypeDefinition.getImplements());
        if (isImplementRelayNode()) {
            implementTypes.add(new TypeName("Node"));
        }
        ObjectTypeDefinition nodeType = newObjectTypeDefinition(objectTypeDefinition.getName() + "Node", implementTypes, objectTypeDefinition.getFieldDefinitions(), null);

        if (!typeRegistry.getType("Node").isPresent() && isImplementRelayNode() && isDefineRelayNodeIfNotExist()) {
            List<FieldDefinition> nodeFields = new ArrayList<>(4);
            nodeFields.add(new FieldDefinition("id", new NonNullType(new TypeName("ID"))));
            addDefinition(typeRegistry, newInterfaceTypeDefinition("Node", nodeFields, null));
        }

        List<FieldDefinition> edgeFields = new ArrayList<>(2);
        edgeFields.add(new FieldDefinition("node", new NonNullType(new TypeName(nodeType.getName()))));
        edgeFields.add(new FieldDefinition("cursor", new NonNullType(new TypeName("String"))));
        ObjectTypeDefinition edgeType = newObjectTypeDefinition(objectTypeDefinition.getName() + "Edge", edgeFields, null);

        List<FieldDefinition> connectionFields = new ArrayList<>(2);
        connectionFields.add(new FieldDefinition("edges", new ListType(new TypeName(edgeType.getName()))));
        connectionFields.add(new FieldDefinition("pageInfo", new NonNullType(new TypeName("PageInfo"))));
        connectionFields.add(new FieldDefinition("totalCount", new NonNullType(new TypeName("Int"))));
        ObjectTypeDefinition connectionType = newObjectTypeDefinition(objectTypeDefinition.getName() + "Connection", connectionFields, null);

        if (!typeRegistry.getType("PageInfo").isPresent() && isDefineRelayNodeIfNotExist()) {
            List<FieldDefinition> pageInfoFields = new ArrayList<>(4);
            pageInfoFields.add(new FieldDefinition("hasNextPage", new NonNullType(new TypeName("Boolean"))));
            pageInfoFields.add(new FieldDefinition("hasPreviousPage", new NonNullType(new TypeName("Boolean"))));
            pageInfoFields.add(new FieldDefinition("startCursor", new TypeName("String")));
            pageInfoFields.add(new FieldDefinition("endCursor", new TypeName("String")));
            addDefinition(typeRegistry, newObjectTypeDefinition("PageInfo", pageInfoFields, null));
        }

        registerManagedViewType(typeRegistry, typeNameToClass, managedView, nodeType);
        registerManagedViewType(typeRegistry, typeNameToClass, managedView, edgeType);
        registerManagedViewType(typeRegistry, typeNameToClass, managedView, connectionType);
        if (isDefineRelayTypes()) {
            addDefinition(typeRegistry, nodeType);
            addDefinition(typeRegistry, edgeType);
            addDefinition(typeRegistry, connectionType);
        }
    }

    protected void addObjectTypeDefinition(GraphQLSchema.Builder schemaBuilder, Map<String, Class<?>> typeNameToClass, ManagedViewType<?> managedView, GraphQLObjectType objectType, GraphQLInputObjectType inputObjectType) {
        typeNameToClass.put(inputObjectType.getName(), managedView.getJavaType());
        if (isDefineNormalTypes()) {
            schemaBuilder.additionalType(inputObjectType);
        }
        if (managedView.isUpdatable() || managedView.isCreatable()) {
            // No need to define relay types for creatable/updatable views
            return;
        }
        String nodeTypeName;
        String edgeTypeName;
        String connectionTypeName;
        String pageInfoTypeName;
        if (scalarTypeMap == null) {
            nodeTypeName = objectType.getName() + "Node";
            edgeTypeName = objectType.getName() + "Edge";
            connectionTypeName = objectType.getName() + "Connection";
            pageInfoTypeName = "PageInfo";

            GraphQLObjectType.Builder nodeType = GraphQLObjectType.newObject().name(nodeTypeName);
            nodeType.fields(objectType.getFieldDefinitions());
            if (isImplementRelayNode()) {
                nodeType.withInterface(new GraphQLTypeReference("Node"));
                if (!typeNameToClass.containsKey("Node") && isDefineRelayNodeIfNotExist()) {
                    GraphQLInterfaceType.Builder nodeInterfaceType = GraphQLInterfaceType.newInterface().name("Node")
                            .field(
                                    GraphQLFieldDefinition.newFieldDefinition().name("id")
                                            .type(new GraphQLNonNull(getScalarType("ID")))
                                            .build()
                            );
                    schemaBuilder.additionalType(nodeInterfaceType.build());
                    typeNameToClass.put("Node", Object.class);
                }
            }
            if (isDefineNormalTypes()) {
                schemaBuilder.additionalType(nodeType.build());
            }
            typeNameToClass.put(nodeTypeName, managedView.getJavaType());
        } else {
            // We use the presence of the scalar map to detect if we are on SmallRye
            // We have to adapt to their naming convention for generic types
            nodeTypeName = objectType.getName();
            edgeTypeName = "GraphQLRelayEdge_" + objectType.getName();
            connectionTypeName = "GraphQLRelayConnection_" + objectType.getName();
            pageInfoTypeName = "GraphQLRelayPageInfo";
        }

        GraphQLObjectType.Builder edgeType = GraphQLObjectType.newObject().name(edgeTypeName);
        edgeType.field(GraphQLFieldDefinition.newFieldDefinition().name("node")
                .type(new GraphQLNonNull(new GraphQLTypeReference(nodeTypeName)))
                .build());
        edgeType.field(GraphQLFieldDefinition.newFieldDefinition().name("cursor")
                .type(new GraphQLNonNull(getScalarType("String")))
                .build());

        GraphQLObjectType.Builder connectionType = GraphQLObjectType.newObject().name(connectionTypeName);
        connectionType.field(GraphQLFieldDefinition.newFieldDefinition().name("edges")
                .type(new GraphQLList(new GraphQLTypeReference(edgeTypeName)))
                .build());
        connectionType.field(GraphQLFieldDefinition.newFieldDefinition().name("pageInfo")
                .type(new GraphQLNonNull(new GraphQLTypeReference(pageInfoTypeName)))
                .build());
        connectionType.field(GraphQLFieldDefinition.newFieldDefinition().name("totalCount")
                .type(new GraphQLNonNull(getScalarType("Int")))
                .build());

        if (!typeNameToClass.containsKey(pageInfoTypeName) && isDefineRelayNodeIfNotExist()) {
            GraphQLObjectType.Builder pageInfoType = GraphQLObjectType.newObject().name(pageInfoTypeName);
            pageInfoType.field(GraphQLFieldDefinition.newFieldDefinition().name("hasNextPage")
                    .type(new GraphQLNonNull(getScalarType("Boolean")))
                    .build());
            pageInfoType.field(GraphQLFieldDefinition.newFieldDefinition().name("hasPreviousPage")
                    .type(new GraphQLNonNull(getScalarType("Boolean")))
                    .build());
            pageInfoType.field(GraphQLFieldDefinition.newFieldDefinition().name("startCursor")
                    .type(getScalarType("String"))
                    .build());
            pageInfoType.field(GraphQLFieldDefinition.newFieldDefinition().name("endCursor")
                    .type(getScalarType("String"))
                    .build());
            schemaBuilder.additionalType(pageInfoType.build());
            typeNameToClass.put(pageInfoTypeName, GraphQLRelayPageInfo.class);
        }
        typeNameToClass.put(edgeTypeName, managedView.getJavaType());
        typeNameToClass.put(connectionTypeName, managedView.getJavaType());
        typeNameToClass.put(objectType.getName(), managedView.getJavaType());
        if (isDefineNormalTypes()) {
            schemaBuilder.additionalType(objectType);
        }

        if (isDefineRelayTypes()) {
            schemaBuilder.additionalType(edgeType.build());
            schemaBuilder.additionalType(connectionType.build());
        }
    }

    protected void addDefinition(TypeDefinitionRegistry typeRegistry, Definition<?> definition) {
        try {
            TYPE_REGISTRY_ADD.invoke(typeRegistry, definition);
        } catch (Exception e) {
            throw new RuntimeException("Could not add definition", e);
        }
    }

    protected void registerManagedViewType(TypeDefinitionRegistry typeRegistry, Map<String, Class<?>> typeNameToClass, ManagedViewType<?> managedView, TypeDefinition<?> objectTypeDefinition) {
        if (isDefineNormalTypes()) {
            addDefinition(typeRegistry, objectTypeDefinition);
        }
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
     * Return the GraphQL id type for the given singular attribute.
     *
     * @param typeRegistry The type registry
     * @param singularAttribute The singular attribute
     * @return The type
     */
    protected Type getInputIdType(TypeDefinitionRegistry typeRegistry, SingularAttribute<?, ?> singularAttribute) {
        // Ideally, we would make this only nullable if the value is generated, but that's hard to determine
        return new TypeName("ID");
    }

    /**
     * Return the GraphQL id type for the given singular attribute.
     *
     * @param singularAttribute The singular attribute
     * @return The type
     */
    protected GraphQLOutputType getIdType(SingularAttribute<?, ?> singularAttribute) {
        if (scalarTypeMap != null) {
            return new GraphQLNonNull(scalarTypeMap.get("ID"));
        }
        return new GraphQLNonNull(new GraphQLTypeReference("ID"));
    }

    /**
     * Return the GraphQL id type for the given singular attribute.
     *
     * @param singularAttribute The singular attribute
     * @return The type
     */
    protected GraphQLInputType getInputIdType(SingularAttribute<?, ?> singularAttribute) {
        // Ideally, we would make this only nullable if the value is generated, but that's hard to determine
        if (scalarTypeMap != null) {
            return scalarTypeMap.get("ID");
        }
        return new GraphQLTypeReference("ID");
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
        if (isDefineNormalTypes()) {
            addDefinition(typeRegistry, newObjectTypeDefinition(entryName, fields, null));
        }
        return new ListType(new TypeName(entryName));
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
    protected Type getInputEntryType(TypeDefinitionRegistry typeRegistry, MethodAttribute<?, ?> attribute, Type key, Type value) {
        String entryName = getObjectTypeName(attribute.getDeclaringType()) + StringUtils.firstToLower(attribute.getName()) + "EntryInput";
        List<FieldDefinition> fields = new ArrayList<>();
        fields.add(new FieldDefinition("key", key));
        fields.add(new FieldDefinition("value", value));
        if (isDefineNormalTypes()) {
            addDefinition(typeRegistry, newObjectTypeDefinition(entryName, fields, null));
        }
        return new ListType(new TypeName(entryName));
    }

    /**
     * Return the GraphQL entry type for the given map attribute with the given key and value types.
     *
     * @param schemaBuilder The schema builder
     * @param attribute The map attribute
     * @param key The key type
     * @param value The value type
     * @return The type
     */
    protected GraphQLOutputType getEntryType(GraphQLSchema.Builder schemaBuilder, MethodAttribute<?, ?> attribute, GraphQLOutputType key, GraphQLOutputType value) {
        String entryName = getObjectTypeName(attribute.getDeclaringType()) + StringUtils.firstToLower(attribute.getName()) + "Entry";
        GraphQLObjectType type = GraphQLObjectType.newObject().name(entryName)
                .field(GraphQLFieldDefinition.newFieldDefinition().name("key").type(key))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("value").type(value))
                .build();
        if (isDefineNormalTypes()) {
            schemaBuilder.additionalType(type);
        }
        return new GraphQLList(new GraphQLTypeReference(entryName));
    }

    /**
     * Return the GraphQL entry type for the given map attribute with the given key and value types.
     *
     * @param schemaBuilder The schema builder
     * @param attribute The map attribute
     * @param key The key type
     * @param value The value type
     * @return The type
     */
    protected GraphQLInputType getInputEntryType(GraphQLSchema.Builder schemaBuilder, MethodAttribute<?, ?> attribute, GraphQLInputType key, GraphQLInputType value) {
        String entryName = getObjectTypeName(attribute.getDeclaringType()) + StringUtils.firstToLower(attribute.getName()) + "EntryInput";
        GraphQLInputObjectType type = GraphQLInputObjectType.newInputObject().name(entryName)
                .field(GraphQLInputObjectField.newInputObjectField().name("key").type(key))
                .field(GraphQLInputObjectField.newInputObjectField().name("value").type(value))
                .build();
        if (isDefineNormalTypes()) {
            schemaBuilder.additionalType(type);
        }
        return new GraphQLList(new GraphQLTypeReference(entryName));
    }

    /**
     * Returns the GraphQL type name for the given managed view type.
     *
     * @param type The managed view type
     * @return The GraphQL type name
     */
    protected String getObjectTypeName(ManagedViewType type) {
        //CHECKSTYLE:OFF: MissingSwitchDefault
        for (Annotation annotation : type.getJavaType().getAnnotations()) {
            switch (annotation.annotationType().getName()) {
                case "org.eclipse.microprofile.graphql.Name":
                case "org.eclipse.microprofile.graphql.Type":
                    return getAnnotationValue(annotation, "value");
                case "io.leangen.graphql.annotations.types.GraphQLType":
                case "io.leangen.graphql.annotations.types.GraphQLInterface":
                case "io.leangen.graphql.annotations.types.GraphQLUnion":
                    return getAnnotationValue(annotation, "name");
            }
        }
        //CHECKSTYLE:ON: MissingSwitchDefault
        return type.getJavaType().getSimpleName();
    }

    /**
     * Returns the GraphQL input type name for the given managed view type.
     *
     * @param managedView The managed view type
     * @return The GraphQL type name
     */
    protected String getInputObjectTypeName(ManagedViewType managedView) {
        String typeName = getObjectTypeName(managedView);
        // So far, we only use this for MicroProfile GraphQL where we can't register custom types
        // and instead have to simply use the name the MP GraphQL implementations choose for such types.
        // In case of input object types, implementations don't suffix the name with "Input" since the type is abstract
        if (Modifier.isAbstract(managedView.getJavaType().getModifiers()) && (managedView.isCreatable() || managedView.isUpdatable())) {
            return typeName;
        } else {
            return typeName + "Input";
        }
    }

    /**
     * Returns the GraphQL type name for the given java type.
     *
     * @param type The java type
     * @return The GraphQL type name
     */
    protected String getTypeName(Class<?> type) {
        //CHECKSTYLE:OFF: MissingSwitchDefault
        for (Annotation annotation : type.getAnnotations()) {
            switch (annotation.annotationType().getName()) {
                case "org.eclipse.microprofile.graphql.Name":
                case "org.eclipse.microprofile.graphql.Type":
                    return getAnnotationValue(annotation, "value");
                case "io.leangen.graphql.annotations.types.GraphQLType":
                case "io.leangen.graphql.annotations.types.GraphQLInterface":
                case "io.leangen.graphql.annotations.types.GraphQLUnion":
                    return getAnnotationValue(annotation, "name");
                case "org.eclipse.microprofile.graphql.Enum":
                    if (type.isEnum()) {
                        return getAnnotationValue(annotation, "value");
                    }
            }
        }
        //CHECKSTYLE:ON: MissingSwitchDefault
        return type.getSimpleName();
    }

    /**
     * Returns the GraphQL field name for the given attribute.
     *
     * @param attribute The attribute
     * @return The GraphQL field name
     */
    protected String getFieldName(MethodAttribute<?, ?> attribute) {
        //CHECKSTYLE:OFF: MissingSwitchDefault
        for (Annotation annotation : attribute.getJavaMethod().getAnnotations()) {
            switch (annotation.annotationType().getName()) {
                case "org.eclipse.microprofile.graphql.Name":
                    return getAnnotationValue(annotation, "value");
                case "com.netflix.graphql.dgs.DgsData":
                    return getAnnotationValue(annotation, "field");
                case "io.leangen.graphql.annotations.GraphQLQuery":
                    return getAnnotationValue(annotation, "name");
            }
        }
        //CHECKSTYLE:ON: MissingSwitchDefault
        return attribute.getName();
    }

    /**
     * Returns the GraphQL description for the given java type.
     *
     * @param type The java type
     * @return The GraphQL type description
     */
    protected String getDescription(Class<?> type) {
        //CHECKSTYLE:OFF: MissingSwitchDefault
        for (Annotation annotation : type.getAnnotations()) {
            switch (annotation.annotationType().getName()) {
                case "org.eclipse.microprofile.graphql.Description":
                    return getAnnotationValue(annotation, "value");
                case "io.leangen.graphql.annotations.types.GraphQLType":
                case "io.leangen.graphql.annotations.types.GraphQLInterface":
                case "io.leangen.graphql.annotations.types.GraphQLUnion":
                    return getAnnotationValue(annotation, "description");
            }
        }
        //CHECKSTYLE:ON: MissingSwitchDefault
        return null;
    }

    /**
     * Returns whether the GraphQL field for the method should be ignored.
     *
     * @param javaMethod The java method
     * @return Whether the field should be ignored
     */
    protected boolean isIgnored(Method javaMethod) {
        //CHECKSTYLE:OFF: MissingSwitchDefault
        for (Annotation annotation : javaMethod.getAnnotations()) {
            switch (annotation.annotationType().getName()) {
                case "org.eclipse.microprofile.graphql.Ignore":
                case "io.leangen.graphql.annotations.GraphQLIgnore":
                    return true;
            }
        }
        //CHECKSTYLE:ON: MissingSwitchDefault
        return false;
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
     * Return the GraphQL type for the given managed view type.
     *
     * @param type The managed view type
     * @return The type
     */
    protected Type getInputObjectType(ManagedViewType type) {
        return new TypeName(getObjectTypeName(type) + "Input");
    }

    /**
     * Return the GraphQL type for the given managed view type.
     *
     * @param type The managed view type
     * @return The type
     */
    protected GraphQLOutputType getObjectTypeReference(ManagedViewType<?> type) {
        return new GraphQLTypeReference(getObjectTypeName(type));
    }

    /**
     * Return the GraphQL type for the given managed view type.
     *
     * @param type The managed view type
     * @return The type
     */
    protected GraphQLInputType getInputObjectTypeReference(ManagedViewType<?> type) {
        return new GraphQLTypeReference(getInputObjectTypeName(type));
    }

    /**
     * Return the GraphQL type for the given singular attribute.
     *
     * @param typeRegistry The type registry
     * @param singularAttribute The singular attribute
     * @param entityMetamodel The entity metamodel
     * @return The type
     */
    protected Type getElementType(TypeDefinitionRegistry typeRegistry, SingularAttribute<?, ?> singularAttribute, EntityMetamodel entityMetamodel) {
        com.blazebit.persistence.view.metamodel.Type elementType = singularAttribute.getType();
        Type type;
        if (elementType.getMappingType() == com.blazebit.persistence.view.metamodel.Type.MappingType.BASIC) {
            type = getScalarType(typeRegistry, elementType.getJavaType());
        } else {
            type = getObjectType((ManagedViewType<?>) elementType);
        }
        if (singularAttribute.isId() || isNotNull(singularAttribute, entityMetamodel)) {
            type = new NonNullType(type);
        }
        return type;
    }

    /**
     * Return the GraphQL type for the given singular attribute.
     *
     * @param typeRegistry The type registry
     * @param singularAttribute The singular attribute
     * @param entityMetamodel The entity metamodel
     * @return The type
     */
    protected Type getInputElementType(TypeDefinitionRegistry typeRegistry, SingularAttribute<?, ?> singularAttribute, EntityMetamodel entityMetamodel) {
        com.blazebit.persistence.view.metamodel.Type elementType = singularAttribute.getType();
        Type type;
        if (elementType.getMappingType() == com.blazebit.persistence.view.metamodel.Type.MappingType.BASIC) {
            type = getScalarType(typeRegistry, elementType.getJavaType());
        } else {
            type = getInputObjectType((ManagedViewType<?>) elementType);
        }
        if (singularAttribute.isId() || isNotNull(singularAttribute, entityMetamodel)) {
            type = new NonNullType(type);
        }
        return type;
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
     * Return the GraphQL type for the given plural attribute.
     *
     * @param typeRegistry The type registry
     * @param pluralAttribute The plural attribute
     * @return The type
     */
    protected Type getInputElementType(TypeDefinitionRegistry typeRegistry, PluralAttribute<?, ?, ?> pluralAttribute) {
        com.blazebit.persistence.view.metamodel.Type elementType = pluralAttribute.getElementType();
        if (elementType.getMappingType() == com.blazebit.persistence.view.metamodel.Type.MappingType.BASIC) {
            return getScalarType(typeRegistry, elementType.getJavaType());
        } else {
            return getInputObjectType((ManagedViewType<?>) elementType);
        }
    }

    /**
     * Return the GraphQL type for the given singular attribute.
     *
     * @param schemaBuilder The schema builder
     * @param singularAttribute The singular attribute
     * @param entityMetamodel The entity metamodel
     * @return The type
     */
    protected GraphQLOutputType getElementType(GraphQLSchema.Builder schemaBuilder, SingularAttribute<?, ?> singularAttribute, Map<Class<?>, String> registeredTypeNames, EntityMetamodel entityMetamodel) {
        com.blazebit.persistence.view.metamodel.Type<?> elementType = singularAttribute.getType();
        GraphQLOutputType type;
        if (elementType.getMappingType() == com.blazebit.persistence.view.metamodel.Type.MappingType.BASIC) {
            type = getScalarType(schemaBuilder, elementType.getJavaType(), registeredTypeNames);
        } else {
            type = getObjectTypeReference((ManagedViewType<?>) elementType);
        }
        if (singularAttribute.isId() || isNotNull(singularAttribute, entityMetamodel)) {
            type = new GraphQLNonNull(type);
        }
        return type;
    }

    /**
     * Return the GraphQL type for the given singular attribute.
     *
     * @param schemaBuilder The schema builder
     * @param singularAttribute The singular attribute
     * @param entityMetamodel The entity metamodel
     * @return The type
     */
    protected GraphQLInputType getInputElementType(GraphQLSchema.Builder schemaBuilder, SingularAttribute<?, ?> singularAttribute, Map<Class<?>, String> registeredTypeNames, EntityMetamodel entityMetamodel) {
        com.blazebit.persistence.view.metamodel.Type<?> elementType = singularAttribute.getType();
        GraphQLInputType type;
        if (elementType.getMappingType() == com.blazebit.persistence.view.metamodel.Type.MappingType.BASIC) {
            type = (GraphQLInputType) getScalarType(schemaBuilder, elementType.getJavaType(), registeredTypeNames);
        } else {
            type = getInputObjectTypeReference((ManagedViewType<?>) elementType);
        }
        if (singularAttribute.isId() || isNotNull(singularAttribute, entityMetamodel)) {
            type = new GraphQLNonNull(type);
        }
        return type;
    }

    /**
     * Return the GraphQL type for the given plural attribute.
     *
     * @param schemaBuilder The schema builder
     * @param pluralAttribute The plural attribute
     * @return The type
     */
    protected GraphQLOutputType getElementType(GraphQLSchema.Builder schemaBuilder, PluralAttribute<?, ?, ?> pluralAttribute, Map<Class<?>, String> registeredTypeNames) {
        com.blazebit.persistence.view.metamodel.Type<?> elementType = pluralAttribute.getElementType();
        if (elementType.getMappingType() == com.blazebit.persistence.view.metamodel.Type.MappingType.BASIC) {
            return getScalarType(schemaBuilder, elementType.getJavaType(), registeredTypeNames);
        } else {
            return getObjectTypeReference((ManagedViewType<?>) elementType);
        }
    }

    /**
     * Return the GraphQL type for the given plural attribute.
     *
     * @param schemaBuilder The schema builder
     * @param pluralAttribute The plural attribute
     * @return The type
     */
    protected GraphQLInputType getInputElementType(GraphQLSchema.Builder schemaBuilder, PluralAttribute<?, ?, ?> pluralAttribute, Map<Class<?>, String> registeredTypeNames) {
        com.blazebit.persistence.view.metamodel.Type<?> elementType = pluralAttribute.getElementType();
        if (elementType.getMappingType() == com.blazebit.persistence.view.metamodel.Type.MappingType.BASIC) {
            return (GraphQLInputType) getScalarType(schemaBuilder, elementType.getJavaType(), registeredTypeNames);
        } else {
            return getInputObjectTypeReference((ManagedViewType<?>) elementType);
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
     * Return the GraphQL type for the key of the given map attribute.
     *
     * @param typeRegistry The type registry
     * @param mapAttribute The map attribute
     * @return The type
     */
    protected Type getInputKeyType(TypeDefinitionRegistry typeRegistry, MapAttribute<?, ?, ?> mapAttribute) {
        com.blazebit.persistence.view.metamodel.Type elementType = mapAttribute.getKeyType();
        if (elementType.getMappingType() == com.blazebit.persistence.view.metamodel.Type.MappingType.BASIC) {
            return getScalarType(typeRegistry, elementType.getJavaType());
        } else {
            return getInputObjectType((ManagedViewType<?>) elementType);
        }
    }

    /**
     * Return the GraphQL type for the key of the given map attribute.
     *
     * @param schemaBuilder The schema builder
     * @param mapAttribute The map attribute
     * @return The type
     */
    protected GraphQLOutputType getKeyType(GraphQLSchema.Builder schemaBuilder, MapAttribute<?, ?, ?> mapAttribute, Map<Class<?>, String> registeredTypeNames) {
        com.blazebit.persistence.view.metamodel.Type<?> elementType = mapAttribute.getKeyType();
        if (elementType.getMappingType() == com.blazebit.persistence.view.metamodel.Type.MappingType.BASIC) {
            return getScalarType(schemaBuilder, elementType.getJavaType(), registeredTypeNames);
        } else {
            return getObjectTypeReference((ManagedViewType<?>) elementType);
        }
    }

    /**
     * Return the GraphQL type for the key of the given map attribute.
     *
     * @param schemaBuilder The schema builder
     * @param mapAttribute The map attribute
     * @return The type
     */
    protected GraphQLInputType getInputKeyType(GraphQLSchema.Builder schemaBuilder, MapAttribute<?, ?, ?> mapAttribute, Map<Class<?>, String> registeredTypeNames) {
        com.blazebit.persistence.view.metamodel.Type<?> elementType = mapAttribute.getKeyType();
        if (elementType.getMappingType() == com.blazebit.persistence.view.metamodel.Type.MappingType.BASIC) {
            return (GraphQLInputType) getScalarType(schemaBuilder, elementType.getJavaType(), registeredTypeNames);
        } else {
            return getInputObjectTypeReference((ManagedViewType<?>) elementType);
        }
    }

    protected GraphQLOutputType getScalarType(String typeName) {
        if (scalarTypeMap != null) {
            GraphQLScalarType scalarType = scalarTypeMap.get(typeName);
            if (scalarType != null) {
                return scalarType;
            }
        }
        return new GraphQLTypeReference(typeName);
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

                    if (isDefineNormalTypes()) {
                        addDefinition(typeRegistry, newEnumTypeDefinition(typeName, enumValueDefinitions, getDescription(javaType)));
                    }
                }
            } else {
                typeName = "String";
            }
        }
        return new TypeName(typeName);
    }

    /**
     * Return the GraphQL type for the given scalar java type.
     *
     * @param schemaBuilder The schema builder
     * @param javaType The java type
     * @param registeredTypeNames The registered type names
     * @return The type
     */
    protected GraphQLOutputType getScalarType(GraphQLSchema.Builder schemaBuilder, Class<?> javaType, Map<Class<?>, String> registeredTypeNames) {
        if (scalarTypeMap != null) {
            GraphQLScalarType scalarType = scalarTypeMap.get(javaType.getName());
            if (scalarType != null) {
                return scalarType;
            }
        }
        String typeName = TYPES.get(javaType);
        if (typeName == null) {
            if (javaType.isEnum()) {
                typeName = getTypeName(javaType);
                if (!registeredTypeNames.containsKey(javaType)) {
                    GraphQLEnumType.Builder enumBuilder = GraphQLEnumType.newEnum().name(typeName);
                    for (Enum<?> enumConstant : (Enum<?>[]) javaType.getEnumConstants()) {
                        enumBuilder.value(enumConstant.name());
                    }

                    if (isDefineNormalTypes()) {
                        schemaBuilder.additionalType(enumBuilder.build());
                    }
                    registeredTypeNames.put(javaType, typeName);
                }
            } else {
                typeName = "String";
            }
        }
        return new GraphQLTypeReference(typeName);
    }

    protected boolean isNotNull(SingularAttribute<?, ?> attribute, EntityMetamodel entityMetamodel) {
        if (attribute instanceof MappingAttribute<?, ?> && !attribute.isQueryParameter()) {
            AbstractAttribute<?, ?> attr = (AbstractAttribute<?, ?>) attribute;
            Map<String, javax.persistence.metamodel.Type<?>> rootTypes = attr.getDeclaringType().getEntityViewRootTypes();
            if (rootTypes.isEmpty()) {
                rootTypes = Collections.singletonMap("this", attr.getDeclaringType().getJpaManagedType());
            } else {
                rootTypes = new HashMap<>(rootTypes);
                rootTypes.put("this", attr.getDeclaringType().getJpaManagedType());
            }
            return !ExpressionUtils.isNullable(entityMetamodel, rootTypes, attr.getMappingExpression());
        }
        return false;
    }
}
