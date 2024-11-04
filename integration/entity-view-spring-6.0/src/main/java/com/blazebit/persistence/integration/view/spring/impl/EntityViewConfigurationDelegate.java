/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.view.spring.impl;

import com.blazebit.persistence.view.EntityView;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ClassUtils;

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
        Set<Class<?>> entityViewListenerClasses = new HashSet<>();
        for (BeanDefinition candidate : configurationSource.getCandidates(resourceLoader)) {
            try {
                Class<?> clazz = ClassUtils.forName( candidate.getBeanClassName(),  resourceLoader == null ? null : resourceLoader.getClassLoader() );
                if (clazz.isAnnotationPresent(EntityView.class)) {
                    entityViewClasses.add(clazz);
                } else {
                    entityViewListenerClasses.add(clazz);
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        final String entityViewClassHolderBeanName = "entityViewConfigurationProducer";
        if (registry.containsBeanDefinition(entityViewClassHolderBeanName)) {
            BeanDefinition existingClassHolder = registry.getBeanDefinition(entityViewClassHolderBeanName);
            Set<Class<?>> existingEntityViewClasses = (Set<Class<?>>) ((GenericBeanDefinition) existingClassHolder).getConstructorArgumentValues().getGenericArgumentValues().get(0).getValue();
            Set<Class<?>> existingEntityViewListenerClasses = (Set<Class<?>>) ((GenericBeanDefinition) existingClassHolder).getConstructorArgumentValues().getGenericArgumentValues().get(1).getValue();
            existingEntityViewClasses.addAll(entityViewClasses);
            existingEntityViewListenerClasses.addAll(entityViewListenerClasses);
        } else {
            // register configuration class
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(EntityViewConfigurationProducer.class);
            beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(entityViewClasses);
            beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(entityViewListenerClasses);
            registry.registerBeanDefinition(entityViewClassHolderBeanName, beanDefinition);
        }
    }
}
