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
import com.blazebit.persistence.integration.hibernate.base.MultiIterator;
import com.blazebit.persistence.integration.hibernate.base.SimpleDatabase;
import com.blazebit.persistence.integration.hibernate.base.TableNameFormatter;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Table;
import org.hibernate.persister.spi.PersisterClassResolver;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
@ServiceProvider(Integrator.class)
public class Hibernate53Integrator implements Integrator {

    @Override
    public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
        // TODO: remember metadata for exact column types
        for (PersistentClass clazz : metadata.getEntityBindings()) {
            Class<?> entityClass = clazz.getMappedClass();
            
            if (entityClass != null && entityClass.isAnnotationPresent(CTE.class)) {
                clazz.getTable().setSubselect("select * from " + clazz.getJpaEntityName());
            }
        }

        serviceRegistry.locateServiceBinding(PersisterClassResolver.class).setService(new CustomPersisterClassResolver());
        TableNameFormatter formatter = new NativeTableNameFormatter(sessionFactory.getJdbcServices().getJdbcEnvironment().getQualifiedObjectNameFormatter());
        serviceRegistry.locateServiceBinding(Database.class).setService(new SimpleDatabase(getTableIterator(metadata.getDatabase().getNamespaces()), sessionFactory.getDialect(), formatter, metadata));
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
