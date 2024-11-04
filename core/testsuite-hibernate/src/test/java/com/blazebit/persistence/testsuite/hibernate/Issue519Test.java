/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.hibernate;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.Subselect;
import org.junit.Test;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.2.0
 */
public class Issue519Test extends AbstractCoreTest {

    private A instance;

    @Override
    protected SchemaMode getSchemaMode() {
        return SchemaMode.JDBC;
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{ A.class, B.class };
    }

    @Override
    protected void setUpOnce() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                instance = new A();
                instance.id = 1L;
                em.persist(instance);
                em.flush();
                em.refresh(instance);
            }
        });
    }

    @Test
    public void subselectEntityTest() {
        CriteriaBuilder<A> cb = cbf.create(em, A.class)
                .from(A.class, "a")
                .where("a.b.b").eq(5L);

        List<A> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals(Long.valueOf(5L), resultList.get(0).b.b);
    }

    @Entity
    @Table(name = "A")
    public static class A {

        @Id
        private Long id;

        @Generated(GenerationTime.INSERT)
        @OneToOne(mappedBy = "a")
        private B b;

    }

    @Entity
    @Subselect("SELECT 5 as b, a.id AS aId FROM A a")
    public static class B {

        private Long aId;

        private A a;

        private Long b;

        @Id
        public Long getAId() {
            return aId;
        }

        public void setAId(Long aId) {
            this.aId = aId;
        }

        @OneToOne
        @PrimaryKeyJoinColumn(name = "a_id")
        public A getA() {
            return a;
        }

        public void setA(A a) {
            this.a = a;
        }

        @Column
        public Long getB() {
            return b;
        }

        public void setB(Long b) {
            this.b = b;
        }
    }
}
