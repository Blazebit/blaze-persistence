/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package ${package}.sample;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.EntityViewManager;
import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import java.util.function.Consumer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import ${package}.model.Cat;
import ${package}.model.Person;
import ${package}.view.CatSimpleView;
import ${package}.view.CatWithOwnerView;
import ${package}.view.PersonSimpleView;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractSampleTest {

    @Inject
    private EntityManagerFactoryHolder emfHolder;
    @Inject
    private EntityManager em;
    @Inject
    protected CriteriaBuilderFactory cbf;
    @Inject
    protected EntityViewManager evm;

    @BeforeAll
    public static void bootContainer() {
        CdiContainer container = CdiContainerLoader.getCdiContainer();
        container.boot();
    }

    @AfterAll
    public static void shutdownContainer() {
        CdiContainer container = CdiContainerLoader.getCdiContainer();
        container.shutdown();
    }

    public void startContexts() {
        CdiContainer container = CdiContainerLoader.getCdiContainer();
        container.getContextControl().startContexts();
        BeanProvider.injectFields(this);
    }

    @AfterEach
    public void stopContexts() {
        CdiContainer container = CdiContainerLoader.getCdiContainer();
        container.getContextControl().stopContexts();
    }

    @BeforeEach
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
            
            em.persist(c1);
            em.persist(c2);
            em.persist(c3);
            em.persist(c4);
            em.persist(c5);
            em.persist(c6);
            
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
