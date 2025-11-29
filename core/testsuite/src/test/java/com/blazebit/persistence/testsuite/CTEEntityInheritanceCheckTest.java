/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.CTE;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PersistenceException;

/**
 * @author Moritz Becker
 * @since 1.4.0
 */
@Category({ NoEclipselink.class })
public class CTEEntityInheritanceCheckTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class[] {
                SimpleEntity.class,
                BaseCte.class,
                ConcreteCte.class
        };
    }

    @Override
    public void init() {
        // No-op
    }

    @Test
    public void test() {
        try {
            emf = createEntityManagerFactory("TestsuiteBase", createProperties("none"));
        } catch (PersistenceException ex) {
            Throwable t = ex;
            if (ex.getCause() != null) {
                t = ex.getCause();
                if (t.getCause() != null) {
                    t = t.getCause();
                }
            }
            Assert.assertTrue(t.getMessage().contains("Found invalid polymorphic CTE entity definitions"));
        }
    }

    @Entity
    public static class SimpleEntity {
        @Id
        private Long id;
    }

    @Entity
    @CTE
    public static class BaseCte {
        @Id
        private Long id;
    }

    @Entity
    @CTE
    public static class ConcreteCte extends BaseCte {
    }
}
