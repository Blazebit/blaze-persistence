/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.CTE;
import com.blazebit.persistence.integration.hibernate.base.Database;
import com.blazebit.persistence.integration.hibernate.base.MultiIterator;
import com.blazebit.persistence.integration.hibernate.base.SimpleDatabase;
import com.blazebit.persistence.integration.hibernate.base.TableNameFormatter;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Table;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.6.7
 */
@ServiceProvider(Integrator.class)
public class Hibernate7Integrator implements Integrator {

    @Override
    public void integrate(Metadata metadata, BootstrapContext bootstrapContext, SessionFactoryImplementor sessionFactory) {
        List<PersistentClass> invalidPolymorphicCtes = new ArrayList<>();
        List<String> invalidFormulaCtes = new ArrayList<>();
        for (PersistentClass clazz : metadata.getEntityBindings()) {
            Class<?> entityClass = clazz.getMappedClass();
            
            if (entityClass != null && entityClass.isAnnotationPresent(CTE.class)) {
                if (clazz.isPolymorphic()) {
                    invalidPolymorphicCtes.add(clazz);
                }
                for (Property property : clazz.getSubclassPropertyClosure()) {
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

        ServiceRegistryImplementor serviceRegistry = sessionFactory.getServiceRegistry();
        TableNameFormatter formatter = new NativeTableNameFormatter(sessionFactory.getJdbcServices().getJdbcEnvironment().getQualifiedObjectNameFormatter());
        serviceRegistry.locateServiceBinding(Database.class).setService(new SimpleDatabase(getTableIterator(metadata.getDatabase().getNamespaces()), metadata.getDatabase().getTypeConfiguration(), sessionFactory.getJdbcServices().getDialect(), formatter, metadata));
    }

    private Iterator<Table> getTableIterator(Iterable<Namespace> namespaces) {
        List<Iterator<Table>> iterators = new ArrayList<>();
        Iterator<Namespace> namespaceIterator = namespaces.iterator();

        while (namespaceIterator.hasNext()) {
            iterators.add(namespaceIterator.next().getTables().iterator());
        }

        return new MultiIterator<>(iterators);
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
    }

}
