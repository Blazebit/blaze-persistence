/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence.view.impl.cdi;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.impl.integration.cdi.CustomBean;
import com.blazebit.persistence.impl.integration.cdi.DefaultLiteral;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import java.lang.annotation.Annotation;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.inject.Singleton;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
@ServiceProvider(Extension.class)
public class EntityViewExtension implements Extension {

    private final EntityViewConfiguration configuration = EntityViews.createDefaultConfiguration();

    <X> void processEntityView(@Observes ProcessAnnotatedType<X> pat) {
        if (pat.getAnnotatedType()
            .isAnnotationPresent(EntityView.class)) {
            configuration.addEntityView(pat.getAnnotatedType()
                .getJavaClass());
        }
    }

    void beforeBuild(@Observes AfterBeanDiscovery abd, BeanManager bm) {
        bm.fireEvent(configuration);
        final EntityViewManager entityViewManager = configuration.createEntityViewManager();
        
        Class<?> beanClass = EntityViewManager.class;
        Class<?>[] types = new Class[] { EntityViewManager.class, Object.class };
        Annotation[] qualifiers = new Annotation[] { new DefaultLiteral()};
        Class<? extends Annotation> scope = Singleton.class;
        EntityViewManager instance = entityViewManager;
        Bean<EntityViewManager> bean = new CustomBean<EntityViewManager>(beanClass, types, qualifiers, scope, instance);

        abd.addBean(bean);
    }
}
