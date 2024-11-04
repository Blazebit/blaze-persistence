/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.microprofile.graphql.repository;

import com.blazebit.persistence.examples.microprofile.graphql.model.Cat;
import com.blazebit.persistence.examples.microprofile.graphql.model.Person;
import org.junit.Before;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public abstract class AbstractSampleTest {

    @Inject
    DataInitializer dataInitializer;

    @Before
    public void init() {
        dataInitializer.run(em -> {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            List<Person> people = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                Person p = new Person("Person " + i);
                people.add(p);
                em.persist(p);
            }
            for (int i = 0; i < 100; i++) {
                Cat c = new Cat("Cat " + i, random.nextInt(20), people.get(random.nextInt(4)));
                em.persist(c);
            }
        });
    }

    @Transactional
    @ApplicationScoped
    public static class DataInitializer {

        @Inject
        EntityManager em;

        public void run(Consumer<EntityManager> c) {
            c.accept(em);
        }
    }
}
