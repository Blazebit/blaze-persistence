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

package com.blazebit.persistence.view.impl;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewManagerFactory;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

/**
 *
 * @author cpbec
 */
@ServiceProvider(Extension.class)
public class EntityViewExtension implements Extension {
    
    private static final EntityViewConfiguration configuration = new EntityViewConfiguration();
    private static EntityViewManagerFactory entityViewManagerFactory;
    
    <X> void processEntityView(@Observes ProcessAnnotatedType<X> pat) {
        if (pat.getAnnotatedType().isAnnotationPresent(EntityView.class)) {
            configuration.addEntityView(pat.getAnnotatedType().getJavaClass());
        }
    }
    
    void initializeEntityViewSystem(@Observes AfterBeanDiscovery abd) {
        entityViewManagerFactory = configuration.createEntityViewManagerFactory();
    }
    
    public static EntityViewManagerFactory getEntityViewManagerFactory() {
        return entityViewManagerFactory;
    }
}
