/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql.spqr.config;

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

import jakarta.annotation.PostConstruct;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.6.4
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
}