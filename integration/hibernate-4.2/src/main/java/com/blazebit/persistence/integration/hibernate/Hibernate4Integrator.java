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

package com.blazebit.persistence.integration.hibernate;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.CTE;
import com.blazebit.persistence.integration.hibernate.base.Database;
import com.blazebit.persistence.integration.hibernate.base.SimpleDatabase;
import com.blazebit.persistence.integration.hibernate.base.SimpleTableNameFormatter;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.hql.spi.PersistentTableBulkIdStrategy;
import org.hibernate.hql.spi.TemporaryTableBulkIdStrategy;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.integrator.spi.ServiceContributingIntegrator;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.metamodel.spi.TypeContributions;
import org.hibernate.metamodel.spi.TypeContributor;
import org.hibernate.persister.spi.PersisterClassResolver;
import org.hibernate.service.Service;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.service.internal.StandardServiceRegistryImpl;
import org.hibernate.service.spi.BasicServiceInitiator;
import org.hibernate.service.spi.Configurable;
import org.hibernate.service.spi.ServiceBinding;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
@ServiceProvider(Integrator.class)
public class Hibernate4Integrator implements ServiceContributingIntegrator, TypeContributor, Configurable, Service {

    private static final Logger LOG = Logger.getLogger(Hibernate4Integrator.class.getName());
    private Map<String, Object> configuration;

    @Override
    public void configure(Map map) {
        this.configuration = map;
    }

    @Override
    public void contribute(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        ((StandardServiceRegistryImpl) serviceRegistry).configureService(new ServiceBinding<>((ServiceBinding.ServiceLifecycleOwner) serviceRegistry, Hibernate4Integrator.class, this));
        Object o = configuration.get("hibernate.hql.bulk_id_strategy");
        if (o == null) {
            serviceRegistry.getService(JdbcServices.class).getDialect().getDefaultProperties().put("hibernate.hql.bulk_id_strategy", new CustomMultiTableBulkIdStrategy(serviceRegistry.getService(JdbcServices.class).getDialect().supportsTemporaryTables() ? TemporaryTableBulkIdStrategy.INSTANCE : new PersistentTableBulkIdStrategy()));
        } else {
            LOG.warning("Can't replace hibernate.hql.bulk_id_strategy because it was overridden by the user with: " + o);
        }
    }

    @Override
    public void prepareServices(ServiceRegistryBuilder serviceRegistryBuilder) {
        serviceRegistryBuilder.addInitiator(new BasicServiceInitiator<Database>() {
            @Override
            public Database initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
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
        List<PersistentClass> invalidPolymorphicCtes = new ArrayList<>();
        List<String> invalidFormulaCtes = new ArrayList<>();
        while (iter.hasNext()) {
            PersistentClass clazz = iter.next();
            Class<?> entityClass = clazz.getMappedClass();
            if (valuesEntity.equals(entityClass)) {
                registerValuesEntity = false;
            }
            
            if (entityClass != null && entityClass.isAnnotationPresent(CTE.class)) {
                if (clazz.isPolymorphic()) {
                    invalidPolymorphicCtes.add(clazz);
                }
                Iterator<Property> iterator = clazz.getSubclassPropertyClosureIterator();
                while (iterator.hasNext()) {
                    Property property = iterator.next();
                    if (property.getValue().hasFormula()) {
                        invalidFormulaCtes.add(clazz.getClassName() + "#" + property.getName());
                    }
                }
                clazz.getTable().setSubselect("select * from " + clazz.getJpaEntityName());
            }
        }

        if (!invalidPolymorphicCtes.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Found invalid polymorphic CTE entity definitions. CTE entities may not extend other entities:");
            for (PersistentClass invalidPolymorphicCte : invalidPolymorphicCtes) {
                sb.append("\n - ").append(invalidPolymorphicCte.getMappedClass().getName());
            }

            throw new RuntimeException(sb.toString());
        }
        if (!invalidFormulaCtes.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Found uses of @Formula in CTE entity definitions. CTE entities can't use @Formula:");
            for (String invalidFormulaCte : invalidFormulaCtes) {
                sb.append("\n - ").append(invalidFormulaCte);
            }

            throw new RuntimeException(sb.toString());
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
