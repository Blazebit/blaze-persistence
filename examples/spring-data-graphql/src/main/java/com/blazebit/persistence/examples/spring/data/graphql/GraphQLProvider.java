/*
 * Copyright 2014 - 2022 Blazebit.
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

package com.blazebit.persistence.examples.spring.data.graphql;

import com.blazebit.persistence.examples.spring.data.graphql.repository.CatViewRepository;
import com.blazebit.persistence.examples.spring.data.graphql.view.CatWithOwnerView;
import com.blazebit.persistence.integration.graphql.GraphQLEntityViewSupport;
import com.blazebit.persistence.integration.graphql.GraphQLEntityViewSupportFactory;
import com.blazebit.persistence.integration.graphql.GraphQLRelayConnection;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.Sorters;
import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
@Component
public class GraphQLProvider {

    @Autowired
    EntityViewManager evm;
    @Autowired
    CatViewRepository repository;

    private GraphQLEntityViewSupport graphQLEntityViewSupport;
    private GraphQLSchema schema;
    private GraphQL graphQL;

    @PostConstruct
    public void init() throws IOException {
        ClassPathResource resource = new ClassPathResource("schema.graphqls");
        String sdl = FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream(), UTF_8));
        this.schema = buildSchema(sdl);
        this.graphQL = GraphQL.newGraphQL(schema).build();
    }

    private GraphQLSchema buildSchema(String sdl) {
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
        GraphQLEntityViewSupportFactory graphQLEntityViewSupportFactory = new GraphQLEntityViewSupportFactory(true, true);
        graphQLEntityViewSupportFactory.setImplementRelayNode(false);
        graphQLEntityViewSupportFactory.setDefineRelayNodeIfNotExist(true);
        this.graphQLEntityViewSupport = graphQLEntityViewSupportFactory.create(typeRegistry, evm);
        RuntimeWiring runtimeWiring = buildWiring();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }

    private RuntimeWiring buildWiring() {
        return RuntimeWiring.newRuntimeWiring()
            .type(TypeRuntimeWiring.newTypeWiring("Query")
                    .dataFetcher("catById", new DataFetcher() {
                        @Override
                        public Object get(DataFetchingEnvironment dataFetchingEnvironment) {
                            return repository.findById(graphQLEntityViewSupport.createSetting(dataFetchingEnvironment), Long.valueOf(dataFetchingEnvironment.getArgument("id")));
                        }
                    })
                    .dataFetcher("findAll", new DataFetcher() {
                        @Override
                        public Object get(DataFetchingEnvironment dataFetchingEnvironment) {
                            EntityViewSetting<CatWithOwnerView, ?> setting = graphQLEntityViewSupport.createPaginatedSetting(dataFetchingEnvironment);
                            setting.addAttributeSorter("id", Sorters.ascending());
                            if (setting.getMaxResults() == 0) {
                                return new GraphQLRelayConnection<>(Collections.emptyList());
                            }
                            return new GraphQLRelayConnection<>(repository.findAll(setting));
                        }
                    })
            )
            // Even though the CatWithOwnerView type will have a field for the name "theData",
            // the regular GraphQL Java runtime can't access the field through the "abc" method,
            // so we need to add dedicated DataFetcher here
            .type(TypeRuntimeWiring.newTypeWiring("CatWithOwnerView")
                      .dataFetcher("theData", new DataFetcher() {
                          @Override
                          public Object get(DataFetchingEnvironment dataFetchingEnvironment) {
                              Object source = dataFetchingEnvironment.getSource();
                              if (source instanceof CatWithOwnerView) {
                                  return ((CatWithOwnerView) source).abc();
                              }
                              return null;
                          }
                      })
            )
            .type(TypeRuntimeWiring.newTypeWiring("CatWithOwnerViewNode")
                      .dataFetcher("theData", new DataFetcher() {
                          @Override
                          public Object get(DataFetchingEnvironment dataFetchingEnvironment) {
                              Object source = dataFetchingEnvironment.getSource();
                              if (source instanceof CatWithOwnerView) {
                                  return ((CatWithOwnerView) source).abc();
                              }
                              return null;
                          }
                      })
            )
            .build();
    }

    @Bean
    public GraphQLSchema getSchema() {
        return schema;
    }

    @Bean
    public GraphQL graphQL() {
        return graphQL;
    }
}
