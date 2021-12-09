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

package com.blazebit.persistence.examples.spring.data.spqr.config;

import com.blazebit.persistence.integration.graphql.GraphQLEntityViewSupport;
import com.blazebit.persistence.integration.graphql.GraphQLEntityViewSupportFactory;
import com.blazebit.persistence.integration.jackson.EntityViewAwareObjectMapper;
import com.blazebit.persistence.view.EntityViewManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.schema.GraphQLSchema;
import io.leangen.graphql.metadata.strategy.DefaultInclusionStrategy;
import io.leangen.graphql.metadata.strategy.InclusionStrategy;
import io.leangen.graphql.metadata.strategy.InputFieldInclusionParams;
import io.leangen.graphql.metadata.strategy.value.ValueMapperFactory;
import io.leangen.graphql.metadata.strategy.value.jackson.JacksonValueMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
@Configuration
public class SpqrConfiguration {

    @Autowired
    EntityViewManager evm;
    @Autowired
    GraphQLSchema graphQLSchema;

    private GraphQLEntityViewSupport graphQLEntityViewSupport;

    @PostConstruct
    public void init() {
        GraphQLEntityViewSupportFactory graphQLEntityViewSupportFactory = new GraphQLEntityViewSupportFactory(false, false);
        graphQLEntityViewSupportFactory.setImplementRelayNode(false);
        graphQLEntityViewSupportFactory.setDefineRelayNodeIfNotExist(false);
        this.graphQLEntityViewSupport = graphQLEntityViewSupportFactory.create(graphQLSchema, evm);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    @Lazy(false)
    public GraphQLEntityViewSupport graphQLEntityViewSupport() {
        return graphQLEntityViewSupport;
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder.postConfigurer(objectMapper -> new EntityViewAwareObjectMapper(evm, objectMapper));
    }

    @Bean
    public ValueMapperFactory valueMapperFactory(ObjectMapper objectMapper) {
        return JacksonValueMapperFactory.builder()
                .withPrototype(objectMapper)
                .build();
    }

    @Bean
    public InclusionStrategy inclusionStrategy() {
        return new DefaultInclusionStrategy() {
            @Override
            public boolean includeInputField(InputFieldInclusionParams params) {
                if (params.getElements().stream().noneMatch(this::isIgnored) && isPackageAcceptable(params.getDeclaringType(), params.getElementDeclaringClass())) {
                    if (params.isDirectlyDeserializable() || params.isDeserializableInSubType()) {
                        return true;
                    }
                    // Always include collections even if there is no setter available
                    for (AnnotatedElement element : params.getElements()) {
                        if (element instanceof Method) {
                            Class<?> returnType = ((Method) element).getReturnType();
                            if (Collection.class.isAssignableFrom(returnType) || Map.class.isAssignableFrom(returnType)) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
        };
    }
}