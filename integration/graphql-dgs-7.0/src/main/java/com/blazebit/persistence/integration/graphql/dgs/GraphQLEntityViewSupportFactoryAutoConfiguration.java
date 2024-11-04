/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql.dgs;

import com.blazebit.persistence.integration.graphql.GraphQLEntityViewSupportFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is the companion configuration for {@link BlazePersistenceDgsAutoConfiguration} and exposes the
 * {@link GraphQLEntityViewSupportFactory} as an optional bean, so it can be customized.
 *
 * @author Christian Beikov
 * @since 1.6.9
 */
@Configuration
public class GraphQLEntityViewSupportFactoryAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public GraphQLEntityViewSupportFactory graphQLEntityViewSupportFactory() {
        GraphQLEntityViewSupportFactory graphQLEntityViewSupportFactory = new GraphQLEntityViewSupportFactory(true, true);
        graphQLEntityViewSupportFactory.setImplementRelayNode(false);
        graphQLEntityViewSupportFactory.setDefineRelayNodeIfNotExist(true);
        graphQLEntityViewSupportFactory.setRegisterScalarTypeDefinitions(true);
        return graphQLEntityViewSupportFactory;
    }

}
