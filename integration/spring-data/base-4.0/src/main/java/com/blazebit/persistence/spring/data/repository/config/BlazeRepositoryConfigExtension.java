/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.repository.config;

import com.blazebit.persistence.spring.data.repository.EntityViewRepository;
import com.blazebit.persistence.spring.data.repository.EntityViewSpecificationExecutor;
import com.blazebit.persistence.view.EntityView;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.JpaRepositoryConfigExtension;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.RepositoryConfigurationSource;

import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Christian Beikov
 * @author Eugen Mayer
 * @since 1.6.9
 */
public class BlazeRepositoryConfigExtension extends JpaRepositoryConfigExtension {
    @Override
    public String getModuleName() {
        return "Blaze-Persistence";
    }

    public String getRepositoryFactoryClassName() {
        return getRepositoryFactoryBeanClassName();
    }

    public String getRepositoryFactoryBeanClassName() {
        return "com.blazebit.persistence.spring.data.impl.repository.BlazePersistenceRepositoryFactoryBean";
    }

    @Override
    public String getRepositoryBaseClassName() {
        return "com.blazebit.persistence.spring.data.impl.repository.EntityViewAwareRepositoryImpl";
    }

    @Override
    protected Collection<Class<? extends Annotation>> getIdentifyingAnnotations() {
        return Arrays.asList(Entity.class, MappedSuperclass.class, EntityView.class);
    }

    @Override
    protected Collection<Class<?>> getIdentifyingTypes() {
        return Arrays.asList(JpaRepository.class, EntityViewRepository.class, EntityViewSpecificationExecutor.class);
    }

    @Override
    public void registerBeansForRoot(BeanDefinitionRegistry registry, RepositoryConfigurationSource config) {
        AnnotationAttributes attributes = ((AnnotationRepositoryConfigurationSource) config).getAttributes();
        if (attributes.get("repositoryFactoryBeanClass") == void.class) {
            try {
                attributes.put("repositoryFactoryBeanClass", Class.forName(getRepositoryFactoryBeanClassName()));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        super.registerBeansForRoot(registry, config);
    }
}
