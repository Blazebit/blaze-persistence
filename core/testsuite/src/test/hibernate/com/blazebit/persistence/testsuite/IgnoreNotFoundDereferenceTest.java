/*
 * Copyright 2014 - 2024 Blazebit.
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

package com.blazebit.persistence.testsuite;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.junit.Test;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class IgnoreNotFoundDereferenceTest extends AbstractCoreTest {

    @Test
    public void testMetamodelBoots() {
        ItemEntity itemEntity = new ItemEntity();

        ProblemEntity problemEntity = new ProblemEntity();
        problemEntity.associatedItem = itemEntity;

        em.persist(itemEntity);
        em.persist(problemEntity);
        em.flush();
        em.clear();

        cbf.create(em, ProblemEntity.class).getResultList();
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] { ItemEntity.class, ProblemEntity.class };
    }

    @Entity(name = "ItemEntity")
    public static class ItemEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        Integer id;

    }

    @Entity(name = "ProblemEntity")
    public static class ProblemEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        Integer id;

        @NotFound(action = NotFoundAction.IGNORE)
        @ManyToOne
        ItemEntity associatedItem;
    }

}
