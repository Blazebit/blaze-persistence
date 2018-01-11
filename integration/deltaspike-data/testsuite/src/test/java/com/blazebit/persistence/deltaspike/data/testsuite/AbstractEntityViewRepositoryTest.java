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

package com.blazebit.persistence.deltaspike.data.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.deltaspike.data.testsuite.entity.Address;
import com.blazebit.persistence.deltaspike.data.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.base.AbstractPersistenceTest;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.metamodel.ViewType;
import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.hibernate.engine.spi.SessionImplementor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class AbstractEntityViewRepositoryTest extends AbstractPersistenceTest {

    private static final Logger LOG = Logger.getLogger(AbstractEntityViewRepositoryTest.class.getName());

    @Inject
    private EntityViewManager evm;

    Person[] persons;

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
                Person.class,
                Address.class
        };
    }

    @BeforeClass
    public static void bootContainer() {
        CdiContainer container = CdiContainerLoader.getCdiContainer();
        container.boot();
    }

    @AfterClass
    public static void shutdownContainer() {
        CdiContainer container = CdiContainerLoader.getCdiContainer();
        container.shutdown();
    }

    @Before
    public void startContexts() {
        CdiContainer container = CdiContainerLoader.getCdiContainer();
        container.getContextControl().startContexts();
        BeanProvider.injectFields(this);
    }

    @After
    public void stopContexts() {
        CdiContainer container = CdiContainerLoader.getCdiContainer();
        container.getContextControl().stopContexts();
    }

    @Override
    protected Connection getConnection(EntityManager em) {
        return em.unwrap(SessionImplementor.class).connection();
    }

    @Override
    protected boolean runTestInTransaction() {
        return false;
    }

    @Override
    public void init() {
        super.init();
        persons = cbf.create(em, Person.class)
                .orderByAsc("id")
                .getResultList().toArray(new Person[0]);
    }

    @Override
    protected void setUpOnce() {
        transactional(new Runnable() {
            @Override
            public void run() {
                createTestData();
            }
        });
    }

    protected void transactional(Runnable r) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            r.run();
            tx.commit();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
            if (tx != null) {
                tx.rollback();
            }
        }
    }

    private void createTestData() {
        Person[] persons = new Person[] {
                new Person(0L, "Mother", 0),
                new Person(1L, "John Doe", 2),
                new Person(2L, "James Harley", 4),
                new Person(3L, "Berry Cooper", 5),
                new Person(4L, "John Smith", 4, "King Street"),
                new Person(5L, "Harry Norman", 1, "Rich Street"),
                new Person(6L, "Harry Norman", 3, "King Street")
        };

        persons[1].setParent(persons[0]);
        persons[2].setParent(persons[0]);
        persons[3].setParent(persons[1]);
        persons[4].setParent(persons[2]);
        persons[5].setParent(persons[2]);
        persons[6].setParent(persons[2]);

        for (Person person : persons) {
            em.persist(person);
        }
    }

    protected <T> T fetch(Class<T> entityView, Object id) {
        ViewType<T> viewType = evm.getMetamodel().view(entityView);
        Class<?> entityClass = viewType.getEntityClass();
        CriteriaBuilder<?> cb = cbf.create(em, entityClass).where(viewType.getIdAttribute().getName()).eq(id);
        return evm.applySetting(EntityViewSetting.create(entityView), cb).getSingleResult();
    }
}
