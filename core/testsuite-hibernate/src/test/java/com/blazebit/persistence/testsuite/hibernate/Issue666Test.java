/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.hibernate;

import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDB2;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Tuple;
import java.io.Serializable;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.3.0
 */
public class Issue666Test extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{BasicEntity.class, IdClassEntity.class, NestedIdClassEntity.class};
    }

    @Entity(name = "BasicEntity")
    public static class BasicEntity {
        @Id
        Long key1;
    }

    @Entity(name = "IdClassEntity")
    @IdClass(IdClassEntity.IdClassEntityId.class)
    public static class IdClassEntity {
        @Id
        @ManyToOne
        BasicEntity basicEntity;
        @Id
        Long key2;

        public static class IdClassEntityId implements Serializable {
            Long basicEntity;
            Long key2;
        }
    }

    @Entity(name = "NestedIdClassEntity")
    @IdClass(NestedIdClassEntity.NestedIdClassEntityId.class)
    public static class NestedIdClassEntity {
        @Id
        @ManyToOne
        IdClassEntity idClassEntity;
        @Id
        Long key3;

        public static class NestedIdClassEntityId implements Serializable {
            IdClassEntity.IdClassEntityId idClassEntity;
            Long key3;
        }
    }

    @Test
    // NOTE: Doing this with Hibernate < 5.0 leads to a syntax error with DB2
    @Category({ NoDB2.class })
    public void metaModelInstantiationWithNestedIdClassAssociationTest() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                cbf.create(em, Tuple.class)
                        .from(NestedIdClassEntity.class, "a")
                        .select("a.idClassEntity.basicEntity.key1")
                        .getResultList();
            }
        });
    }
}