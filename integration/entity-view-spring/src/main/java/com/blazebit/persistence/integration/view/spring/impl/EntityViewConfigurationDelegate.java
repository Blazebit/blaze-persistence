/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.integration.view.spring.impl;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class EntityViewConfigurationDelegate {

    private final AbstractEntityViewConfigurationSource configurationSource;
    private final ResourceLoader resourceLoader;
    private final Environment environment;

    public EntityViewConfigurationDelegate(AbstractEntityViewConfigurationSource configurationSource, ResourceLoader resourceLoader, Environment environment) {
        this.configurationSource = configurationSource;
        this.resourceLoader = resourceLoader;
        this.environment = environment;
    }

    @SuppressWarnings("unchecked")
    void registerEntityViews(BeanDefinitionRegistry registry) {
        Set<Class<?>> entityViewClasses = new HashSet<>();
        for (BeanDefinition candidate : configurationSource.getCandidates(resourceLoader)) {
            try {
                entityViewClasses.add(Class.forName(candidate.getBeanClassName()));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        final String entityViewClassHolderBeanName = "entityViewConfigurationProducer";
        if (registry.containsBeanDefinition(entityViewClassHolderBeanName)) {
            BeanDefinition existingClassHolder = registry.getBeanDefinition(entityViewClassHolderBeanName);
            Set<Class<?>> existingEntityViewClasses = (Set<Class<?>>) ((GenericBeanDefinition) existingClassHolder).getConstructorArgumentValues().getGenericArgumentValue(Set.class).getValue();
            existingEntityViewClasses.addAll(entityViewClasses);
        } else {
            // register configuration class
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(EntityViewConfigurationProducer.class);
            beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(entityViewClasses);
            registry.registerBeanDefinition(entityViewClassHolderBeanName, beanDefinition);
        }
    }
}
