/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoH2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.DocumentCTE;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.ParameterOrderCte;
import com.blazebit.persistence.testsuite.entity.ParameterOrderCteB;
import com.blazebit.persistence.testsuite.entity.ParameterOrderEntity;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.RecursiveEntity;
import com.blazebit.persistence.testsuite.entity.TestAdvancedCTE1;
import com.blazebit.persistence.testsuite.entity.TestAdvancedCTE2;
import com.blazebit.persistence.testsuite.entity.TestCTE;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.testsuite.entity.Workflow;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import jakarta.persistence.EntityManager;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.1
 */
public class InlineCTETest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
            Document.class,
            Version.class,
            Person.class,
            Workflow.class,
            IntIdEntity.class,
            DocumentCTE.class,
            RecursiveEntity.class,
            TestCTE.class,
            TestAdvancedCTE1.class,
            TestAdvancedCTE2.class,
            ParameterOrderCte.class,
            ParameterOrderCteB.class,
            ParameterOrderEntity.class
        };
    }

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                RecursiveEntity root1 = new RecursiveEntity("root1");
                RecursiveEntity child1_1 = new RecursiveEntity("child1_1", root1);
                RecursiveEntity child1_2 = new RecursiveEntity("child1_2", root1);

                RecursiveEntity child1_1_1 = new RecursiveEntity("child1_1_1", child1_1);
                RecursiveEntity child1_2_1 = new RecursiveEntity("child1_2_1", child1_2);

                em.persist(root1);
                em.persist(child1_1);
                em.persist(child1_2);
                em.persist(child1_1_1);
                em.persist(child1_2_1);
                root1.getChildren2().add(child1_1);
                root1.getChildren2().add(child1_2);


                ParameterOrderEntity parameterOrderEntity = new ParameterOrderEntity();
                parameterOrderEntity.setOne((short) 1);
                parameterOrderEntity.setTwo(2);
                parameterOrderEntity.setThree(3L);
                em.persist(parameterOrderEntity);
            }
        });
    }

    @Test
    @Category({ NoEclipselink.class })
    public void testInliningFromSubqueryCte() {
        cbf.create(em, RecursiveEntity.class)
                .withRecursive(DocumentCTE.class)
                    .from(Document.class, "d")
                    .bind("id").select("d.id")
                    .bind("parent.id").select("d.parent.id")
                    .bind("root.id").select("d.id")
                    .where("d").eqExpression(":param_1")
                    .setParameter("param_1", em.getReference(Document.class, 1L))
                .unionAll()
                    .from(Document.class, "d")
                    .innerJoinOn(DocumentCTE.class, "parentDocument").on("parentDocument.id").eqExpression("d.parent.id").end()
                    .bind("id").select("d.id")
                    .bind("parent.id").select("d.parent.id")
                    .bind("root.id").select("parentDocument.root.id")
                .end()
                .select("document")
                .from(Document.class, "document")
                .where("document").in()
                    .fromSubquery(DocumentCTE.class, "de1")
                        .from(DocumentCTE.class, "d2")
                        .bind("id").select("d2.id")
                        .bind("parent.id").select("d2.parent.id")
                        .bind("root.id").select("d2.root.id")
                    .end()
                    .select("de1.document")
                .end()
                .getResultList();
    }

    // TODO: Oracle requires a cycle clause #295
    @Test
    @Category({ NoEclipselink.class, NoOracle.class })
    public void testReusedNestedCte() {
        CriteriaBuilder<ParameterOrderEntity> cteBuilder = cbf.create(em, ParameterOrderEntity.class)
                .withRecursive(TestCTE.class)
                    .from(RecursiveEntity.class, "re")
                    .bind("id").select("re.id")
                    .bind("name").select("re.name")
                    .bind("level").select("1")
                    .where("re.parent").isNull()
                .unionAll()
                    .from(RecursiveEntity.class, "re")
                    .innerJoinOn(TestCTE.class, "tcte").on("tcte.id").eqExpression("re.parent.id").end()
                    .bind("id").select("re.id")
                    .bind("name").select("re.name")
                    .bind("level").select("tcte.level + 1")
                .end()
                .with(TestAdvancedCTE1.class)
                    .from(TestCTE.class, "testcte")
                    .groupBy("id", "level", "name")
                    .bind("id").select("id")
                    .bind("level").select("level")
                    .bind("parentId").select("id")
                    .bind("embeddable.name").select("name")
                    .bind("embeddable.description").select("name")
                    .bind("embeddable.recursiveEntity.id").select("id")
                .end()
                .with(TestAdvancedCTE2.class)
                    .from(TestCTE.class, "testcte")
                    .where("id").in().from(TestAdvancedCTE1.class).select("id").end()
                    .where("id").in().from(TestCTE.class).select("id").end()
                    .groupBy("id", "name")
                    .bind("id").select("id")
                    .bind("embeddable.name").select("name")
                    .bind("embeddable.description").select("name")
                    .bind("embeddable.recursiveEntity.id").select("id")
                .end()
                .with(ParameterOrderEntity.class)
                    .from(TestAdvancedCTE1.class, "a")
                    .innerJoinOn(TestAdvancedCTE2.class, "b").on("a.id").eqExpression("b.id").end()
                    .bind("id").select("a.id")
                    .bind("one").select("1")
                    .bind("two").select("2")
                    .bind("three").select("a.id + b.id")
                .end();

        assertEquals(
                "WITH RECURSIVE TestCTE(id, name, level) AS(\n" +
                        "SELECT re.id, re.name, 1 FROM RecursiveEntity re WHERE re.parent IS NULL\n" +
                        "UNION ALL\n" +
                        "SELECT re.id, re.name, tcte.level + 1 FROM RecursiveEntity re JOIN TestCTE tcte" + onClause("tcte.id = re.parent.id") + "\n" +
                        ")\n" +
                        "SELECT parameterOrderEntity " +
                        "FROM ParameterOrderEntity(" +
                            "SELECT a.id, 1, 2, a.id + b.id " +
                            "FROM TestAdvancedCTE1(" +
                                "SELECT testcte.id, testcte.level, testcte.id, testcte.name, testcte.name, testcte.id " +
                                "FROM TestCTE testcte GROUP BY testcte.id, testcte.level, testcte.name" +
                            ") a(id, level, parentId, embeddable.name, embeddable.description, embeddable.recursiveEntity.id) " +
                            "JOIN TestAdvancedCTE2(" +
                                "SELECT testcte.id, testcte.name, testcte.name, testcte.id " +
                                "FROM TestCTE testcte " +
                                "WHERE testcte.id IN (" +
                                    "SELECT testAdvancedCTE1.id " +
                                    "FROM TestAdvancedCTE1(" +
                                        "SELECT testcte.id, testcte.level, testcte.id, testcte.name, testcte.name, testcte.id " +
                                        "FROM TestCTE testcte " +
                                        "GROUP BY testcte.id, testcte.level, testcte.name" +
                                    ") testAdvancedCTE1(id, level, parentId, embeddable.name, embeddable.description, embeddable.recursiveEntity.id)" +
                                ") " +
                                "AND testcte.id IN (" +
                                    "SELECT testCTE.id FROM TestCTE testCTE" +
                                ") " +
                                "GROUP BY testcte.id, testcte.name" +
                            ") b(id, embeddable.name, embeddable.description, embeddable.recursiveEntity.id)" + onClause("a.id = b.id") +
                        ") parameterOrderEntity(id, one, two, three)",
                cteBuilder.getQueryString()
        );
        cteBuilder.getResultList();
    }

    // TODO: Oracle requires a cycle clause #295
    @Test
    @Category({ NoEclipselink.class, NoOracle.class })
    public void testReusedNestedCte2() {
        CriteriaBuilder<ParameterOrderCte> cteBuilder = cbf.create(em, ParameterOrderCte.class)
                .withRecursive(TestCTE.class)
                    .from(RecursiveEntity.class, "re")
                    .bind("id").select("re.id")
                    .bind("name").select("re.name")
                    .bind("level").select("1")
                    .where("re.parent").isNull()
                .unionAll()
                    .from(RecursiveEntity.class, "re")
                    .innerJoinOn(TestCTE.class, "tcte").on("tcte.id").eqExpression("re.parent.id").end()
                    .bind("id").select("re.id")
                    .bind("name").select("re.name")
                    .bind("level").select("tcte.level + 1")
                .end()
                .with(TestAdvancedCTE1.class)
                    .from(TestCTE.class, "testcte")
                    .groupBy("id", "level", "name")
                    .bind("id").select("id")
                    .bind("level").select("level")
                    .bind("parentId").select("id")
                    .bind("embeddable.name").select("name")
                    .bind("embeddable.description").select("name")
                    .bind("embeddable.recursiveEntity.id").select("id")
                .end()
                .with(TestAdvancedCTE2.class)
                    .from(TestCTE.class, "testcte")
                    .where("id").in().from(TestAdvancedCTE1.class).select("id").end()
                    .where("id").in().from(TestCTE.class).select("id").end()
                    .groupBy("id", "name")
                    .bind("id").select("id")
                    .bind("embeddable.name").select("name")
                    .bind("embeddable.description").select("name")
                    .bind("embeddable.recursiveEntity.id").select("id")
                .end()
                .with(ParameterOrderEntity.class)
                    .from(TestAdvancedCTE1.class, "a")
                    .innerJoinOn(TestAdvancedCTE2.class, "b").on("a.id").eqExpression("b.id").end()
                    .bind("id").select("a.id")
                    .bind("one").select("1")
                    .bind("two").select("2")
                    .bind("three").select("a.id + b.id")
                .end()
                .with(ParameterOrderCte.class)
                    .from(ParameterOrderEntity.class)
                    .bind("four").select("one")
                    .bind("five").select("two")
                    .bind("six").select("three")
                    .end()
                ;

        cteBuilder.getResultList();
    }

    @Test
    @Category({ NoEclipselink.class })
    public void testParameterBindingOrder() {
        CriteriaBuilder<ParameterOrderEntity> cteBuilder = cbf.create(em, ParameterOrderEntity.class)
                .with(ParameterOrderCte.class)
                .from(ParameterOrderEntity.class, "poe")
                .where("poe.one").eq((short) 1)
                .where("poe.two").eq(2)
                .where("poe.three").eq(3L)
                .bind("four").select("poe.one")
                .bind("five").select("poe.two")
                .bind("six").select("poe.three")
                .end();

        List<ParameterOrderEntity> resultList = cbf.create(em, ParameterOrderEntity.class)
                .withCtesFrom(cteBuilder)
                .from(ParameterOrderEntity.class, "poe2")
                .where("poe2.three").eq(3L)
                .where("poe2.two").eq(2)
                .where("poe2.one").eq((short) 1)
                .where("poe2.two").notIn(1, 3, 4)
                .where("poe2.one").in().from(ParameterOrderCte.class, "poc").select("poc.four").end()
                .getResultList();

        Assert.assertEquals(1, resultList.size());
    }

    @Test
    @Category({ NoEclipselink.class })
    public void testParameterBindingOrderPaginated() {
        CriteriaBuilder<ParameterOrderEntity> cteBuilder = cbf.create(em, ParameterOrderEntity.class)
                .with(ParameterOrderCte.class)
                .from(ParameterOrderEntity.class, "poe")
                .where("poe.one").eq((short) 1)
                .where("poe.two").eq(2)
                .where("poe.three").eq(3L)
                .bind("four").select("poe.one")
                .bind("five").select("poe.two")
                .bind("six").select("poe.three")
                .end();

        List<ParameterOrderCte> resultList = cbf.create(em, ParameterOrderCte.class)
                .withCtesFrom(cteBuilder)
                .from(ParameterOrderCte.class, "poc2")
                .where("poc2.six").eq(3L)
                .where("poc2.five").eq(2)
                .where("poc2.four").eq((short) 1)
                .where("poc2.five").notIn(1, 3, 4)
                .orderByAsc("poc2.four")
                .page(0, 10)
                .getResultList();

        Assert.assertEquals(1, resultList.size());
    }

    @Test
    @Category({ NoEclipselink.class })
    public void testParameterBindingOrderNested() {
        CriteriaBuilder<ParameterOrderEntity> cteBuilder = cbf.create(em, ParameterOrderEntity.class)
                .with(ParameterOrderCte.class)
                    .from(ParameterOrderEntity.class, "poe")
                    .where("poe.one").eq((short) 1)
                    .where("poe.two").eq(2)
                    .where("poe.three").eq(3L)
                    .bind("four").select("poe.one")
                    .bind("five").select("poe.two")
                    .bind("six").select("poe.three")
                .end()
                .with(ParameterOrderCteB.class)
                    .from(ParameterOrderCte.class, "poc2")
                    .where("poc2.six").eq(3L)
                    .where("poc2.five").eq(2)
                    .where("poc2.four").eq((short) 1)
                    .where("poc2.five").notIn(1, 3, 4)
                    .bind("seven").select("poc2.four")
                    .bind("eight").select("poc2.five")
                    .bind("nine").select("poc2.six")
                .end();

        List<ParameterOrderCteB> resultList = cbf.create(em, ParameterOrderCteB.class)
                .withCtesFrom(cteBuilder)
                .from(ParameterOrderCteB.class, "poc3")
                .getResultList();

        Assert.assertEquals(1, resultList.size());
    }

    @Test
    @Category({ NoEclipselink.class })
    public void testParameterBindingOrderNestedPaginated() {
        CriteriaBuilder<ParameterOrderEntity> cteBuilder = cbf.create(em, ParameterOrderEntity.class)
                .with(ParameterOrderCte.class)
                    .from(ParameterOrderEntity.class, "poe")
                    .where("poe.one").eq((short) 1)
                    .where("poe.two").eq(2)
                    .where("poe.three").eq(3L)
                    .bind("four").select("poe.one")
                    .bind("five").select("poe.two")
                    .bind("six").select("poe.three")
                .end()
                .with(ParameterOrderCteB.class)
                    .from(ParameterOrderCte.class, "poc2")
                    .where("poc2.six").eq(3L)
                    .where("poc2.five").eq(2)
                    .where("poc2.four").eq((short) 1)
                    .where("poc2.five").notIn(1, 3, 4)
                    .bind("seven").select("poc2.four")
                    .bind("eight").select("poc2.five")
                    .bind("nine").select("poc2.six")
                .end();

        List<ParameterOrderCteB> resultList = cbf.create(em, ParameterOrderCteB.class)
                .withCtesFrom(cteBuilder)
                .from(ParameterOrderCteB.class, "poc3")
                .orderByAsc("poc3.seven")
                .orderByAsc("poc3.eight")
                .orderByAsc("poc3.nine")
                .page(0, 10)
                .getResultList();

        Assert.assertEquals(1, resultList.size());
    }

    @Test
    @Category({ NoEclipselink.class })
    public void testInlineCTE() {
        CriteriaBuilder<TestCTE> cb = cbf.create(em, TestCTE.class, "t").where("t.level").ltExpression("2");
        cb.with(TestCTE.class, true)
            .from(RecursiveEntity.class, "e")
            .bind("id").select("e.id")
            .bind("name").select("e.name")
            .bind("level").select("0")
            .where("e.parent").isNull()
        .end();
        String subquery = "SELECT e.id, e.name, 0 FROM RecursiveEntity e WHERE e.parent IS NULL";
        String expected = ""
                + "SELECT t FROM TestCTE(" + subquery + ") t(id, name, level) WHERE t.level < 2";

        assertEquals(expected, cb.getQueryString());
        List<TestCTE> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals("root1", resultList.get(0).getName());
    }

    @Test
    @Category({ NoEclipselink.class })
    public void testInlineCTENullExpression() {
        CriteriaBuilder<TestCTE> cb = cbf.create(em, TestCTE.class, "t").where("t.level").ltExpression("2");
        cb.with(TestCTE.class, true)
                .from(RecursiveEntity.class, "e")
                .bind("id").select("e.id")
                .bind("name").select("e.name")
                .bind("level").select("null")
                .where("e.parent").isNull()
                .end();
        String subquery = "SELECT e.id, e.name, NULL FROM RecursiveEntity e WHERE e.parent IS NULL";
        String expected = ""
                + "SELECT t FROM TestCTE(" + subquery + ") t(id, name, level) WHERE t.level < 2";

        assertEquals(expected, cb.getQueryString());
        List<TestCTE> resultList = cb.getResultList();
        assertEquals(0, resultList.size());
    }

    @Test
    @Category({ NoEclipselink.class })
    public void testInlineCTEWithParam() {
        CriteriaBuilder<TestCTE> cb = cbf.create(em, TestCTE.class, "t").where("t.level").ltExpression("2");
        cb.with(TestCTE.class, true)
                .from(RecursiveEntity.class, "e")
                .bind("id").select("e.id")
                .bind("name").select("e.name")
                .bind("level").select("0")
                .where("e.parent.name").eq("root1")
                .end();
        String subquery = "SELECT e.id, e.name, 0 FROM RecursiveEntity e LEFT JOIN e.parent parent_1 WHERE parent_1.name = :param_0";
        String expected = ""
                + "SELECT t FROM TestCTE(" + subquery + ") t(id, name, level) WHERE t.level < 2";

        assertEquals(expected, cb.getQueryString());
        List<TestCTE> resultList = cb.getResultList();
        assertEquals(2, resultList.size());
    }

    @Test
    @Category({ NoEclipselink.class })
    public void testInlineCTEInCorrelatedSubquery() {
        CriteriaBuilder<RecursiveEntity> cb = cbf.create(em, RecursiveEntity.class, "r")
                .whereExists()
                    .from(TestCTE.class, "t")
                    .where("t.id").eqExpression("r.id")
                .end();
        cb.with(TestCTE.class, true)
                .from(RecursiveEntity.class, "e")
                .bind("id").select("e.id")
                .bind("name").select("e.name")
                .bind("level").select("0")
                .where("e.parent.name").eq("root1")
                .end();
        String subquery = "SELECT e.id, e.name, 0 FROM RecursiveEntity e LEFT JOIN e.parent parent_1 WHERE parent_1.name = :param_0";
        String expected = ""
                + "SELECT r FROM RecursiveEntity r WHERE EXISTS (SELECT 1 FROM TestCTE(" + subquery + ") t(id, name, level) WHERE t.id = r.id)";

        assertEquals(expected, cb.getQueryString());
        List<RecursiveEntity> resultList = cb.getResultList();
        assertEquals(2, resultList.size());
    }

    @Test
    @Category({ NoEclipselink.class })
    public void testInlineEntityWithLimit() {
        CriteriaBuilder<RecursiveEntity> cb = cbf.create(em, RecursiveEntity.class)
                .fromEntitySubquery(RecursiveEntity.class, "t")
                    .where("t.parent.name").eq("root1")
                    .orderByAsc("name")
                    .setMaxResults(1)
                .end();
        String subquery = "SELECT t.id, t.name, t.parent.id FROM RecursiveEntity t LEFT JOIN t.parent parent_1 WHERE parent_1.name = :param_0 ORDER BY t.name ASC LIMIT 1";
        String expected = ""
                + "SELECT t FROM RecursiveEntity(" + subquery + ") t(id, name, parent.id)";

        assertEquals(expected, cb.getQueryString());
        List<RecursiveEntity> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
    }

    // Test for #1285
    @Test
    @Category({ NoEclipselink.class })
    public void testJoinOnSubquery() {
        CriteriaBuilder<RecursiveEntity> cb = cbf.create(em, RecursiveEntity.class)
                .from(RecursiveEntity.class, "t")
                .innerJoinOnSubquery(TestCTE.class, "cte")
                    .from(RecursiveEntity.class, "sub")
                    .bind("id").select("MAX(sub.id)", "id_")
                    .bind("name").select("'abc'", "name_")
                    .bind("level").select("1", "level_")
                    .where("sub.id").gtLiteral(0)
                .end().setOnExpression("t.id = cte.id")
                .where("t.id").gtLiteral(0);
        String expected = ""
                + "SELECT t FROM RecursiveEntity t JOIN TestCTE(" +
                "SELECT MAX(sub.id) AS id_, 'abc' AS name_, 1 AS level_ FROM RecursiveEntity sub WHERE sub.id > 0" +
                ") cte(id, name, level)" + onClause("t.id = cte.id") +
                " WHERE t.id > 0";

        assertEquals(expected, cb.getQueryString());
        List<RecursiveEntity> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
    }

    @Test
    @Category({ NoEclipselink.class })
    public void testMultipleInlineEntityWithLimitJoin() {
        CriteriaBuilder<RecursiveEntity> cb = cbf.create(em, RecursiveEntity.class)
                .fromEntitySubquery(RecursiveEntity.class, "t")
                    .leftJoinOnEntitySubquery(RecursiveEntity.class, "subT")
                        .where("subT.parent.name").eq("root1")
                        .orderByAsc("name")
                        .setMaxResults(1)
                    .end().setOnExpression("t = subT")
                    .where("t.parent.name").eq("root1")
                    .orderByAsc("name")
                    .setMaxResults(1)
                .end()
                .leftJoinOnEntitySubquery(RecursiveEntity.class, "subT2")
                    .where("subT2.parent.name").eq("root1")
                    .orderByAsc("name")
                    .setMaxResults(1)
                .end().setOnExpression("t = subT2");
        String subquery = "SELECT t.id, t.name, t.parent.id FROM RecursiveEntity t LEFT JOIN t.parent parent_1 " +
                "LEFT JOIN RecursiveEntity(" +
                "SELECT subT.id, subT.name, subT.parent.id FROM RecursiveEntity subT LEFT JOIN subT.parent parent_1 WHERE parent_1.name = :param_0 ORDER BY subT.name ASC LIMIT 1" +
                ") subT(id, name, parent.id) ON (t = subT) WHERE parent_1.name = :param_1 ORDER BY t.name ASC LIMIT 1";
        String expected = ""
                + "SELECT t FROM RecursiveEntity(" + subquery + ") t(id, name, parent.id) LEFT JOIN RecursiveEntity(" +
                "SELECT subT2.id, subT2.name, subT2.parent.id FROM RecursiveEntity subT2 LEFT JOIN subT2.parent parent_1 WHERE parent_1.name = :param_2 ORDER BY subT2.name ASC LIMIT 1" +
                ") subT2(id, name, parent.id) ON (t = subT2)";

        assertEquals(expected, cb.getQueryString());
        List<RecursiveEntity> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
    }

    @Test
    @Category({ NoEclipselink.class })
    public void testJoinInlineEntityWithLimit() {
        CriteriaBuilder<RecursiveEntity> cb = cbf.create(em, RecursiveEntity.class)
                .fromValues(RecursiveEntity.class, "name", "val", Collections.singletonList("child1_1"))
                .leftJoinOnEntitySubquery(RecursiveEntity.class, "t")
                    .where("t.parent.name").eq("root1")
                    .orderByAsc("name")
                    .setMaxResults(1)
                .end()
                    .on("val").eqExpression("t.name")
                .end()
                .select("t");
        String subquery = "SELECT t.id, t.name, t.parent.id FROM RecursiveEntity t LEFT JOIN t.parent parent_1 WHERE parent_1.name = :param_0 ORDER BY t.name ASC LIMIT 1";
        String expected = ""
                + "SELECT t FROM String(1 VALUES LIKE RecursiveEntity.name) val LEFT JOIN RecursiveEntity(" + subquery + ") t(id, name, parent.id)" + onClause("val = t.name");

        assertEquals(expected, cb.getQueryString());
        List<RecursiveEntity> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
    }

    // NOTE: H2 does not support lateral joins
    @Test
    @Category({ NoEclipselink.class, NoH2.class })
    public void testJoinLateralEntityWithLimit() {
        CriteriaBuilder<RecursiveEntity> cb = cbf.create(em, RecursiveEntity.class)
                .fromValues(RecursiveEntity.class, "name", "val", Collections.singletonList("child1_1"))
                .leftJoinLateralOnEntitySubquery(RecursiveEntity.class, "t", "r")
                    .where("r.parent.name").eq("root1")
                    .where("val").eqExpression("r.name")
                    .orderByAsc("name")
                    .setMaxResults(1)
                .end()
                    .on("1").eqExpression("1")
                .end()
                .select("t");
        String subquery = "SELECT r.id, r.name, r.parent.id FROM RecursiveEntity r LEFT JOIN r.parent parent_1 WHERE parent_1.name = :param_0 AND val = r.name ORDER BY r.name ASC LIMIT 1";
        String expected = ""
                + "SELECT t FROM String(1 VALUES LIKE RecursiveEntity.name) val LEFT JOIN LATERAL RecursiveEntity(" + subquery + ") t(id, name, parent.id)" + onClause("1 = 1");

        assertEquals(expected, cb.getQueryString());
        List<RecursiveEntity> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
    }

    // NOTE: H2 does not support lateral joins
    @Test
    @Category({ NoEclipselink.class, NoH2.class })
    public void testJoinLateralCorrelationEntityWithLimit() {
        CriteriaBuilder<RecursiveEntity> cb = cbf.create(em, RecursiveEntity.class)
                .from(RecursiveEntity.class, "x")
                .where("x.name").eq("root1")
                .leftJoinLateralOnEntitySubquery("x.children", "t", "r")
                    .orderByAsc("name")
                    .setMaxResults(1)
                .end()
                    .on("1").eqExpression("1")
                .end()
                .select("t");
        String expectedSubquery = "SELECT r.id, r.name, r.parent.id FROM x.children r ORDER BY r.name ASC LIMIT 1";
        String expected = ""
                + "SELECT t FROM RecursiveEntity x LEFT JOIN LATERAL RecursiveEntity(" + expectedSubquery + ") t(id, name, parent.id)" + onClause("1 = 1") + " WHERE x.name = :param_0";

        assertEquals(expected, cb.getQueryString());
        List<RecursiveEntity> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
    }

    // NOTE: H2 does not support lateral joins
    @Test
    @Category({ NoEclipselink.class, NoH2.class })
    public void testJoinLateralCorrelationEntityJoinTableWithLimit() {
        CriteriaBuilder<RecursiveEntity> cb = cbf.create(em, RecursiveEntity.class)
                .from(RecursiveEntity.class, "x")
                .where("x.name").eq("root1")
                .leftJoinLateralOnEntitySubquery("x.children2", "t", "r")
                    .orderByAsc("name")
                    .setMaxResults(1)
                .end()
                    .on("1").eqExpression("1")
                .end()
                .select("t");
        String expectedSubquery = "SELECT r.id, r.name, r.parent.id FROM x.children2 r ORDER BY r.name ASC LIMIT 1";
        String expected = ""
                + "SELECT t FROM RecursiveEntity x LEFT JOIN LATERAL RecursiveEntity(" + expectedSubquery + ") t(id, name, parent.id)" + onClause("1 = 1") + " WHERE x.name = :param_0";

        assertEquals(expected, cb.getQueryString());
        List<RecursiveEntity> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
    }

    // Test for #1193
    // NOTE: H2 does not support lateral joins
    @Test
    @Category({ NoEclipselink.class, NoH2.class })
    public void testJoinLateralWithArrayExpressionImplicitJoin() {
        CriteriaBuilder<RecursiveEntity> cb = cbf.create(em, RecursiveEntity.class)
                .from(RecursiveEntity.class, "x")
                .leftJoinLateralEntitySubquery("RecursiveEntity[name = x.parent.name]", "t", "r").end()
                .where("x.name").eq("root1")
                .select("t");
        String expectedSubquery = "SELECT r.id, r.name, r.parent.id FROM RecursiveEntity r, RecursiveEntity x_parent_base LEFT JOIN x_parent_base.parent parent_1 WHERE r.name = parent_1.name AND x.id = x_parent_base.id";
        String expected = ""
                + "SELECT t FROM RecursiveEntity x LEFT JOIN LATERAL RecursiveEntity(" + expectedSubquery + ") t(id, name, parent.id)" + onClause("1 = 1") + " WHERE x.name = :param_0";

        assertEquals(expected, cb.getQueryString());
        List<RecursiveEntity> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
    }

    // Additional test for #1193
    // NOTE: H2 does not support lateral joins
    @Test
    @Category({ NoEclipselink.class, NoH2.class })
    public void testJoinLateralWithElementArrayExpressionImplicitJoin() {
        CriteriaBuilder<RecursiveEntity> cb = cbf.create(em, RecursiveEntity.class)
                .from(RecursiveEntity.class, "x")
                .leftJoinLateralEntitySubquery("x.children[name = x.parent.name]", "t", "r").end()
                .where("x.name").eq("root1")
                .select("t");
        String expectedSubquery = "SELECT r.id, r.name, r.parent.id FROM RecursiveEntity x_children_base CROSS JOIN RecursiveEntity x_parent_base LEFT JOIN x_parent_base.parent parent_1 JOIN x_children_base.children r ON (r.name = parent_1.name) WHERE x.id = x_children_base.id AND x.id = x_parent_base.id";
        String expected = ""
                + "SELECT t FROM RecursiveEntity x LEFT JOIN LATERAL RecursiveEntity(" + expectedSubquery + ") t(id, name, parent.id)" + onClause("1 = 1") + " WHERE x.name = :param_0";

        assertEquals(expected, cb.getQueryString());
        List<RecursiveEntity> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
    }
}
