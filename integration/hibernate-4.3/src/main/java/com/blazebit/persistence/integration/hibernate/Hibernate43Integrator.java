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

package com.blazebit.persistence.integration.hibernate;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.CTE;
import com.blazebit.persistence.integration.hibernate.base.Database;
import com.blazebit.persistence.integration.hibernate.base.SimpleDatabase;
import com.blazebit.persistence.integration.hibernate.base.SimpleTableNameFormatter;
import org.hibernate.boot.registry.StandardServiceInitiator;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.integrator.spi.ServiceContributingIntegrator;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.persister.spi.PersisterClassResolver;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import java.util.Iterator;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
@ServiceProvider(Integrator.class)
public class Hibernate43Integrator implements ServiceContributingIntegrator {

    @Override
    public void prepareServices(StandardServiceRegistryBuilder serviceRegistryBuilder) {
        serviceRegistryBuilder.addInitiator(new StandardServiceInitiator<Database>() {
            @Override
            public Database initiateService(@SuppressWarnings("rawtypes") Map configurationValues, ServiceRegistryImplementor registry) {
                return null;
            }

            @Override
            public Class<Database> getServiceInitiated() {
                return Database.class;
            }
        });
    }

    @Override
    public void integrate(Configuration configuration, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
        Class<?> valuesEntity;
        boolean registerValuesEntity = true;
        try {
            valuesEntity = Class.forName("com.blazebit.persistence.impl.function.entity.ValuesEntity");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Are you missing blaze-persistence-core-impl on the classpath?", e);
        }

        Iterator<PersistentClass> iter = configuration.getClassMappings();
        while (iter.hasNext()) {
            PersistentClass clazz = iter.next();
            Class<?> entityClass = clazz.getMappedClass();
            
            if (entityClass != null && entityClass.isAnnotationPresent(CTE.class)) {
                clazz.getTable().setSubselect("select * from " + clazz.getJpaEntityName());
            }
        }

        if (registerValuesEntity) {
            // Register values entity if wasn't found
            configuration.addAnnotatedClass(valuesEntity);
            configuration.buildMappings();
            PersistentClass clazz = configuration.getClassMapping(valuesEntity.getName());
            clazz.getTable().setSubselect("select * from " + clazz.getJpaEntityName());
        }

        serviceRegistry.locateServiceBinding(PersisterClassResolver.class).setService(new CustomPersisterClassResolver());
        serviceRegistry.locateServiceBinding(Database.class).setService(new SimpleDatabase(configuration.getTableMappings(), sessionFactory.getDialect(), new SimpleTableNameFormatter(), configuration.buildMapping()));
    }

    @Override
    public void integrate(MetadataImplementor metadata, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
    }

}
