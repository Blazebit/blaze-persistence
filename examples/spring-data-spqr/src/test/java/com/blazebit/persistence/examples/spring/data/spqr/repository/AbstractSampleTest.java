/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.spqr.repository;

import com.blazebit.persistence.examples.spring.data.spqr.model.Boy;
import com.blazebit.persistence.examples.spring.data.spqr.model.Cat;
import com.blazebit.persistence.examples.spring.data.spqr.model.Girl;
import com.blazebit.persistence.examples.spring.data.spqr.model.Person;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
            List<Person> people = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                Boy b = new Boy("Boy " + i);
                em.persist(b);
                Girl g = new Girl("Girl " + i, "Doll " + i);
                em.persist(g);
                Person p = new Person("Person " + i, new HashSet<>(Arrays.asList(b, g)));
                people.add(p);
                em.persist(p);
            }
            for (int i = 0; i < 100; i++) {
                Cat c = new Cat("Cat " + i, i % 20, people.get(i % 4));
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
