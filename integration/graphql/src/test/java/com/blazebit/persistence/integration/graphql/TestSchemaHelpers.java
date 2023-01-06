/*
 * Copyright 2014 - 2023 Blazebit.
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

import com.blazebit.persistence.integration.graphql.views.DocumentView;
import com.blazebit.persistence.integration.graphql.views.PersonView;
import graphql.schema.Coercing;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.SelectedField;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author David Kubecka
 * @since 1.6.9
 */
public class TestSchemaHelpers {

    static GraphQLFieldDefinition idFieldDefinition = makeFieldDefinition("id", makeScalarType());
    static GraphQLFieldDefinition nameFieldDefinition = makeFieldDefinition("name", makeScalarType());
    static GraphQLObjectType personObjectType = makeObjectType("Person", idFieldDefinition, nameFieldDefinition);

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

    public static GraphQLObjectType makeObjectType(String name, GraphQLFieldDefinition... fields) {
        GraphQLObjectType.Builder objectTypeBuilder = new GraphQLObjectType.Builder();
        objectTypeBuilder.name(name);
        Arrays.stream(fields).forEach(objectTypeBuilder::field);
        return objectTypeBuilder.build();
    }

    public static GraphQLObjectType makeRelayConnection(GraphQLObjectType rootType) {
        GraphQLFieldDefinition nodeFieldDefinition = makeFieldDefinition("node", rootType);
        GraphQLObjectType edgeObjectType = makeObjectType("Edge", nodeFieldDefinition);
        GraphQLFieldDefinition edgesFieldDefinition = makeFieldDefinition("edges", new GraphQLList(edgeObjectType));
        return makeObjectType("Connection", edgesFieldDefinition);
    }

    public static DataFetchingFieldSelectionSet makeMockSelectionSet(String... fields) {
        List<SelectedField> selectedFields = Arrays.stream(fields).map(field -> {
            SelectedField selectedField = mock(SelectedField.class);
            when(selectedField.getQualifiedName()).thenReturn(field);
            return selectedField;
        }).collect(Collectors.toList());

        DataFetchingFieldSelectionSet selectionSet = mock(DataFetchingFieldSelectionSet.class);
        when(selectionSet.getFields()).thenReturn(selectedFields);
        when(selectionSet.getFields(any())).thenReturn(selectedFields);
        return selectionSet;
    }

    public static GraphQLEntityViewSupport getGraphQLEntityViewSupport() {
        TypeDef documentTypeDef = new TypeDef("Document", DocumentView.class, Arrays.asList("id", "name", "owner"));
        TypeDef personTypeDef = new TypeDef("Person", PersonView.class, Arrays.asList("id", "name"));
        return setupEntityViewSupport(documentTypeDef, personTypeDef);
    }

    public static GraphQLEntityViewSupport setupEntityViewSupport(TypeDef... typeDefs) {
        Map<String, Class<?>> typeNameToClass = new HashMap<>();
        Map<String, Map<String, String>> typeNameToFieldMapping = new HashMap<>();

        Arrays.stream(typeDefs).forEach(typeDef -> {
            String name = typeDef.name;
            typeNameToClass.put(name, typeDef.entityViewClass);
            Map<String, String> fieldMapping = new HashMap<>();
            typeDef.fields.forEach(field -> fieldMapping.put(field, field));
            typeNameToFieldMapping.put(name, fieldMapping);
        });

        return new GraphQLEntityViewSupport(typeNameToClass, typeNameToFieldMapping, Collections.emptySet());
    }

    public static DataFetchingEnvironment makeMockDataFetchingEnvironment(GraphQLFieldDefinition rootFieldDefinition, DataFetchingFieldSelectionSet selectionSet) {
        DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
        when(dfe.getFieldDefinition()).thenReturn(rootFieldDefinition);
        when(dfe.getSelectionSet()).thenReturn(selectionSet);
        return dfe;
    }

    static class TypeDef {
        private final String name;
        private final Class<?> entityViewClass;
        private final List<String> fields;

        public TypeDef(String name, Class<?> entityViewClass, List<String> fields) {
            this.name = name;
            this.entityViewClass = entityViewClass;
            this.fields = fields;
        }
    }
}
