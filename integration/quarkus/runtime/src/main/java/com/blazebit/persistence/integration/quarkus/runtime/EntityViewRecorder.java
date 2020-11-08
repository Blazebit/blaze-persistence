/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.integration.quarkus.runtime;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.view.ConfigurationProperties;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import io.quarkus.arc.Arc;
import io.quarkus.hibernate.orm.runtime.JPAConfig;
import io.quarkus.runtime.annotations.Recorder;

import javax.enterprise.inject.Default;
import javax.persistence.EntityManagerFactory;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@Recorder
public class EntityViewRecorder {

    public Supplier<CriteriaBuilderFactory> criteriaBuilderFactorySupplier(BlazePersistenceInstanceConfiguration blazePersistenceConfig, String persistenceUnitName) {
        return () -> {
            CriteriaBuilderConfiguration criteriaBuilderConfiguration = Criteria.getDefault();
            blazePersistenceConfig.apply(criteriaBuilderConfiguration);
            EntityManagerFactory emf = Arc.container().instance(JPAConfig.class, new Annotation[0]).get().getEntityManagerFactory(persistenceUnitName);
            return criteriaBuilderConfiguration.createCriteriaBuilderFactory(emf);
        };
    }

    public Supplier<EntityViewManager> entityViewManagerSupplier(BlazePersistenceInstanceConfiguration blazePersistenceConfig,
                                                                 String blazePersistenceInstanceName,
                                                                 Set<String> entityViewClasses,
                                                                 Set<String> entityViewListenerClasses) {
        return () -> {
            EntityViewConfiguration entityViewConfiguration = EntityViews.createDefaultConfiguration();
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            for (String entityViewClass : entityViewClasses) {
                try {
                    entityViewConfiguration.addEntityView(
                        classLoader.loadClass(entityViewClass)
                    );
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            for (String entityViewListenerClass : entityViewListenerClasses) {
                try {
                    entityViewConfiguration.addEntityViewListener(
                            classLoader.loadClass(entityViewListenerClass)
                    );
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            blazePersistenceConfig.apply(entityViewConfiguration);
            entityViewConfiguration.setProperty(ConfigurationProperties.PROXY_UNSAFE_ALLOWED, Boolean.FALSE.toString());

            Annotation[] cbfQualifiers;
            if (BlazePersistenceInstanceUtil.isDefaultBlazePersistenceInstance(blazePersistenceInstanceName)) {
                cbfQualifiers = new Annotation[] { new Default.Literal() };
            } else {
                cbfQualifiers = new Annotation[] { new BlazePersistenceInstance.BlazePersistenceInstanceLiteral(blazePersistenceInstanceName) };
            }

            CriteriaBuilderFactory cbf = Arc.container().instance(CriteriaBuilderFactory.class, cbfQualifiers).get();
            return entityViewConfiguration.createEntityViewManager(cbf);
        };
    }
}
