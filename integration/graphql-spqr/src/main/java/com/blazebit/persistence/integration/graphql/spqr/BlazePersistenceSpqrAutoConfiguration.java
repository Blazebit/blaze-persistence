/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql.spqr;

import com.blazebit.persistence.integration.jackson.EntityViewAwareObjectMapper;
import com.blazebit.persistence.view.EntityViewManager;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.leangen.graphql.metadata.strategy.DefaultInclusionStrategy;
import io.leangen.graphql.metadata.strategy.InclusionStrategy;
import io.leangen.graphql.metadata.strategy.InputFieldInclusionParams;
import io.leangen.graphql.metadata.strategy.value.ValueMapperFactory;
import io.leangen.graphql.metadata.strategy.value.jackson.JacksonValueMapperFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.6.4
 */
@Configuration
public class BlazePersistenceSpqrAutoConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer blazePersistenceJsonCustomizer(EntityViewManager evm) {
        Module module = new Module() {
            @Override
            public String getModuleName() {
                return "com.blazebit.persistence";
            }

            @Override
            public Version version() {
                return Version.unknownVersion();
            }

            @Override
            public void setupModule(SetupContext context) {
                new EntityViewAwareObjectMapper(evm, context.getOwner());
            }
        };
        return builder -> builder.modules(module);
    }

    @Bean
    @ConditionalOnMissingBean
    public ValueMapperFactory valueMapperFactory(ObjectMapper objectMapper) {
        return JacksonValueMapperFactory.builder()
                .withPrototype(objectMapper)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
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