/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql;

import com.blazebit.persistence.integration.graphql.views.AnimalView;
import com.blazebit.persistence.integration.graphql.views.CatView;
import com.blazebit.persistence.integration.graphql.views.DocumentView;
import com.blazebit.persistence.integration.graphql.views.PersonView;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;
import graphql.schema.Coercing;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNamedOutputType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLUnionType;
import graphql.schema.SelectedField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static graphql.schema.GraphQLTypeUtil.unwrapAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author David Kubecka
 * @since 1.6.9
 */
public class TestSchemaHelpers {

    private static final Map<String, Map<String, GraphQLNamedOutputType>> objectFieldToTypeMapping = new HashMap<>();

    static GraphQLFieldDefinition idFieldDefinition = makeFieldDefinition("id", makeScalarType());
    static GraphQLFieldDefinition nameFieldDefinition = makeFieldDefinition("name", makeScalarType());

    static GraphQLInterfaceType animalInterfaceType = makeInterfaceType( "Animal", idFieldDefinition, nameFieldDefinition);
    static GraphQLObjectType catObjectType = makeObjectType("Cat", animalInterfaceType, idFieldDefinition, nameFieldDefinition);
    static GraphQLFieldDefinition animalFieldDefinition = makeFieldDefinition( "animal", animalInterfaceType );

    static GraphQLObjectType personObjectType =
            makeObjectType("Person", idFieldDefinition, nameFieldDefinition, animalFieldDefinition);

    static GraphQLFieldDefinition ownerFieldDefinition = makeFieldDefinition("owner", personObjectType);
    static GraphQLObjectType documentObjectType =
            makeObjectType("Document", idFieldDefinition, nameFieldDefinition, ownerFieldDefinition);

    public static GraphQLFieldDefinition makeFieldDefinition(String name, GraphQLOutputType type) {
        GraphQLFieldDefinition.Builder fieldDefinitionBuilder = new GraphQLFieldDefinition.Builder();
        fieldDefinitionBuilder.name(name);
        fieldDefinitionBuilder.type(type);
        return fieldDefinitionBuilder.build();
    }

    public static GraphQLScalarType makeScalarType() {
        GraphQLScalarType.Builder scalarTypeBuilder = new GraphQLScalarType.Builder();
        scalarTypeBuilder.name("String");
        scalarTypeBuilder.coercing(mock(Coercing.class));
        return scalarTypeBuilder.build();
    }

    public static GraphQLInterfaceType makeInterfaceType(String name, GraphQLFieldDefinition... fields) {
        GraphQLInterfaceType.Builder builder = new GraphQLInterfaceType.Builder();
        builder.name(name);
        HashMap<String, GraphQLNamedOutputType> fieldToTypeMapping = new HashMap<>();
        fieldToTypeMapping.put("__typename", makeScalarType());
        Arrays.stream(fields).forEach(builder::field);
        Arrays.stream(fields).forEach(field -> fieldToTypeMapping.put(field.getName(), (GraphQLNamedOutputType) unwrapAll(field.getType())));
        objectFieldToTypeMapping.put(name, fieldToTypeMapping);
        return builder.build();
    }

    public static GraphQLObjectType makeObjectType(String name, GraphQLFieldDefinition... fields) {
        return makeObjectType(name, null, fields);
    }

    public static GraphQLObjectType makeObjectType(String name, GraphQLInterfaceType interfaceType, GraphQLFieldDefinition... fields) {
        GraphQLObjectType.Builder objectTypeBuilder = new GraphQLObjectType.Builder();
        objectTypeBuilder.name(name);
        if (interfaceType != null) {
            objectTypeBuilder.withInterface(interfaceType);
        }
        HashMap<String, GraphQLNamedOutputType> fieldToTypeMapping = new HashMap<>();
        fieldToTypeMapping.put("__typename", makeScalarType());
        Arrays.stream(fields).forEach(objectTypeBuilder::field);
        Arrays.stream(fields).forEach(field -> fieldToTypeMapping.put(field.getName(), (GraphQLNamedOutputType) unwrapAll(field.getType())));
        objectFieldToTypeMapping.put(name, fieldToTypeMapping);
        return objectTypeBuilder.build();
    }

    public static GraphQLObjectType makeRelayConnection(GraphQLObjectType rootType) {
        GraphQLFieldDefinition nodeFieldDefinition = makeFieldDefinition("node", rootType);
        GraphQLObjectType edgeObjectType = makeObjectType("Edge", nodeFieldDefinition);
        GraphQLFieldDefinition edgesFieldDefinition = makeFieldDefinition("edges", new GraphQLList(edgeObjectType));
        return makeObjectType("Connection", edgesFieldDefinition);
    }

