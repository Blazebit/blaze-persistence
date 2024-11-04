/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.hateoas.webmvc.config;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.spi.type.TypeConverter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import java.lang.reflect.Type;
import java.util.UUID;

/**
 * @author Moritz Becker
 * @author Eugen Mayer
 * @since 1.6.9
 */
@Configuration
public class BlazePersistenceConfiguration {

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    @Lazy(false)
    public CriteriaBuilderFactory createCriteriaBuilderFactory() {
        CriteriaBuilderConfiguration config = Criteria.getDefault();
        return config.createCriteriaBuilderFactory(entityManagerFactory);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    @Lazy(false)
    public EntityViewManager createEntityViewManager(CriteriaBuilderFactory cbf, EntityViewConfiguration entityViewConfiguration) {
        entityViewConfiguration.registerTypeConverter(String.class, UUID.class, new TypeConverter<String, UUID>() {
            @Override
            public Class<?> getUnderlyingType(Class<?> owningClass, Type declaredType) {
                return String.class;
            }

            @Override
            public UUID convertToViewType(String object) {
                return object == null ? null : UUID.fromString(object);
            }

            @Override
            public String convertToUnderlyingType(UUID object) {
                return object == null ? null : object.toString();
            }
        });
        return entityViewConfiguration.createEntityViewManager(cbf);
    }
}
