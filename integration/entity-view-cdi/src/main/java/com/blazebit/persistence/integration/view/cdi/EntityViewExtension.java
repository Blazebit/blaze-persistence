/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.view.cdi;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewListener;
import com.blazebit.persistence.view.EntityViewListeners;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.WithAnnotations;
import java.lang.annotation.Annotation;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class EntityViewExtension implements Extension {

    private final EntityViewConfiguration configuration = EntityViews.createDefaultConfiguration();
    private final List<RuntimeException> exceptions = new ArrayList<>();

    <X> void processEntityView(@Observes @WithAnnotations({EntityView.class, EntityViewListener.class, EntityViewListeners.class}) ProcessAnnotatedType<X> pat) {
        if (pat.getAnnotatedType().isAnnotationPresent(EntityView.class)) {
            try {
                configuration.addEntityView(pat.getAnnotatedType().getJavaClass());
            } catch (RuntimeException ex) {
                exceptions.add(new IllegalArgumentException("Exception occurred while reading entity view class: " + pat.getAnnotatedType().getJavaClass().getName(), ex));
            }
        } else if (pat.getAnnotatedType().isAnnotationPresent(EntityViewListener.class) || pat.getAnnotatedType().isAnnotationPresent(EntityViewListeners.class)) {
            try {
                configuration.addEntityViewListener(pat.getAnnotatedType().getJavaClass());
            } catch (RuntimeException ex) {
                exceptions.add(new IllegalArgumentException("Exception occurred while reading entity view class: " + pat.getAnnotatedType().getJavaClass().getName(), ex));
            }
        }
    }
    
    void beforeBuild(@Observes AfterBeanDiscovery abd, BeanManager bm) {
        if (!exceptions.isEmpty()) {
            for (RuntimeException exception : exceptions) {
                abd.addDefinitionError(exception);
            }
            return;
        }
        Class<?> beanClass = EntityViewConfiguration.class;
        Class<?>[] types = new Class[] { EntityViewConfiguration.class, Object.class };
        Annotation[] qualifiers = new Annotation[] { new DefaultLiteral()};
        Class<? extends Annotation> scope = Dependent.class;
        Bean<EntityViewConfiguration> bean = new CustomBean<EntityViewConfiguration>(beanClass, types, qualifiers, scope, configuration);

        abd.addBean(bean);
    }
    
}
