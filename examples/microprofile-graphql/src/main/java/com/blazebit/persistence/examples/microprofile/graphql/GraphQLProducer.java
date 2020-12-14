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

package com.blazebit.persistence.examples.microprofile.graphql;

import com.blazebit.persistence.integration.graphql.GraphQLEntityViewSupport;
import com.blazebit.persistence.integration.graphql.GraphQLEntityViewSupportFactory;
import com.blazebit.persistence.view.EntityViewManager;
import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.scalar.GraphQLScalarTypes;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * @author Christian Beikov
 * @since 1.6.2
 */
@ApplicationScoped
public class GraphQLProducer {

    @Inject
    EntityViewManager evm;

    GraphQLEntityViewSupport graphQLEntityViewSupport;

    void configure(@Observes GraphQLSchema.Builder schemaBuilder) {
        // Option 1: As of SmallRye GraphQL 1.3.1 you can disable the generation of GraphQL types and annotate all entity views with @Type instead
        // boolean defineNormalTypes = false;
        // boolean defineRelayTypes = false;

        // Option 2: Let the integration replace the entity view GraphQL types
        boolean defineNormalTypes = true;
        boolean defineRelayTypes = true;

        // Configure how to integrate entity views
        GraphQLEntityViewSupportFactory graphQLEntityViewSupportFactory = new GraphQLEntityViewSupportFactory(defineNormalTypes, defineRelayTypes);

        graphQLEntityViewSupportFactory.setImplementRelayNode(false);
        graphQLEntityViewSupportFactory.setDefineRelayNodeIfNotExist(true);
        graphQLEntityViewSupportFactory.setScalarTypeMap(GraphQLScalarTypes.getScalarMap());
        this.graphQLEntityViewSupport = graphQLEntityViewSupportFactory.create(schemaBuilder, evm);
    }

    @Produces
    @ApplicationScoped
    GraphQLEntityViewSupport graphQLEntityViewSupport() {
        return graphQLEntityViewSupport;
    }
}
