/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql.dgs.config;

import com.blazebit.persistence.integration.graphql.GraphQLEntityViewSupportFactory;
import com.blazebit.persistence.integration.graphql.dgs.BlazePersistenceDgsAutoConfiguration;
import com.blazebit.persistence.integration.graphql.dgs.GraphQLEntityViewSupportFactoryAutoConfiguration;
import com.blazebit.persistence.integration.graphql.dgs.view.CatUpdateView;
import com.blazebit.persistence.integration.graphql.dgs.view.PersonIdView;
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
public class GraphQLEntityViewSupportFactoryConfiguration extends GraphQLEntityViewSupportFactoryAutoConfiguration {
    @Bean
    public GraphQLEntityViewSupportFactory graphQLEntityViewSupportFactory() {
        GraphQLEntityViewSupportFactory graphQLEntityViewSupportFactory = super.graphQLEntityViewSupportFactory();
        graphQLEntityViewSupportFactory.setTypeInclusionPredicate(managedViewType -> {
            // Ignore an unneeded type
            return managedViewType.getJavaType() != CatUpdateView.class
            // Exclude a type that shall be discovered
                    && managedViewType.getJavaType() != PersonIdView.class;
        });
        return graphQLEntityViewSupportFactory;
    }

}
