/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql.spqr;

import com.blazebit.persistence.integration.graphql.spqr.model.Cat;
import com.blazebit.persistence.integration.graphql.spqr.model.Person;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * @author Christian Beikov
 * @since 1.6.4
 */
public abstract class AbstractSampleTest {

    @Autowired
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
    @Component
    public static class DataInitializer {

        @Autowired
        EntityManager em;

        public void run(Consumer<EntityManager> c) {
            c.accept(em);
        }
    }
}
