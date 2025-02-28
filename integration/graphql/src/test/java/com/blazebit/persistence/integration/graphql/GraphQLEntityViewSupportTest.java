/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.integration.graphql.views.AnimalView;
import com.blazebit.persistence.integration.graphql.views.CatView;
import com.blazebit.persistence.integration.graphql.views.DocumentView;
import com.blazebit.persistence.integration.graphql.views.PersonView;
import com.blazebit.persistence.view.EntityViewSetting;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLFieldDefinition;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static com.blazebit.persistence.integration.graphql.TestSchemaHelpers.animalInterfaceType;
import static com.blazebit.persistence.integration.graphql.TestSchemaHelpers.documentObjectType;
import static com.blazebit.persistence.integration.graphql.TestSchemaHelpers.getGraphQLEntityViewSupport;
import static com.blazebit.persistence.integration.graphql.TestSchemaHelpers.makeFieldDefinition;
import static com.blazebit.persistence.integration.graphql.TestSchemaHelpers.makeMockDataFetchingEnvironment;
import static com.blazebit.persistence.integration.graphql.TestSchemaHelpers.makeMockSelectionSet;
import static com.blazebit.persistence.integration.graphql.TestSchemaHelpers.makeRelayConnection;
import static com.blazebit.persistence.integration.graphql.TestSchemaHelpers.personObjectType;

/**
 * @author David Kubecka
 * @since 1.6.9
 */
public class GraphQLEntityViewSupportTest {

    GraphQLEntityViewSupport graphQLEntityViewSupport = getGraphQLEntityViewSupport();

    @Test
    public void testFetchesInSetting() {
        GraphQLFieldDefinition rootFieldDefinition = makeFieldDefinition("getDocument", documentObjectType);
        DataFetchingFieldSelectionSet selectionSet =
                makeMockSelectionSet("Document", "name", "owner", "owner/name", "Document.__typename");

        DataFetchingEnvironment dfe = makeMockDataFetchingEnvironment(rootFieldDefinition, selectionSet);

        EntityViewSetting<DocumentView, CriteriaBuilder<DocumentView>> setting = graphQLEntityViewSupport.createSetting(dfe);

        Assert.assertEquals(new HashSet<>(Arrays.asList("id", "name", "owner.name")), setting.getFetches());
    }

    @Test
    public void testFetchesInPaginatedSetting() {
        GraphQLFieldDefinition rootFieldDefinition = makeFieldDefinition("getDocument", makeRelayConnection(documentObjectType));
        DataFetchingFieldSelectionSet selectionSet =
                makeMockSelectionSet("Connection", "edges", "edges/node", "edges/node/owner", "edges/node/owner/name");

        DataFetchingEnvironment dfe = makeMockDataFetchingEnvironment(rootFieldDefinition, selectionSet);

        EntityViewSetting<DocumentView, PaginatedCriteriaBuilder<DocumentView>> paginatedSetting =
                graphQLEntityViewSupport.createPaginatedSetting(dfe);

        Assert.assertEquals(new HashSet<>(Arrays.asList("owner.name")), paginatedSetting.getFetches());
    }

    @Test
    public void testConditionalFetchesInSetting() {
        GraphQLFieldDefinition rootFieldDefinition = makeFieldDefinition("getDocument", documentObjectType);
        DataFetchingFieldSelectionSet selectionSet =
                makeMockSelectionSet("Document", "name");

        DataFetchingEnvironment dfe = makeMockDataFetchingEnvironment(rootFieldDefinition, selectionSet);

        EntityViewSetting<DocumentView, CriteriaBuilder<DocumentView>> setting = graphQLEntityViewSupport.createSetting(dfe);

        Assert.assertEquals(new HashSet<>(Arrays.asList("id", "name")), setting.getFetches());
    }

    @Test
    public void testRootInheritanceInSelectionSet() {
        GraphQLFieldDefinition rootFieldDefinition = makeFieldDefinition( "getAnimal", animalInterfaceType );
        DataFetchingFieldSelectionSet selectionSet = makeMockSelectionSet("Cat", "Animal.name", "[Cat,Animal].__typename");

        DataFetchingEnvironment dfe = makeMockDataFetchingEnvironment(rootFieldDefinition, selectionSet);

        EntityViewSetting<AnimalView, CriteriaBuilder<AnimalView>> setting = graphQLEntityViewSupport.createSetting(dfe);

        Assert.assertEquals(new HashSet<>(Arrays.asList("id", "name")), setting.getFetches());
    }

    @Test
    public void testInheritanceParentInSelectionSet() {
        GraphQLFieldDefinition rootFieldDefinition = makeFieldDefinition("getPerson", personObjectType);
        DataFetchingFieldSelectionSet selectionSet = makeMockSelectionSet("Person", "name", "animal", "animal/Animal.name");

        DataFetchingEnvironment dfe = makeMockDataFetchingEnvironment(rootFieldDefinition, selectionSet);

        EntityViewSetting<PersonView, CriteriaBuilder<PersonView>> setting = graphQLEntityViewSupport.createSetting(dfe);

        Assert.assertEquals(new HashSet<>(Arrays.asList("name", "animal.name")), setting.getFetches());
    }

    @Test
    public void testNestedInheritanceInSelectionSet() {
        GraphQLFieldDefinition rootFieldDefinition = makeFieldDefinition("getPerson", personObjectType);
        DataFetchingFieldSelectionSet selectionSet = makeMockSelectionSet("Person", "name", "animal", "animal/Cat.name");

        DataFetchingEnvironment dfe = makeMockDataFetchingEnvironment(rootFieldDefinition, selectionSet);

        EntityViewSetting<PersonView, CriteriaBuilder<PersonView>> setting = graphQLEntityViewSupport.createSetting(dfe);

        Assert.assertEquals(new HashSet<>(Arrays.asList("name", "animal.name")), setting.getFetches());
    }
}
