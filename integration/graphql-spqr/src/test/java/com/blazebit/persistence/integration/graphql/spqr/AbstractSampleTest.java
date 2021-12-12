/*
 * Copyright 2014 - 2021 Blazebit.
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

package com.blazebit.persistence.integration.graphql.spqr;

import com.blazebit.persistence.integration.graphql.spqr.model.Cat;
import com.blazebit.persistence.integration.graphql.spqr.model.Person;
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
