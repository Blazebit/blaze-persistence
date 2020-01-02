/*
 * Copyright 2014 - 2020 Blazebit.
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
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PersistenceException;

/**
 * @author Moritz Becker
 * @since 1.4.0
 */
@Category({NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
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
