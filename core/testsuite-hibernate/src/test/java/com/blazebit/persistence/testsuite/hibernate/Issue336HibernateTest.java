/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.hibernate;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.hibernate.annotations.Formula;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;

/**
 * @author Christian beikov
 * @since 1.2.0
 */
public class Issue336HibernateTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
            FormulaTestEntity.class
        };
    }

    @Before
    public void setUp() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                FormulaTestEntity root1 = new FormulaTestEntity("test");
                em.persist(root1);
            }
        });
    }

    @Test
    public void testBuild() throws Exception {
        CriteriaBuilder<FormulaTestEntity> cb = cbf.create(em, FormulaTestEntity.class);

        List<FormulaTestEntity> result = cb.getResultList();
        Assert.assertEquals("test", result.get(0).getName());
        Assert.assertEquals("TEST", result.get(0).getUpperName());
    }

    @Entity
    @Table(name = "issue_336_formula_test_entity")
    public static class FormulaTestEntity {

        private Long id;
        private String name;
        private String upperName;

        public FormulaTestEntity() {
        }

        public FormulaTestEntity(String name) {
            this.name = name;
        }

        @Id
        @GeneratedValue
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        @Basic(optional = false)
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Formula("UPPER(name)")
        public String getUpperName() {
            return upperName;
        }

        public void setUpperName(String upperName) {
            this.upperName = upperName;
        }
    }

}
