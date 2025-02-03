/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.graphql.repository;

import com.blazebit.persistence.examples.spring.data.graphql.model.Cat;
import com.blazebit.persistence.examples.spring.data.graphql.model.Human;
import com.blazebit.persistence.examples.spring.data.graphql.model.Person;

import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public abstract class AbstractSampleTest {

    @Autowired
    DataInitializer dataInitializer;
    private boolean ran;

    @Before
    public void init() {
        dataInitializer.run(em -> {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            List<Person> people = new ArrayList<>();
            List<Human> humans = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                Person p = new Person("Person " + i);
                people.add(p);
                em.persist(p);
                Human h = new Human( "Person " + i);
                humans.add(h);
                em.persist(h);
            }
            for (int i = 0; i < 100; i++) {
                int personIdx = random.nextInt(4);
                Cat c = new Cat("Cat " + i, random.nextInt(20), people.get(personIdx), humans.get(personIdx));
                em.persist(c);
            }
        });
    }

    @After
    public void cleanup() {
        dataInitializer.run(em -> {
            em.createQuery("delete from Cat").executeUpdate();
            em.createQuery("delete from Person").executeUpdate();
            em.createQuery("delete from Human").executeUpdate();
        });
    }

    @Transactional
    @Component
    public static class DataInitializer {

        @Autowired
        EntityManager em;

        public void run(Consumer<EntityManager> c) {
            c.accept(em);
        }
    }
}
