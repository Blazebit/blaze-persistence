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

package com.blazebit.persistence.impl.cdi;

import com.blazebit.persistence.spi.ConfigurationEnricher;
import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.inject.Inject;
import org.apache.deltaspike.core.api.literal.DefaultLiteral;
import org.apache.deltaspike.core.util.bean.BeanBuilder;

/**
 *
 * @author cpbec
 */
@ServiceProvider(Extension.class)
public class CriteriaBuilderExtension implements Extension {
    
    private final CriteriaBuilderConfiguration configuration = Criteria.getDefault();
    
    void initializeEntityViewSystem(@Observes AfterBeanDiscovery abd, BeanManager bm, @Any Instance<ConfigurationEnricher<CriteriaBuilderConfiguration>> configurationEnrichers) {
        for (ConfigurationEnricher<CriteriaBuilderConfiguration> enricher : configurationEnrichers) {
            enricher.beforeBuild(configuration, abd);
        }
        
        CriteriaBuilderFactory criteriaBuilderFactory = configuration.createCriteriaBuilderFactory();
        Bean<CriteriaBuilderFactory> bean = new BeanBuilder<CriteriaBuilderFactory>(bm)
            .beanClass(CriteriaBuilderFactory.class)
            .types(CriteriaBuilderFactory.class, Object.class)
            .passivationCapable(false)
            .qualifiers(new DefaultLiteral())
            .scope(ApplicationScoped.class)
            .beanLifecycle(new CriteriaBuilderFactoryLifecycle(criteriaBuilderFactory))
            .create();
        
        abd.addBean(bean);
    }
}
