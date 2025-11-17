/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.hibernate;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.junit.Test;

import com.blazebit.persistence.testsuite.AbstractCoreTest;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

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
