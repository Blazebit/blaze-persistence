/*
 * Copyright 2014 - 2023 Blazebit.
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

import com.blazebit.persistence.integration.graphql.GraphQLEntityViewSupport;
import com.blazebit.persistence.integration.graphql.GraphQLEntityViewSupportFactory;
import com.blazebit.persistence.integration.graphql.dgs.converter.ByteInputIdConverter;
import com.blazebit.persistence.integration.graphql.dgs.converter.IntegerInputIdConverter;
import com.blazebit.persistence.integration.graphql.dgs.converter.LongInputIdConverter;
import com.blazebit.persistence.integration.graphql.dgs.converter.ShortInputIdConverter;
import com.blazebit.persistence.integration.graphql.dgs.converter.StringInputIdConverter;
import com.blazebit.persistence.integration.graphql.dgs.converter.UUIDInputIdConverter;
import com.blazebit.persistence.integration.graphql.dgs.mapper.EntityViewInputObjectMapper;
import com.blazebit.persistence.view.EntityViewManager;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsTypeDefinitionRegistry;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * The following is a bit against the <a href="https://netflix.github.io/dgs/getting-started/#creating-a-schema">schema first DGS principle</a>
 * but we do it anyway since it's a lot easier to work this way than to define types twice and try to make sure they match up.
 *
 * @author Christian Beikov
 * @since 1.6.9
 */
@Configuration
@DgsComponent
@Import({
    EntityViewInputObjectMapper.class,
    ByteInputIdConverter.class,
    ShortInputIdConverter.class,
    IntegerInputIdConverter.class,
    LongInputIdConverter.class,
    UUIDInputIdConverter.class,
    StringInputIdConverter.class
})
@ImportAutoConfiguration(GraphQLEntityViewSupportFactoryAutoConfiguration.class)
public class BlazePersistenceDgsAutoConfiguration {
    private GraphQLEntityViewSupport graphQLEntityViewSupport;
    private TypeDefinitionRegistry typeRegistry;

    /**
     * The constructor creates the {@link TypeDefinitionRegistry} that is later exposed via @DgsTypeDefinitionRegistry.
     * It is done here since we have a ordering constraint, graphQLEntityViewSupportFactory need to fill it in its
     * create method while creating the {@link GraphQLEntityViewSupport} that is later exposed as bean.
     */
    public BlazePersistenceDgsAutoConfiguration(EntityViewManager evm, GraphQLEntityViewSupportFactory graphQLEntityViewSupportFactory) {
        this.typeRegistry = new TypeDefinitionRegistry();
        this.graphQLEntityViewSupport = graphQLEntityViewSupportFactory.create(typeRegistry, evm);
    }

    @Bean
    public GraphQLEntityViewSupport getSchema() {
        return graphQLEntityViewSupport;
    }

    @SuppressWarnings("unused")
    @DgsTypeDefinitionRegistry
    public TypeDefinitionRegistry registry() {
        return typeRegistry;
    }


}