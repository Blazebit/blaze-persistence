/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.hibernate;

import com.blazebit.persistence.CTE;

import org.hibernate.annotations.Formula;
import org.junit.Assert;
import org.junit.Test;

import com.blazebit.persistence.testsuite.AbstractCoreTest;
import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Table;

/**
 * @author Christian beikov
 * @since 1.5.0
 */
public class Issue546Test extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
            FormulaTestEntity.class
        };
    }

    @Override
    public void init() {
        // No-op
    }

    @Test
    public void testBuild() throws Exception {
        try {
            createEntityManagerFactory("TestsuiteBase", createProperties("none"));
            Assert.fail("Expected exception");
        } catch (PersistenceException ex) {
            Throwable cause = ex.getCause();
            if (cause.getCause() != null) {
                cause = cause.getCause();
            }
            Assert.assertTrue(cause.getMessage().contains(FormulaTestEntity.class.getName() + "#upperName"));
        }
    }

    @CTE
    @Entity
    @Table(name = "issue_546_formula_test_entity")
    public static class FormulaTestEntity {
        @Id
        @GeneratedValue
        private Long id;
        @Basic(optional = false)
        private String name;
        @Formula("UPPER(name)")
        private String upperName;
    }

}
