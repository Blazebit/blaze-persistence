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
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
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
import org.junit.After;
import org.junit.Before;

public abstract class AbstractSampleTest {
    
    protected EntityManagerFactory emf;
    protected CriteriaBuilderFactory cbf;
    protected EntityViewManager evm;

    @Before
    public void init() {
        emf = Persistence.createEntityManagerFactory("default");
        CriteriaBuilderConfiguration config = Criteria.getDefault();
        cbf = config.createCriteriaBuilderFactory(emf);

        EntityViewConfiguration entityViewConfiguration = EntityViews.createDefaultConfiguration();

        for (Class<?> entityViewClazz : getEntityViewClasses()) {
            entityViewConfiguration.addEntityView(entityViewClazz);
        }

        evm = entityViewConfiguration.createEntityViewManager(cbf);
        
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

    protected abstract Class<?>[] getEntityViewClasses();
    
    protected void transactional(Consumer<EntityManager> consumer) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        boolean success = false;
        
        try {
            tx.begin();
            consumer.accept(em);
            success = true;
        } finally {
            try {
                if (success) {
                    tx.commit();
                } else {
                    tx.rollback();
                }
            } finally {
                em.close();
            }
        }
    }

    @After
    public void destruct() {
        emf.close();
    }
}
