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

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.impl.integration.cdi.CustomBean;
import com.blazebit.persistence.impl.integration.cdi.DefaultLiteral;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import java.lang.annotation.Annotation;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Singleton;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
@ServiceProvider(Extension.class)
public class CriteriaBuilderExtension implements Extension {

    private final CriteriaBuilderConfiguration configuration = Criteria.getDefault();

    void initializeEntityViewSystem(@Observes AfterBeanDiscovery abd, BeanManager bm) {
        bm.fireEvent(configuration);
        CriteriaBuilderFactory criteriaBuilderFactory = configuration.createCriteriaBuilderFactory();
        
        Class<?> beanClass = CriteriaBuilderFactory.class;
        Class<?>[] types = new Class[] { CriteriaBuilderFactory.class, Object.class };
        Annotation[] qualifiers = new Annotation[] { new DefaultLiteral()};
        Class<? extends Annotation> scope = Singleton.class;
        CriteriaBuilderFactory instance = criteriaBuilderFactory;
        Bean<CriteriaBuilderFactory> bean = new CustomBean<CriteriaBuilderFactory>(beanClass, types, qualifiers, scope, instance);

        abd.addBean(bean);
    }
}
