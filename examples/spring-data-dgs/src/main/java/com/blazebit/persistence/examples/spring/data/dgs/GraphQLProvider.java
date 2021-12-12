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

package com.blazebit.persistence.examples.spring.data.dgs;

import com.blazebit.persistence.integration.graphql.GraphQLEntityViewSupport;
import com.blazebit.persistence.integration.graphql.GraphQLEntityViewSupportFactory;
import com.blazebit.persistence.integration.jackson.EntityViewAwareObjectMapper;
import com.blazebit.persistence.view.EntityViewManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsFederationResolver;
import com.netflix.graphql.dgs.DgsTypeDefinitionRegistry;
import com.netflix.graphql.dgs.autoconfig.DgsConfigurationProperties;
import com.netflix.graphql.dgs.internal.DgsSchemaProvider;
import com.netflix.graphql.mocking.MockProvider;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.6.2
 */
@DgsComponent
public class GraphQLProvider {

    @Autowired
    EntityViewManager evm;

    private TypeDefinitionRegistry typeRegistry;
    private GraphQLEntityViewSupport graphQLEntityViewSupport;

    @PostConstruct
    public void init() {
        this.typeRegistry = new TypeDefinitionRegistry();
        GraphQLEntityViewSupportFactory graphQLEntityViewSupportFactory = new GraphQLEntityViewSupportFactory(true, true);
        graphQLEntityViewSupportFactory.setImplementRelayNode(false);
        graphQLEntityViewSupportFactory.setDefineRelayNodeIfNotExist(true);
        this.graphQLEntityViewSupport = graphQLEntityViewSupportFactory.create(typeRegistry, evm);
    }

    @DgsTypeDefinitionRegistry
    public TypeDefinitionRegistry registry() {
        return typeRegistry;
    }

    @Bean
    public GraphQLEntityViewSupport getSchema() {
        return graphQLEntityViewSupport;
    }


    @Bean
    public DgsSchemaProvider dgsSchemaProvider(
            ApplicationContext applicationContext,
            Optional<DgsFederationResolver> federationResolver,
            DataFetcherExceptionHandler dataFetcherExceptionHandler,
            Optional<TypeDefinitionRegistry> existingTypeDefinitionFactory,
            Optional<GraphQLCodeRegistry> existingCodeRegistry,
            Optional<Set<MockProvider>> mockProviders,
            DgsConfigurationProperties configProps
    ) {
        DgsSchemaProvider dgsSchemaProvider = new DgsSchemaProvider(
                applicationContext,
                federationResolver,
                existingTypeDefinitionFactory,
                mockProviders,
                configProps.getSchemaLocations()
        );
        // Jackson was replaced in 4.6.0+ so that's not going to work anymore..
        // See https://github.com/Netflix/dgs-framework/discussions/775
        try {
            Field field = dgsSchemaProvider.getClass().getDeclaredField("objectMapper");
            field.setAccessible(true);
            ObjectMapper objectMapper = (ObjectMapper) field.get(dgsSchemaProvider);
            new EntityViewAwareObjectMapper(evm, objectMapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return dgsSchemaProvider;
    }

}
