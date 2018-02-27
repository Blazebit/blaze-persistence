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

package com.blazebit.persistence.integration.view.cdi;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import java.lang.annotation.Annotation;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@ServiceProvider(Extension.class)
public class EntityViewExtension implements Extension {

    private final EntityViewConfiguration configuration = EntityViews.createDefaultConfiguration();
    private final List<RuntimeException> exceptions = new ArrayList<>();

    <X> void processEntityView(@Observes ProcessAnnotatedType<X> pat) {
        if (pat.getAnnotatedType().isAnnotationPresent(EntityView.class)) {
            try {
                configuration.addEntityView(pat.getAnnotatedType().getJavaClass());
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
