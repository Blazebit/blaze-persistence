/*
 * Copyright 2014 - 2018 Blazebit.
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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.testsuite.base.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
// NOTE: EclipseLink doesn't support Map in embeddables: https://bugs.eclipse.org/bugs/show_bug.cgi?id=391062
// TODO: report that datanucleus doesn't support element collection in an embeddable
@Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
public class PaginationEmbeddedIdTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
                IntIdEntity.class,
                EmbeddableTestEntity.class,
                EmbeddableTestEntityContainer.class
        };
    }

    @Test
    public void simpleTest() {
        CriteriaBuilder<EmbeddableTestEntity> crit = cbf.create(em, EmbeddableTestEntity.class, "e");
        crit.where("e.embeddable.elementCollection['test'].primaryName").eq("test");
        crit.orderByAsc("e.id");

        // do not include joins that are only needed for the select clause
        String expectedCountQuery = "SELECT " + countPaginated("e.id", true) + " FROM EmbeddableTestEntity e "
                + "LEFT JOIN e.embeddable.elementCollection elementCollection_test_1"
                + onClause("KEY(elementCollection_test_1) = 'test'")
                + " WHERE " + joinAliasValue("elementCollection_test_1", "primaryName") + " = :param_0";

        // limit this query using setFirstResult() and setMaxResult() according to the parameters passed to page()
        String expectedIdQuery = "SELECT e.id FROM EmbeddableTestEntity e "
                + "LEFT JOIN e.embeddable.elementCollection elementCollection_test_1"
                + onClause("KEY(elementCollection_test_1) = 'test'")
                + " WHERE " + joinAliasValue("elementCollection_test_1", "primaryName") + " = :param_0"
                + " GROUP BY " + groupBy("e.id", renderNullPrecedenceGroupBy("e.id"))
                + " ORDER BY " + renderNullPrecedence("e.id", "ASC", "LAST");

        String expectedObjectQuery = "SELECT e FROM EmbeddableTestEntity e"
                + " WHERE e.id IN :ids"
                + " ORDER BY " + renderNullPrecedence("e.id", "ASC", "LAST");

        PaginatedCriteriaBuilder<EmbeddableTestEntity> pcb = crit.page(0, 2);

        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedIdQuery, pcb.getPageIdQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());
        pcb.getResultList();
    }
}
