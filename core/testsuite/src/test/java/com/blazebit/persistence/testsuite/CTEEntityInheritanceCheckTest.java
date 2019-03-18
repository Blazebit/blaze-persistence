/*
 * Copyright 2014 - 2019 Blazebit.
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
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.Entity;
import javax.persistence.Id;

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

    @Test(expected = RuntimeException.class)
    public void test() {
        CriteriaBuilder<BaseCte> cb = cbf.create(em, BaseCte.class)
                .with(BaseCte.class)
                    .from(SimpleEntity.class)
                    .bind("id").select("id")
                .end();

        cb.getResultList();
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
