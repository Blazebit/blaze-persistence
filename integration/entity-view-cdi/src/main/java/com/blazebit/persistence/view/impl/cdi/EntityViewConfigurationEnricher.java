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

import com.blazebit.persistence.spi.ConfigurationEnricher;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import org.apache.deltaspike.core.api.literal.DefaultLiteral;
import org.apache.deltaspike.core.util.bean.BeanBuilder;

/**
 *
 * @author cpbec
 */
public class EntityViewConfigurationEnricher implements ConfigurationEnricher<CriteriaBuilderConfiguration> {
    
    @Inject
    private BeanManager bm;

    @Inject
    private EntityViewExtension extension;
    
    @Inject
    @Any
    private Instance<ConfigurationEnricher<EntityViewConfiguration>> configurationEnrichers;
    
    @Override
    public void beforeBuild(CriteriaBuilderConfiguration config, AfterBeanDiscovery abd) {
        EntityViewConfiguration configuration = extension.getConfiguration();
        for (ConfigurationEnricher<EntityViewConfiguration> enricher : configurationEnrichers) {
            enricher.beforeBuild(configuration, abd);
        }
        
        final EntityViewManager entityViewManager = configuration.createEntityViewManager();
        Bean<EntityViewManager> bean = new BeanBuilder<EntityViewManager>(bm)
            .beanClass(EntityViewManager.class)
            .types(EntityViewManager.class, Object.class)
            .passivationCapable(false)
            .qualifiers(new DefaultLiteral())
            .scope(ApplicationScoped.class)
            .beanLifecycle(new EntityViewManagerLifecycle(entityViewManager))
            .create();
        
        abd.addBean(bean);
    }
}
