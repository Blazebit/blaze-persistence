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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoH2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate51;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQLOld;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.entity.ParameterOrderCte;
import com.blazebit.persistence.testsuite.entity.ParameterOrderCteB;
import com.blazebit.persistence.testsuite.entity.ParameterOrderEntity;
import com.blazebit.persistence.testsuite.entity.RecursiveEntity;
import com.blazebit.persistence.testsuite.entity.TestAdvancedCTE1;
import com.blazebit.persistence.testsuite.entity.TestAdvancedCTE2;
import com.blazebit.persistence.testsuite.entity.TestCTE;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;

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

    // TODO: Oracle requires a cycle clause #295
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQLOld.class, NoOracle.class })
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
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQLOld.class, NoOracle.class })
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

    // NOTE: Hibernate 4.2 and 4.3 interprets entity name tokens in string literals...
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoHibernate42.class, NoHibernate43.class })
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

    // NOTE: Hibernate 4.2 and 4.3 interprets entity name tokens in string literals...
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoHibernate42.class, NoHibernate43.class })
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
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
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

    // NOTE: Hibernate 4.2 and 4.3 interprets entity name tokens in string literals...
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoHibernate42.class, NoHibernate43.class })
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
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
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
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
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
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
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

    // NOTE: Hibernate 4.2 and 4.3 interprets entity name tokens in string literals...
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoHibernate42.class, NoHibernate43.class })
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
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
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

    // NOTE: Entity joins are only supported on Hibernate 5.1+
    // NOTE: Hibernate 5.1 renders t.id = tSub.id rather than t = tSub
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class})
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

    // NOTE: Entity joins are only supported on Hibernate 5.1+
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoHibernate42.class, NoHibernate43.class, NoHibernate50.class })
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

    // NOTE: H2 and old MySQL do not support lateral joins
    // NOTE: Our Oracle version does not support lateral yet, only after 12c
    // NOTE: Entity joins are only supported on Hibernate 5.1+
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoH2.class, NoMySQLOld.class, NoOracle.class, NoHibernate42.class, NoHibernate43.class, NoHibernate50.class })
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

    // NOTE: H2 and old MySQL do not support lateral joins
    // NOTE: Our Oracle version does not support lateral yet, only after 12c
    // NOTE: Entity joins are only supported on Hibernate 5.1+
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoH2.class, NoMySQLOld.class, NoOracle.class, NoHibernate42.class, NoHibernate43.class, NoHibernate50.class })
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
        String expectedSubquery = "SELECT r.id, r.name, r.parent.id FROM RecursiveEntity r WHERE r.parent.id = x.id ORDER BY r.name ASC LIMIT 1";
        String expected = ""
                + "SELECT t FROM RecursiveEntity x LEFT JOIN LATERAL RecursiveEntity(" + expectedSubquery + ") t(id, name, parent.id)" + onClause("1 = 1") + " WHERE x.name = :param_0";

        assertEquals(expected, cb.getQueryString());
        List<RecursiveEntity> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
    }

    // NOTE: H2 and old MySQL do not support lateral joins
    // NOTE: Our Oracle version does not support lateral yet, only after 12c
    // NOTE: Entity joins are only supported on Hibernate 5.1+
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoH2.class, NoMySQLOld.class, NoOracle.class, NoHibernate42.class, NoHibernate43.class, NoHibernate50.class })
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
}
