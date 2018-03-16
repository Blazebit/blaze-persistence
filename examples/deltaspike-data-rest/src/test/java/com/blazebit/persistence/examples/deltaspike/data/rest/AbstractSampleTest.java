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

package com.blazebit.persistence.examples.deltaspike.data.rest;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.examples.deltaspike.data.rest.model.Cat;
import com.blazebit.persistence.examples.deltaspike.data.rest.model.Person;
import com.blazebit.persistence.examples.deltaspike.data.rest.repository.CatJpaRepository;
import com.blazebit.persistence.view.EntityViewManager;
import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.function.Consumer;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractSampleTest {

    @Inject
    private EntityManagerFactoryHolder emfHolder;
    @Inject
    private EntityManager em;
    @Inject
    private CatJpaRepository catJpaRepository;
    @Inject
    protected CriteriaBuilderFactory cbf;
    @Inject
    protected EntityViewManager evm;

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

    @Before
    public void init() {
        startContexts();
        transactional(em -> {
            Person p1 = new Person("P1");
            Person p2 = new Person("P2");
            Person p3 = new Person("P3");
            em.persist(p1);
            em.persist(p2);
            em.persist(p3);
            
            Cat c1 = new Cat("C1", 1, p2);
            Cat c2 = new Cat("C2", 2, p2);
            Cat c3 = new Cat("C3", 4, p2);
            
            Cat c4 = new Cat("C4", 6, p3);
            
            Cat c5 = new Cat("C5", 8, null);
            Cat c6 = new Cat("C6", 7, null);
            
            catJpaRepository.save(c1);
            catJpaRepository.save(c2);
            catJpaRepository.save(c3);
            catJpaRepository.save(c4);
            catJpaRepository.save(c5);
            catJpaRepository.save(c6);
            
            c1.setMother(c3);
            c3.getKittens().add(c1);
            
            c1.setFather(c5);
            c5.getKittens().add(c1);
            
            c2.setMother(c3);
            c3.getKittens().add(c2);
            
            c2.setFather(c6);
            c6.getKittens().add(c2);
            
            c4.setFather(c6);
            c6.getKittens().add(c4);
        });
    }

    protected void transactional(Consumer<EntityManager> consumer) {
        EntityTransaction tx = em.getTransaction();
        boolean success = false;
        
        try {
            tx.begin();
            consumer.accept(em);
            success = true;
        } finally {
            if (success) {
                tx.commit();
            } else {
                tx.rollback();
            }
        }
    }
}
