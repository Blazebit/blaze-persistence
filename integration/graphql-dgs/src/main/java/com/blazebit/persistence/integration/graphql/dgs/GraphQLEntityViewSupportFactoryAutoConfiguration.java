/*
 * Copyright 2014 - 2024 Blazebit.
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
