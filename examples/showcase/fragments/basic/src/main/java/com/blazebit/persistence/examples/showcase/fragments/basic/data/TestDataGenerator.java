/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.showcase.fragments.basic.data;

import com.blazebit.persistence.examples.showcase.base.bean.EntityManagerHolder;
import com.blazebit.persistence.examples.showcase.base.model.Cat;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@Transactional
public class TestDataGenerator {

    @Inject
    private EntityManagerHolder emHolder;

    public void generateTestData() {
        Cat moac = new Cat("A - Mother of all cats");
        emHolder.getEntityManager().persist(moac);

        Cat gen1_1 = new Cat("C - Generation 1 - Cat 1");
        gen1_1.setFather(moac);
        emHolder.getEntityManager().persist(gen1_1);

        Cat gen1_2 = new Cat("D - Generation 1 - Cat 2");
        gen1_2.setFather(moac);
        emHolder.getEntityManager().persist(gen1_2);

        Cat gen2_1_1 = new Cat("E - Generation 2 - Cat 1/1");
        gen2_1_1.setFather(moac);
        emHolder.getEntityManager().persist(gen2_1_1);

        Cat gen2_1_2 = new Cat("F - Generation 2 - Cat 1/2");
        gen2_1_2.setFather(gen1_1);
        emHolder.getEntityManager().persist(gen2_1_2);

        Cat gen2_2_1 = new Cat("G - Generation 2 - Cat 2/1");
        gen2_2_1.setFather(gen1_2);
        emHolder.getEntityManager().persist(gen2_2_1);
    }

    public void addCat(String name) {
        Cat cat = new Cat(name);
        emHolder.getEntityManager().persist(cat);
    }
}
