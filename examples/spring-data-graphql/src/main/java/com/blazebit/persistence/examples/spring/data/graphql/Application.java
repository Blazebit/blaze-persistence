/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.graphql;

import com.blazebit.persistence.examples.spring.data.graphql.model.Cat;
import com.blazebit.persistence.examples.spring.data.graphql.model.Human;
import com.blazebit.persistence.examples.spring.data.graphql.model.Person;
import com.blazebit.persistence.examples.spring.data.graphql.repository.CatJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
@SpringBootApplication
@EnableSpringDataWebSupport
@ComponentScan(basePackages = "com.blazebit.persistence.examples")
public class Application {

    @Autowired
    EntityManager em;
    @Autowired
    CatJpaRepository catJpaRepository;

    public static void main(String[] args) {
        SpringApplication.run(Application.class).getBean(Application.class).run();
    }

    @Transactional
    public void run() {
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
            catJpaRepository.save(c);
        }
    }
}
