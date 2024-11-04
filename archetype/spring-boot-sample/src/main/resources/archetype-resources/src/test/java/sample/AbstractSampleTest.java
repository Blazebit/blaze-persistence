/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package ${package}.sample;

import javax.persistence.EntityManager;
import ${package}.model.Cat;
import ${package}.model.Person;
import ${package}.view.CatSimpleView;
import ${package}.view.CatWithOwnerView;
import ${package}.view.PersonSimpleView;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract class AbstractSampleTest {

    @Autowired
    protected EntityManager em;

    @Before
    public void init() {
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
    }
}
