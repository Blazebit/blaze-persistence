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

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Christian Beikov
 * @since 1.6.8
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
