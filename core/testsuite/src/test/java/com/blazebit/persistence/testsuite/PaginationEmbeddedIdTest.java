/*
 * Copyright 2014 - 2022 Blazebit.
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
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMSSQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.entity.*;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;

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
                EmbeddableTestEntitySub.class,
                EmbeddableTestEntityContainer.class
        };
    }

    @Override
    protected void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                EmbeddableTestEntity entity1 = new EmbeddableTestEntity();
                entity1.getId().setKey("e1");
                entity1.getId().setValue("e1");
                entity1.setVersion(1L);

                EmbeddableTestEntity entity2 = new EmbeddableTestEntity();
                entity2.getId().setKey("e2");
                entity2.getId().setValue("e2");
                entity2.setVersion(1L);

                EmbeddableTestEntity entity3 = new EmbeddableTestEntity();
                entity3.getId().setKey("e3");
                entity3.getId().setValue("a");
                entity3.setVersion(1L);

                EmbeddableTestEntity entity4 = new EmbeddableTestEntity();
                entity4.getId().setKey("e4");
                entity4.getId().setValue("a");
                entity4.setVersion(1L);

                IntIdEntity intIdEntity1 = new IntIdEntity("i1", 1);
                IntIdEntity intIdEntity2 = new IntIdEntity("i2", 2);

                em.persist(entity1);
                em.persist(entity2);
                em.persist(entity3);
                em.persist(entity4);
                em.persist(intIdEntity1);
                em.persist(intIdEntity2);

                entity1.getEmbeddable().getElementCollection().put("test", new NameObject("test", "b", intIdEntity1));
                entity1.getEmbeddable().getElementCollection().put("test2", new NameObject("test", "b", intIdEntity2));
                entity2.getEmbeddable().getElementCollection().put("test", new NameObject("test", "b", intIdEntity1));
                entity2.getEmbeddable().getElementCollection().put("test2", new NameObject("test", "b", intIdEntity2));
                entity3.getEmbeddable().getElementCollection().put("test", new NameObject("test", "b", intIdEntity1));
                entity3.getEmbeddable().getElementCollection().put("test2", new NameObject("test", "b", intIdEntity2));
                entity4.getEmbeddable().getElementCollection().put("test", new NameObject("test", "b", intIdEntity1));
                entity4.getEmbeddable().getElementCollection().put("test2", new NameObject("test", "b", intIdEntity2));
            }
        });
    }

    @Test
    public void simpleTest() {
        CriteriaBuilder<EmbeddableTestEntity> crit = cbf.create(em, EmbeddableTestEntity.class, "e");
        crit.where("e.embeddable.elementCollection['test'].primaryName").eq("test");
        crit.orderByAsc("e.id");

        // do not include joins that are only needed for the select clause
        String expectedCountQuery = "SELECT " + countPaginated("e.id.key, e.id.value", false) + " FROM EmbeddableTestEntity e "
                + "LEFT JOIN e.embeddable.elementCollection elementCollection_test_1"
                + onClause("KEY(elementCollection_test_1) = 'test'")
                + " WHERE " + joinAliasValue("elementCollection_test_1", "primaryName") + " = :param_0";

        String expectedObjectQuery = "SELECT e, (" + expectedCountQuery + ") FROM EmbeddableTestEntity e "
                + "LEFT JOIN e.embeddable.elementCollection elementCollection_test_1"
                + onClause("KEY(elementCollection_test_1) = 'test'")
                + " WHERE " + joinAliasValue("elementCollection_test_1", "primaryName") + " = :param_0"
                + " ORDER BY e.id.key ASC, e.id.value ASC";

        PaginatedCriteriaBuilder<EmbeddableTestEntity> pcb = crit.page(0, 2);

        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());
        pcb.getResultList();
    }

    @Test
    public void testJoinAvoidance() {
        CriteriaBuilder<String> crit = cbf.create(em, String.class).from(EmbeddableTestEntity.class, "e");
        crit.select("elem.primaryName");
        crit.leftJoinDefaultOn("e.embeddable.elementCollection", "elem")
                .on("KEY(elem)").eqExpression("'test'")
                .end();
        crit.orderByAsc("e.id");

        // do not include joins that are only needed for the select clause
        String expectedCountQuery = "SELECT " + countPaginated("e.id.key, e.id.value", false) + " FROM EmbeddableTestEntity e";

        String expectedObjectQuery = "SELECT " + joinAliasValue("elem", "primaryName") + ", (" + expectedCountQuery + ") FROM EmbeddableTestEntity e "
                + "LEFT JOIN e.embeddable.elementCollection elem"
                + onClause("KEY(elem) = 'test'")
                + " ORDER BY e.id.key ASC, e.id.value ASC";

        PaginatedCriteriaBuilder<String> pcb = crit.page(0, 2);

        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());
        pcb.getResultList();
    }

    @Test
    // Test for #444
    @Category({ NoOracle.class, NoMSSQL.class })
    public void keysetPaginateById() {
        CriteriaBuilder<EmbeddableTestEntity> crit = cbf.create(em, EmbeddableTestEntity.class, "e");
        crit.where("e.embeddable.elementCollection.primaryName").eq("test");
        crit.orderByAsc("e.id");

        // do not include joins that are only needed for the select clause
        String expectedCountQuery = "SELECT " + countPaginated("e.id.key, e.id.value", true) + " FROM EmbeddableTestEntity e "
                + "LEFT JOIN e.embeddable.elementCollection elementCollection_1"
                + " WHERE " + joinAliasValue("elementCollection_1", "primaryName") + " = :param_0";

        // limit this query using setFirstResult() and setMaxResult() according to the parameters passed to page()
        String expectedIdQuery = "SELECT e.id.key, e.id.value FROM EmbeddableTestEntity e "
                + "LEFT JOIN e.embeddable.elementCollection elementCollection_1"
                + " WHERE " + joinAliasValue("elementCollection_1", "primaryName") + " = :param_0"
                + " GROUP BY " + groupBy("e.id.key", "e.id.value")
                + " ORDER BY e.id.key ASC, e.id.value ASC";

        String expectedObjectQuery = "SELECT e FROM EmbeddableTestEntity e"
                + " WHERE (e.id.key = :ids_0_0 AND e.id.value = :ids_1_0)"
                + " ORDER BY e.id.key ASC, e.id.value ASC";
        String expectedInlineObjectQuery = "SELECT e, e.id.key, e.id.value, (" + expectedCountQuery + ") FROM EmbeddableTestEntity e"
                + " WHERE " + function("compare_row_value_subquery", "'IN'", "e.id.key", "e.id.value", function("LIMIT", "(" + expectedIdQuery + ")", "1")) + " = 0"
                + " ORDER BY e.id.key ASC, e.id.value ASC";

        PaginatedCriteriaBuilder<EmbeddableTestEntity> pcb = crit.page(null, 0, 1);

        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedIdQuery, pcb.withInlineIdQuery(false).withInlineCountQuery(false).getPageIdQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());
        if (dbmsDialect.supportsRowValueConstructor()) {
            assertEquals(expectedInlineObjectQuery, pcb.withInlineIdQuery(true).withInlineCountQuery(true).getQueryString());
        }
        PagedList<EmbeddableTestEntity> resultList = pcb.getResultList();

        pcb = crit.page(resultList.getKeysetPage(), 1, 1);

        expectedCountQuery = "SELECT " + countPaginated("e.id.key, e.id.value", true) + " FROM EmbeddableTestEntity e "
                + "LEFT JOIN e.embeddable.elementCollection elementCollection_1"
                + " WHERE " + joinAliasValue("elementCollection_1", "primaryName") + " = :param_0";

        expectedIdQuery = "SELECT e.id.key, e.id.value FROM EmbeddableTestEntity e "
                + "LEFT JOIN e.embeddable.elementCollection elementCollection_1"
                + " WHERE " + function("compare_row_value", "'<'", "CASE WHEN (1=NULLIF(1,1) AND :_keysetParameter_0=e.id.key) THEN 1 ELSE 0 END,CASE WHEN (1=NULLIF(1,1) AND :_keysetParameter_1=e.id.value) THEN 1 ELSE 0 END") + " = 0"
                + " AND " + joinAliasValue("elementCollection_1", "primaryName") + " = :param_0"
                + " GROUP BY " + groupBy("e.id.key", "e.id.value")
                + " ORDER BY e.id.key ASC, e.id.value ASC";

        expectedObjectQuery = "SELECT e FROM EmbeddableTestEntity e"
                + " WHERE (e.id.key = :ids_0_0 AND e.id.value = :ids_1_0)"
                + " ORDER BY e.id.key ASC, e.id.value ASC";
        expectedInlineObjectQuery = "SELECT e, e.id.key, e.id.value, (" + expectedCountQuery + ") FROM EmbeddableTestEntity e"
                + " WHERE " + function("compare_row_value_subquery", "'IN'", "e.id.key", "e.id.value", function("LIMIT", "(" + expectedIdQuery + ")", "1")) + " = 0"
                + " ORDER BY e.id.key ASC, e.id.value ASC";

        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedIdQuery, pcb.withInlineIdQuery(false).withInlineCountQuery(false).getPageIdQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());
        if (dbmsDialect.supportsRowValueConstructor()) {
            assertEquals(expectedInlineObjectQuery, pcb.withInlineIdQuery(true).withInlineCountQuery(true).getQueryString());
        }
        pcb.getResultList();
    }

    @Test
    public void keysetPaginateById2() {
        CriteriaBuilder<EmbeddableTestEntity> crit = cbf.create(em, EmbeddableTestEntity.class, "e");
        crit.where("e.embeddable.elementCollection.primaryName").eq("test");
        crit.orderByAsc("e.id.key");
        verifyException(crit.page(0, 1), IllegalStateException.class, r -> r.getQueryString());
    }

    @Test
    public void keysetPaginateById3() {
        CriteriaBuilder<EmbeddableTestEntity> crit = cbf.create(em, EmbeddableTestEntity.class, "e");
        crit.where("e.embeddable.elementCollection.primaryName").eq("test");
        crit.orderByAsc("e.id.key");
        crit.orderByAsc("e.id.value");

        String expectedCountQuery = "SELECT " + countPaginated("e.id.key, e.id.value", true) + " FROM EmbeddableTestEntity e "
                + "LEFT JOIN e.embeddable.elementCollection elementCollection_1"
                + " WHERE " + joinAliasValue("elementCollection_1", "primaryName") + " = :param_0";
        String expectedIdQuery = "SELECT e.id.key, e.id.value FROM EmbeddableTestEntity e LEFT JOIN e.embeddable.elementCollection elementCollection_1 WHERE elementCollection_1.primaryName = :param_0 GROUP BY e.id.key, e.id.value ORDER BY e.id.key ASC, e.id.value ASC";
        String expectedObjectQuery = "SELECT e FROM EmbeddableTestEntity e"
                + " WHERE (e.id.key = :ids_0_0 AND e.id.value = :ids_1_0)"
                + " ORDER BY e.id.key ASC, e.id.value ASC";
        String expectedInlineObjectQuery = "SELECT e, (" + expectedCountQuery + ") FROM EmbeddableTestEntity e"
                + " WHERE " + function("compare_row_value_subquery", "'IN'", "e.id.key", "e.id.value", function("LIMIT", "(" + expectedIdQuery + ")", "1")) + " = 0"
                + " ORDER BY e.id.key ASC, e.id.value ASC";
        PaginatedCriteriaBuilder<EmbeddableTestEntity> pcb = crit.page(0, 1);
        assertEquals(expectedObjectQuery, pcb.withInlineIdQuery(false).withInlineCountQuery(false).getQueryString());
        if (dbmsDialect.supportsRowValueConstructor()) {
            assertEquals(expectedInlineObjectQuery, pcb.withInlineIdQuery(true).withInlineCountQuery(true).getQueryString());
        }
    }

    @Test
    // Test for #711
    public void paginateWithConstantifiedIdPart() {
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class)
                .from(EmbeddableTestEntity.class, "e");
        crit.select("e.id").select("e.embeddable.elementCollection.primaryName");
        crit.where("e.id.key").eq("e3");
        crit.orderByAsc("e.id.key");
        crit.orderByAsc("e.id.value");
        PaginatedCriteriaBuilder<Tuple> pcb = crit.pageBy(0, 1, "e.id.key", "e.id.value");

        String expectedCountQuery = "SELECT " + countPaginated("e.id.key, e.id.value", false) + " FROM EmbeddableTestEntity e"
                + " WHERE e.id.key = :param_0";
        String expectedIdQuery = "SELECT e.id.key, e.id.value FROM EmbeddableTestEntity e WHERE e.id.key = :param_0 ORDER BY e.id.key ASC, e.id.value ASC";
        String expectedObjectQuery = "SELECT e.id, elementCollection_1.primaryName FROM EmbeddableTestEntity e LEFT JOIN e.embeddable.elementCollection elementCollection_1"
                + " WHERE (e.id.key = :ids_0_0 AND e.id.value = :ids_1_0)"
                + " ORDER BY e.id.key ASC, e.id.value ASC";
        String expectedInlineObjectQuery = "SELECT e.id, elementCollection_1.primaryName, (" + expectedCountQuery + ") FROM EmbeddableTestEntity e LEFT JOIN e.embeddable.elementCollection elementCollection_1"
                + " WHERE " + function("compare_row_value_subquery", "'IN'", "e.id.key", "e.id.value", function("LIMIT", "(" + expectedIdQuery + ")", "1")) + " = 0"
                + " ORDER BY e.id.key ASC, e.id.value ASC";
        assertEquals(expectedObjectQuery, pcb.withInlineIdQuery(false).withInlineCountQuery(false).getQueryString());
        if (dbmsDialect.supportsRowValueConstructor()) {
            assertEquals(expectedInlineObjectQuery, pcb.withInlineIdQuery(true).withInlineCountQuery(true).getQueryString());
        }
        PagedList<Tuple> list = pcb.getResultList();
        assertEquals(2, list.size());
    }
}
