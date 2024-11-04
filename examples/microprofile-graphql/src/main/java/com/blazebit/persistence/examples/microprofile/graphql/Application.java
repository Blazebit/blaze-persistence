/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.microprofile.graphql;

import com.blazebit.persistence.examples.microprofile.graphql.model.Cat;
import com.blazebit.persistence.examples.microprofile.graphql.model.Person;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Christian Beikov
 * @since 1.6.2
 */
@ApplicationScoped
public class Application {

    @Inject
    EntityManager em;

    @Transactional
    void onStart(@Observes @Initialized(ApplicationScoped.class) Object ev) {
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
    }
}
