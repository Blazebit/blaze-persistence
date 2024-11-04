/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.spqr.config;

import com.blazebit.persistence.integration.graphql.GraphQLEntityViewSupport;
import com.blazebit.persistence.integration.graphql.GraphQLEntityViewSupportFactory;
import com.blazebit.persistence.view.EntityViewManager;
import graphql.schema.GraphQLSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;

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