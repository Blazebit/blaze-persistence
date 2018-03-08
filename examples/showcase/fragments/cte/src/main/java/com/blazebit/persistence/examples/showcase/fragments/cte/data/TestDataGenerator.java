/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.examples.showcase.fragments.cte.data;

import com.blazebit.persistence.examples.showcase.base.bean.EntityManagerHolder;
import com.blazebit.persistence.examples.showcase.base.model.Cat;

import javax.inject.Inject;
import javax.transaction.Transactional;

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