    public static DataFetchingFieldSelectionSet makeMockSelectionSet(String rootType, String... fields) {
        List<SelectedField> selectedFields = Arrays.stream(fields).map(field -> {
            List<String> qualifiedFieldParts = new ArrayList<>();
            String[] fieldParts = field.split("/");
            GraphQLNamedOutputType fieldType = null;
            String baseType = rootType;
            for (String fieldPart : fieldParts) {
                if (fieldPart.contains(".")) {
                    // provided fieldPart is already fully qualified
                    qualifiedFieldParts.add(fieldPart);
                    baseType = getBaseTypes(fieldPart)[0];
                    fieldPart = (fieldPart.split("\\."))[1];
                } else {
                    qualifiedFieldParts.add(baseType + "." + fieldPart);
                }
                fieldType = objectFieldToTypeMapping.get(baseType).get(fieldPart);
                baseType = fieldType.getName();
            }
            SelectedField selectedField = mock(SelectedField.class);
            when(selectedField.getFullyQualifiedName()).thenReturn(String.join("/", qualifiedFieldParts));
            when(selectedField.getQualifiedName()).thenReturn(String.join("/", fieldParts));
            when(selectedField.getType()).thenReturn(fieldType);
            when(selectedField.getObjectTypeNames()).thenReturn(Arrays.asList(getBaseTypes(fieldParts[fieldParts.length-1])));
            return selectedField;
        }).collect(Collectors.toList());

        DataFetchingFieldSelectionSet selectionSet = mock(DataFetchingFieldSelectionSet.class);
        when(selectionSet.getFields()).thenReturn(selectedFields);
        return selectionSet;
    }

    private static String[] getBaseTypes(String fieldPart) {
        return (fieldPart.split("\\."))[0].replaceFirst("^\\[", "").replaceFirst("]$", "").split(",");
    }

    public static GraphQLEntityViewSupport getGraphQLEntityViewSupport() {
        TypeDef documentTypeDef = new TypeDef("Document", DocumentView.class, Arrays.asList("id", "name", "owner"));
        TypeDef personTypeDef = new TypeDef("Person", PersonView.class, Arrays.asList("id", "name", "animal"));
        TypeDef animalTypeDef = new TypeDef("Animal", AnimalView.class, Arrays.asList("id", "name"));
        TypeDef catTypeDef = new TypeDef("Cat", CatView.class, Arrays.asList("id", "name"));
        return setupEntityViewSupport(documentTypeDef, personTypeDef, animalTypeDef, catTypeDef);
    }

    public static GraphQLEntityViewSupport setupEntityViewSupport(TypeDef... typeDefs) {
        Map<String, ManagedViewType<?>> typeNameToViewType = new HashMap<>();
        Map<String, Map<String, String>> typeNameToFieldMapping = new HashMap<>();

        Arrays.stream(typeDefs).forEach(typeDef -> {
            String name = typeDef.name;
            typeNameToViewType.put(name, typeDef.viewType);
            Map<String, String> fieldMapping = new HashMap<>();
            typeDef.fields.forEach(field -> fieldMapping.put(field, field));
            typeNameToFieldMapping.put(name, fieldMapping);
        });

        return new GraphQLEntityViewSupport(typeNameToViewType, typeNameToFieldMapping, Collections.emptySet());
    }

    public static DataFetchingEnvironment makeMockDataFetchingEnvironment(GraphQLFieldDefinition rootFieldDefinition, DataFetchingFieldSelectionSet selectionSet) {
        DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
        GraphQLSchema schema = mock(GraphQLSchema.class);
        when(dfe.getGraphQLSchema()).thenReturn(schema);
        when(dfe.getFieldDefinition()).thenReturn(rootFieldDefinition);
        when(dfe.getSelectionSet()).thenReturn(selectionSet);

        when(schema.getType(documentObjectType.getName())).thenReturn(documentObjectType);
        when(schema.getType(personObjectType.getName())).thenReturn(personObjectType);
        when(schema.getType( animalInterfaceType.getName())).thenReturn( animalInterfaceType );
        when(schema.getType(catObjectType.getName())).thenReturn(catObjectType);
        return dfe;
    }

    static class TypeDef {
        private final String name;
        private final ViewType<?> viewType;
        private final List<String> fields;

        public TypeDef(String name, Class entityViewClass, List<String> fields) {
            this.name = name;
            this.viewType = mock(ViewType.class);
            this.fields = fields;

            MethodAttribute idAttribute = mock(MethodAttribute.class);
            when(idAttribute.getName()).thenReturn("id");
            when(this.viewType.getJavaType()).thenReturn(entityViewClass);
            when(this.viewType.getIdAttribute()).thenReturn(idAttribute);
        }
    }
}
