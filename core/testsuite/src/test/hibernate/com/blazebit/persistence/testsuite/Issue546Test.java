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

import com.blazebit.persistence.CTE;
import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import org.hibernate.annotations.Formula;
import org.junit.Assert;
import org.junit.Test;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PersistenceException;
import javax.persistence.Table;

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
