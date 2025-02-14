/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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

import jakarta.enterprise.inject.Default;
import jakarta.persistence.EntityManagerFactory;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@Recorder
public class EntityViewRecorder {

    public Supplier<CriteriaBuilderFactory> criteriaBuilderFactorySupplier(BlazePersistenceConfiguration blazePersistenceConfig, String blazePersistenceInstanceName, String persistenceUnitName) {
        return () -> {
            CriteriaBuilderConfiguration criteriaBuilderConfiguration = Criteria.getDefault();
            blazePersistenceConfig.blazePersistenceInstances().get(blazePersistenceInstanceName).apply(criteriaBuilderConfiguration);
            Annotation[] cbfQualifiers;
            if (BlazePersistenceInstanceUtil.isDefaultBlazePersistenceInstance(blazePersistenceInstanceName)) {
                cbfQualifiers = new Annotation[] { new Default.Literal() };
            } else {
                cbfQualifiers = new Annotation[] { new BlazePersistenceInstance.BlazePersistenceInstanceLiteral(blazePersistenceInstanceName) };
            }

            Arc.container().beanManager().getEvent().select(CriteriaBuilderConfiguration.class, cbfQualifiers).fire(criteriaBuilderConfiguration);
            EntityManagerFactory emf = Arc.container().instance(JPAConfig.class, new Annotation[0]).get().getEntityManagerFactory(persistenceUnitName);
            return criteriaBuilderConfiguration.createCriteriaBuilderFactory(emf);
        };
    }

    public Supplier<EntityViewManager> entityViewManagerSupplier(BlazePersistenceConfiguration blazePersistenceConfig,
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
            blazePersistenceConfig.blazePersistenceInstances().get(blazePersistenceInstanceName).apply(entityViewConfiguration);
            entityViewConfiguration.setProperty(ConfigurationProperties.PROXY_UNSAFE_ALLOWED, Boolean.FALSE.toString());

            Annotation[] cbfQualifiers;
            if (BlazePersistenceInstanceUtil.isDefaultBlazePersistenceInstance(blazePersistenceInstanceName)) {
                cbfQualifiers = new Annotation[] { new Default.Literal() };
            } else {
                cbfQualifiers = new Annotation[] { new BlazePersistenceInstance.BlazePersistenceInstanceLiteral(blazePersistenceInstanceName) };
            }

            Arc.container().beanManager().getEvent().select(EntityViewConfiguration.class, cbfQualifiers).fire(entityViewConfiguration);
            CriteriaBuilderFactory cbf = Arc.container().instance(CriteriaBuilderFactory.class, cbfQualifiers).get();
            return entityViewConfiguration.createEntityViewManager(cbf);
        };
    }
}
