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

import static com.googlecode.catchexception.CatchException.caughtException;
import static com.googlecode.catchexception.CatchException.verifyException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;

import com.blazebit.persistence.FullSelectCTECriteriaBuilder;
import com.blazebit.persistence.LeafOngoingSetOperationCTECriteriaBuilder;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.impl.BuilderChainingException;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoH2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.entity.TestAdvancedCTE1;
import com.blazebit.persistence.testsuite.entity.TestAdvancedCTE2;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.RecursiveEntity;
import com.blazebit.persistence.testsuite.entity.TestCTE;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public class CTETest extends AbstractCoreTest {
    
    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
            RecursiveEntity.class,
            TestCTE.class,
            TestAdvancedCTE1.class,
            TestAdvancedCTE2.class
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
            }
        });
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class })
    public void testNotFullyBoundCTE() {
        CriteriaBuilder<TestCTE> cb = cbf.create(em, TestCTE.class, "t");
        FullSelectCTECriteriaBuilder<CriteriaBuilder<TestCTE>> fullSelectCTECriteriaBuilder = cb.with(TestCTE.class)
                .from(RecursiveEntity.class, "e")
                .bind("id").select("e.id");

        verifyException(fullSelectCTECriteriaBuilder, IllegalStateException.class).end();
        // Assert that these columns haven't been bound
        assertTrue(caughtException().getMessage().contains("name"));
        assertTrue(caughtException().getMessage().contains("nesting_level"));
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class })
    public void testNotFullyBoundCTEOnSetOperation() {
        CriteriaBuilder<TestCTE> cb = cbf.create(em, TestCTE.class);
        FullSelectCTECriteriaBuilder<CriteriaBuilder<TestCTE>> builder = cb.with(TestCTE.class)
                .from(RecursiveEntity.class, "e")
                .bind("id").select("e.id")
                .bind("name").select("e.name")
                .where("e.parent").isNull();
        verifyException(builder, IllegalStateException.class).unionAll();
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class })
    public void testNotFullyBoundCTEOnSecondSetOperation() {
        CriteriaBuilder<TestCTE> cb = cbf.create(em, TestCTE.class);
        LeafOngoingSetOperationCTECriteriaBuilder<CriteriaBuilder<TestCTE>> builder = cb.with(TestCTE.class)
                    .from(RecursiveEntity.class, "e")
                    .bind("id").select("e.id")
                    .bind("name").select("e.name")
                    .bind("level").select("0")
                    .where("e.parent").isNull()
                .unionAll()
                    .from(RecursiveEntity.class, "e")
                    .bind("id").select("e.id")
                    .bind("name").select("e.name")
                    .where("e.parent").isNull()
                ;
        verifyException(builder, IllegalStateException.class).endSet();
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class })
    public void testNotDefinedCTE() {
        CriteriaBuilder<TestCTE> cb = cbf.create(em, TestCTE.class, "t");

        verifyException(cb, IllegalStateException.class).getQueryString();
        // Assert the undefined cte
        assertTrue(caughtException().getMessage().contains(TestCTE.class.getName()));
    }
    
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class })
    public void testCTE() {
        CriteriaBuilder<TestCTE> cb = cbf.create(em, TestCTE.class, "t").where("t.level").ltExpression("2");
        cb.with(TestCTE.class)
            .from(RecursiveEntity.class, "e")
            .bind("id").select("e.id")
            .bind("name").select("e.name")
            .bind("level").select("0")
            .where("e.parent").isNull()
        .end();
        String expected = ""
                + "WITH " + TestCTE.class.getSimpleName() + "(id, name, level) AS(\n"
                + "SELECT e.id, e.name, 0 FROM RecursiveEntity e WHERE e.parent IS NULL"
                + "\n)\n"
                + "SELECT t FROM " + TestCTE.class.getSimpleName() + " t WHERE t.level < 2";
        
        assertEquals(expected, cb.getQueryString());
        List<TestCTE> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals("root1", resultList.get(0).getName());
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class })
    public void testCTEAdvanced() {
        CriteriaBuilder<TestAdvancedCTE1> cb = cbf.create(em, TestAdvancedCTE1.class, "t").where("t.level").ltExpression("2");
        cb.with(TestAdvancedCTE1.class)
            .from(RecursiveEntity.class, "e")
            .bind("id").select("e.id")
            .bind("embeddable.name").select("e.name")
            .bind("embeddable.description").select("'desc'")
            .bind("embeddable.recursiveEntity").select("e")
            .bind("level").select("0")
            .bind("parent").select("e.parent")
            .where("e.parent").isNull()
        .end();
        String expected = ""
                + "WITH " + TestAdvancedCTE1.class.getSimpleName() + "(id, embeddable.name, embeddable.description, embeddable.recursiveEntity.id, level, parent.id) AS(\n"
                // NOTE: The parent relation select gets transformed to an id select!
                + "SELECT e.id, e.name, 'desc', e.id, 0, e.parent.id FROM RecursiveEntity e WHERE e.parent IS NULL"
                + "\n)\n"
                + "SELECT t FROM " + TestAdvancedCTE1.class.getSimpleName() + " t WHERE t.level < 2";

        assertEquals(expected, cb.getQueryString());
        List<TestAdvancedCTE1> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals("root1", resultList.get(0).getEmbeddable().getName());
        assertEquals("desc", resultList.get(0).getEmbeddable().getDescription());
    }

    // NOTE: Apparently H2 doesn't like limit in CTEs
    @Test
    @Category({ NoH2.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class })
    public void testCTELimit() {
        CriteriaBuilder<TestCTE> cb = cbf.create(em, TestCTE.class, "t");
        cb.with(TestCTE.class)
            .from(RecursiveEntity.class, "e")
            .bind("id").select("e.id")
            .bind("name").select("e.name")
            .bind("level").select("0")
            .where("e.parent").isNull()
            .orderByAsc("e.id")
            .setMaxResults(1)
        .end();
        String expected = ""
                + "WITH " + TestCTE.class.getSimpleName() + "(id, name, level) AS(\n"
                + "SELECT e.id, e.name, 0 FROM RecursiveEntity e WHERE e.parent IS NULL ORDER BY e.id ASC LIMIT 1"
                + "\n)\n"
                + "SELECT t FROM " + TestCTE.class.getSimpleName() + " t";
        
        assertEquals(expected, cb.getQueryString());
        List<TestCTE> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals("root1", resultList.get(0).getName());
    }

    // TODO: Oracle requires a cycle clause #295
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class, NoOracle.class })
    public void testRecursiveCTE() {
        CriteriaBuilder<TestCTE> cb = cbf.create(em, TestCTE.class, "t").where("t.level").ltExpression("2");
        cb.withRecursive(TestCTE.class)
            .from(RecursiveEntity.class, "e")
            .bind("id").select("e.id")
            .bind("name").select("e.name")
            .bind("level").select("0")
            .where("e.parent").isNull()
        .unionAll()
            .from(TestCTE.class, "t")
            .innerJoinOn(RecursiveEntity.class, "e")
                .on("t.id").eqExpression("e.parent.id")
            .end()
            .bind("id").select("e.id")
            .bind("name").select("e.name")
            .bind("level").select("t.level + 1")
        .end();
        String expected = ""
                + "WITH RECURSIVE " + TestCTE.class.getSimpleName() + "(id, name, level) AS(\n"
                + "SELECT e.id, e.name, 0 FROM RecursiveEntity e WHERE e.parent IS NULL"
                + "\nUNION ALL\n"
                + "SELECT e.id, e.name, t.level + 1 FROM " + TestCTE.class.getSimpleName() + " t" + innerJoinRecursive("RecursiveEntity e", "t.id = e.parent.id")
                + "\n)\n"
                + "SELECT t FROM " + TestCTE.class.getSimpleName() + " t WHERE t.level < 2";
        
        assertEquals(expected, cb.getQueryString());
        List<TestCTE> resultList = cb.getResultList();
        assertEquals(3, resultList.size());
        assertEquals("root1", resultList.get(0).getName());
    }

    // TODO: Oracle requires a cycle clause #295
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class, NoOracle.class })
    public void testPaginationWithRecursiveCte() {
        CriteriaBuilder<RecursiveEntity> cb = cbf.create(em, RecursiveEntity.class).orderByAsc("id");
        // The CTE doesn't really matter, it's just there to trigger #570
        cb.withRecursive(TestCTE.class)
                    .from(RecursiveEntity.class, "e")
                    .bind("id").select("e.id")
                    .bind("name").select("e.name")
                    .bind("level").select("0")
                    .where("e.parent").isNull()
                .unionAll()
                    .from(TestCTE.class, "t")
                    .innerJoinOn(RecursiveEntity.class, "e")
                        .on("t.id").eqExpression("e.parent.id")
                    .end()
                    .bind("id").select("e.id")
                    .bind("name").select("e.name")
                    .bind("level").select("t.level + 1")
                .end();
        List<RecursiveEntity> resultList = cb.setMaxResults(1)
                .getQuery()
                .getResultList();
        assertEquals(1, resultList.size());
    }

    // TODO: Oracle requires a cycle clause #295
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class, NoOracle.class })
    public void testRecursiveCTEAdvanced() {
        CriteriaBuilder<TestAdvancedCTE1> cb = cbf.create(em, TestAdvancedCTE1.class, "t").where("t.level").ltExpression("2");
        cb.withRecursive(TestAdvancedCTE1.class)
            .from(RecursiveEntity.class, "e")
            .bind("id").select("e.id")
            .bind("embeddable.name").select("e.name")
            .bind("embeddable.description").select("'desc'")
            .bind("embeddable.recursiveEntity").select("e")
            .bind("level").select("0")
            .bind("parentId").select("e.parent.id")
            .where("e.parent").isNull()
        .unionAll()
            .from(TestAdvancedCTE1.class, "t")
            .innerJoinOn(RecursiveEntity.class, "e")
                .on("t.id").eqExpression("e.parent.id")
            .end()
            .bind("id").select("e.id")
            .bind("embeddable.name").select("e.name")
            .bind("embeddable.description").select("'desc'")
            .bind("embeddable.recursiveEntity").select("e")
            .bind("level").select("t.level + 1")
            .bind("parent").select("e.parent")
        .end();
        String expected = ""
                + "WITH RECURSIVE " + TestAdvancedCTE1.class.getSimpleName() + "(id, embeddable.name, embeddable.description, embeddable.recursiveEntity.id, level, parentId) AS(\n"
                + "SELECT e.id, e.name, 'desc', e.id, 0, e.parent.id FROM RecursiveEntity e WHERE e.parent IS NULL"
                + "\nUNION ALL\n"
                // NOTE: The parent relation select gets transformed to an id select!
                + "SELECT e.id, e.name, 'desc', e.id, t.level + 1, e.parent.id FROM " + TestAdvancedCTE1.class.getSimpleName() + " t" + innerJoinRecursive("RecursiveEntity e", "t.id = e.parent.id")
                + "\n)\n"
                + "SELECT t FROM " + TestAdvancedCTE1.class.getSimpleName() + " t WHERE t.level < 2";

        assertEquals(expected, cb.getQueryString());
        List<TestAdvancedCTE1> resultList = cb.getResultList();
        assertEquals(3, resultList.size());
        assertEquals("root1", resultList.get(0).getEmbeddable().getName());
        assertEquals("desc", resultList.get(0).getEmbeddable().getDescription());
    }

    // TODO: Oracle requires a cycle clause #295
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class, NoOracle.class })
    public void testRecursiveCTEPagination() {
        CriteriaBuilder<TestCTE> cb = cbf.create(em, TestCTE.class);
        cb.withRecursive(TestCTE.class)
                .from(RecursiveEntity.class, "e")
                .bind("id").select("e.id")
                .bind("name").select("e.name")
                .bind("level").select("0")
                .where("e.parent").isNull()
        .unionAll()
                .from(TestCTE.class, "t")
                .innerJoinOn(RecursiveEntity.class, "e")
                    .on("t.id").eqExpression("e.parent.id")
                .end()
                .bind("id").select("e.id")
                .bind("name").select("e.name")
                .bind("level").select("t.level + 1")
        .end();
        cb.from(TestCTE.class, "t")
                .where("t.level").ltExpression("2")
                .orderByAsc("t.level")
                .orderByAsc("t.id");

        PaginatedCriteriaBuilder<TestCTE> pcb = cb.page(0, 1);

        String expectedCountQuery = ""
                + "WITH RECURSIVE " + TestCTE.class.getSimpleName() + "(id, name, level) AS(\n"
                + "SELECT e.id, e.name, 0 FROM RecursiveEntity e WHERE e.parent IS NULL"
                + "\nUNION ALL\n"
                + "SELECT e.id, e.name, t.level + 1 FROM " + TestCTE.class.getSimpleName() + " t" + innerJoinRecursive("RecursiveEntity e", "t.id = e.parent.id")
                + "\n)\n"
                + "SELECT " + countPaginated("t.id", false) + " FROM " + TestCTE.class.getSimpleName() + " t WHERE t.level < 2";

        String expectedObjectQuery = ""
                + "WITH RECURSIVE " + TestCTE.class.getSimpleName() + "(id, name, level) AS(\n"
                + "SELECT e.id, e.name, 0 FROM RecursiveEntity e WHERE e.parent IS NULL"
                + "\nUNION ALL\n"
                + "SELECT e.id, e.name, t.level + 1 FROM " + TestCTE.class.getSimpleName() + " t" + innerJoinRecursive("RecursiveEntity e", "t.id = e.parent.id")
                + "\n)\n"
                + "SELECT t FROM " + TestCTE.class.getSimpleName() + " t WHERE t.level < 2 ORDER BY t.level ASC, t.id ASC";

        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());

        PagedList<TestCTE> resultList = pcb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals(3, resultList.getTotalSize());
        assertEquals("root1", resultList.get(0).getName());

        pcb = cb.page(1, 1);
        resultList = pcb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals(3, resultList.getTotalSize());
        assertEquals("child1_1", resultList.get(0).getName());

        pcb = cb.page(2, 1);
        resultList = pcb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals(3, resultList.getTotalSize());
        assertEquals("child1_2", resultList.get(0).getName());

        pcb = cb.page(0, 2);
        resultList = pcb.getResultList();
        assertEquals(2, resultList.size());
        assertEquals(3, resultList.getTotalSize());
        assertEquals("root1", resultList.get(0).getName());
        assertEquals("child1_1", resultList.get(1).getName());
    }

    // NOTE: Apparently H2 produces wrong results when a CTE is used with IN predicate
    // TODO: Oracle requires a cycle clause #295
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class, NoH2.class, NoOracle.class })
    public void testRecursiveCTEPaginationIdQuery() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.withRecursive(TestCTE.class)
        .from(RecursiveEntity.class, "e")
            .bind("id").select("e.id")
            .bind("name").select("e.name")
            .bind("level").select("0")
            .where("e.parent").isNull()
        .unionAll()
            .from(TestCTE.class, "t")
            .innerJoinOn(RecursiveEntity.class, "e")
                .on("t.id").eqExpression("e.parent.id")
            .end()
            .bind("id").select("e.id")
            .bind("name").select("e.name")
            .bind("level").select("t.level + 1")
        .end();
        cb.from(RecursiveEntity.class, "r")
            .select("r.name")
            .select("r.children.name")
            .where("r.id").in()
                .from(TestCTE.class, "t")
                .select("t.id")
                .where("t.level").ltExpression("2")
            .end()
            .orderByAsc("r.id");

        PaginatedCriteriaBuilder<Tuple> pcb = cb.page(0, 2);

        String expectedCountQuery = ""
                + "WITH RECURSIVE TestCTE(id, name, level) AS(\n"
                + "SELECT e.id, e.name, 0 FROM RecursiveEntity e WHERE e.parent IS NULL"
                + "\nUNION ALL\n"
                + "SELECT e.id, e.name, t.level + 1 FROM TestCTE t" + innerJoinRecursive("RecursiveEntity e", "t.id = e.parent.id")
                + "\n)\n"
                + "SELECT " + countPaginated("r.id", false) + " FROM RecursiveEntity r WHERE r.id IN (SELECT t.id FROM TestCTE t WHERE t.level < 2)";

        // TODO: check if we can somehow infer that the order by of children.id is unnecessary and thus be able to omit the join
        String expectedIdQuery = ""
                + "WITH RECURSIVE TestCTE(id, name, level) AS(\n"
                + "SELECT e.id, e.name, 0 FROM RecursiveEntity e WHERE e.parent IS NULL"
                + "\nUNION ALL\n"
                + "SELECT e.id, e.name, t.level + 1 FROM TestCTE t" + innerJoinRecursive("RecursiveEntity e", "t.id = e.parent.id")
                + "\n)\n"
                + "SELECT r.id FROM RecursiveEntity r WHERE r.id IN (SELECT t.id FROM TestCTE t WHERE t.level < 2) GROUP BY r.id ORDER BY r.id ASC";

        String expectedObjectQuery = ""
                + "WITH RECURSIVE TestCTE(id, name, level) AS(\n"
                + "SELECT e.id, e.name, 0 FROM RecursiveEntity e WHERE e.parent IS NULL"
                + "\nUNION ALL\n"
                + "SELECT e.id, e.name, t.level + 1 FROM TestCTE t" + innerJoinRecursive("RecursiveEntity e", "t.id = e.parent.id")
                + "\n)\n"
                + "SELECT r.name, children_1.name FROM RecursiveEntity r LEFT JOIN r.children children_1 WHERE r.id IN :ids ORDER BY r.id ASC";

        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedIdQuery, pcb.getPageIdQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());

        PagedList<Tuple> resultList = pcb.getResultList();
        // Well, it's a tuple and contains collections, so result list contains "more" results
        assertEquals(3, resultList.size());
        assertEquals(3, resultList.getTotalSize());

        // Unfortunately we can't specify order by just for the object query to determinisitcally test this and a comparator is too much, so we do it like that
        if ("child1_1".equals(resultList.get(0).get(1))) {
            assertEquals("root1", resultList.get(0).get(0));
            assertEquals("child1_1", resultList.get(0).get(1));

            assertEquals("root1", resultList.get(1).get(0));
            assertEquals("child1_2", resultList.get(1).get(1));
        } else {
            assertEquals("root1", resultList.get(0).get(0));
            assertEquals("child1_2", resultList.get(0).get(1));

            assertEquals("root1", resultList.get(1).get(0));
            assertEquals("child1_1", resultList.get(1).get(1));
        }

        assertEquals("child1_1", resultList.get(2).get(0));
        assertEquals("child1_1_1", resultList.get(2).get(1));
    }

    // TODO: Oracle requires a cycle clause #295
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class, NoOracle.class })
    public void testRecursiveCTEPaginationIdQueryLeftJoin() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.withRecursive(TestCTE.class)
            .from(RecursiveEntity.class, "e")
            .bind("id").select("e.id")
            .bind("name").select("e.name")
            .bind("level").select("0")
            .where("e.parent").isNull()
        .unionAll()
            .from(TestCTE.class, "t")
            .innerJoinOn(RecursiveEntity.class, "e")
                .on("t.id").eqExpression("e.parent.id")
            .end()
            .bind("id").select("e.id")
            .bind("name").select("e.name")
            .bind("level").select("t.level + 1")
        .end();
        cb.from(RecursiveEntity.class, "r")
            .select("r.name")
            .select("r.children.name")
            .innerJoinOn(TestCTE.class, "t")
                .on("r.id").eqExpression("t.id")
                .on("t.level").ltExpression("2")
            .end()
            .orderByAsc("r.id");

        PaginatedCriteriaBuilder<Tuple> pcb = cb.page(0, 2);

        String expectedCountQuery = ""
                + "WITH RECURSIVE TestCTE(id, name, level) AS(\n"
                + "SELECT e.id, e.name, 0 FROM RecursiveEntity e WHERE e.parent IS NULL"
                + "\nUNION ALL\n"
                + "SELECT e.id, e.name, t.level + 1 FROM TestCTE t" + innerJoinRecursive("RecursiveEntity e", "t.id = e.parent.id")
                + "\n)\n"
                + "SELECT " + countPaginated("r.id", true) + " FROM RecursiveEntity r" + innerJoin("TestCTE t", "r.id = t.id AND t.level < 2");

        String expectedIdQuery = ""
                + "WITH RECURSIVE TestCTE(id, name, level) AS(\n"
                + "SELECT e.id, e.name, 0 FROM RecursiveEntity e WHERE e.parent IS NULL"
                + "\nUNION ALL\n"
                + "SELECT e.id, e.name, t.level + 1 FROM TestCTE t" + innerJoinRecursive("RecursiveEntity e", "t.id = e.parent.id")
                + "\n)\n"
                + "SELECT r.id FROM RecursiveEntity r" + innerJoin("TestCTE t", "r.id = t.id AND t.level < 2")
                + " GROUP BY r.id ORDER BY r.id ASC";

        String expectedObjectQuery = ""
                + "WITH RECURSIVE TestCTE(id, name, level) AS(\n"
                + "SELECT e.id, e.name, 0 FROM RecursiveEntity e WHERE e.parent IS NULL"
                + "\nUNION ALL\n"
                + "SELECT e.id, e.name, t.level + 1 FROM TestCTE t" + innerJoinRecursive("RecursiveEntity e", "t.id = e.parent.id")
                + "\n)\n"
                + "SELECT r.name, children_1.name FROM RecursiveEntity r LEFT JOIN r.children children_1" + innerJoin("TestCTE t", "r.id = t.id AND t.level < 2", "r.id IN :ids")
                + " ORDER BY r.id ASC";

        assertEquals(expectedCountQuery, pcb.getPageCountQueryString());
        assertEquals(expectedIdQuery, pcb.getPageIdQueryString());
        assertEquals(expectedObjectQuery, pcb.getQueryString());

        PagedList<Tuple> resultList = pcb.getResultList();
        // Well, it's a tuple and contains collections, so result list contains "more" results
        assertEquals(3, resultList.size());
        assertEquals(3, resultList.getTotalSize());
        // Unfortunately we can't specify order by just for the object query to deterministically test this and a comparator is too much, so we do it like that
        if ("child1_1".equals(resultList.get(0).get(1))) {
            assertEquals("root1", resultList.get(0).get(0));
            assertEquals("child1_1", resultList.get(0).get(1));

            assertEquals("root1", resultList.get(1).get(0));
            assertEquals("child1_2", resultList.get(1).get(1));
        } else {
            assertEquals("root1", resultList.get(0).get(0));
            assertEquals("child1_2", resultList.get(0).get(1));

            assertEquals("root1", resultList.get(1).get(0));
            assertEquals("child1_1", resultList.get(1).get(1));
        }
    }
    
    // NOTE: Apparently H2 can't handle multiple CTEs
    @Test
    @Category({ NoH2.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class })
    public void testCTEInSubquery() {
        CriteriaBuilder<String> cb = cbf.create(em, String.class)
            .from(RecursiveEntity.class, "r")
            .where("r.id").in()
                .from(TestCTE.class, "a")
                .where("a.level").ltExpression("2")
                .select("a.id")
            .end()
            .select("r.name");
        cb.with(TestCTE.class)
            .from(RecursiveEntity.class, "e")
            .bind("id").select("e.id")
            .bind("name").select("e.name")
            .bind("level").select("0")
            .where("e.parent").isNull()
        .end();
        String expected = ""
                + "WITH " + TestCTE.class.getSimpleName() + "(id, name, level) AS(\n"
                + "SELECT e.id, e.name, 0 FROM RecursiveEntity e WHERE e.parent IS NULL"
                + "\n)\n"
                + "SELECT r.name FROM RecursiveEntity r WHERE r.id IN (SELECT a.id FROM " + TestCTE.class.getSimpleName() + " a WHERE a.level < 2)";
        
        assertEquals(expected, cb.getQueryString());
        List<String> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals("root1", resultList.get(0));
    }

    // NOTE: Apparently H2 can't handle multiple CTEs
    @Test
    @Category({ NoH2.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class })
    public void testBindEmbeddable() {
        CriteriaBuilder<TestAdvancedCTE2> cb = cbf.create(em, TestAdvancedCTE2.class);
        cb.with(TestAdvancedCTE1.class)
            .from(RecursiveEntity.class, "e")
            .bind("id").select("e.id")
            .bind("embeddable.name").select("e.name")
            .bind("embeddable.description").select("'desc'")
            .bind("embeddable.recursiveEntity").select("e")
            .bind("level").select("0")
            .bind("parent").select("e.parent")
        .end()
        .with(TestAdvancedCTE2.class)
            .from(TestAdvancedCTE1.class)
            .bind("id").select("id")
            .bind("embeddable").select("embeddable")
        .end()
        .orderByAsc("id");
        String expected = ""
                + "WITH " + TestAdvancedCTE1.class.getSimpleName() + "(id, embeddable.name, embeddable.description, embeddable.recursiveEntity.id, level, parent.id) AS(\n"
                + "SELECT e.id, e.name, 'desc', e.id, 0, e.parent.id FROM RecursiveEntity e\n"
                + "), " + TestAdvancedCTE2.class.getSimpleName() + "(id, embeddable.description, embeddable.name, embeddable.recursiveEntity.id) AS(\n" +
                "SELECT testAdvancedCTE1.id, testAdvancedCTE1.embeddable.description, testAdvancedCTE1.embeddable.name, testAdvancedCTE1.embeddable.recursiveEntity.id FROM TestAdvancedCTE1 testAdvancedCTE1\n" +
                ")\n"
                + "SELECT testAdvancedCTE2 FROM TestAdvancedCTE2 testAdvancedCTE2 ORDER BY testAdvancedCTE2.id ASC";

        assertEquals(expected, cb.getQueryString());
        List<TestAdvancedCTE2> results = cb.getResultList();
        assertEquals(5, results.size());
        for (TestAdvancedCTE2 result : results) {
            assertEquals("desc", result.getEmbeddable().getDescription());
        }
        assertEquals("root1", results.get(0).getEmbeddable().getName());
        assertEquals("child1_1", results.get(1).getEmbeddable().getName());
        assertEquals("child1_2", results.get(2).getEmbeddable().getName());
        assertEquals("child1_1_1", results.get(3).getEmbeddable().getName());
        assertEquals("child1_2_1", results.get(4).getEmbeddable().getName());
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class })
    public void testBindEmbeddableWithNullBindingsForJoinableAttributes() {
        CriteriaBuilder<TestAdvancedCTE1> cb = cbf.create(em, TestAdvancedCTE1.class);
        cb.with(TestAdvancedCTE1.class)
            .from(RecursiveEntity.class, "e")
            .bind("id").select("e.id")
            .bind("embeddable.name").select("e.name")
            .bind("embeddable.description").select("'desc'")
            .bind("embeddable.recursiveEntity").select("NULL")
            .bind("level").select("0")
            .bind("parent").select("NULL")
        .end()
        .orderByAsc("id");
        String expected = ""
                + "WITH " + TestAdvancedCTE1.class.getSimpleName() + "(id, embeddable.name, embeddable.description, embeddable.recursiveEntity, level, parent) AS(\n"
                + "SELECT e.id, e.name, 'desc', NULLIF(1,1), 0, NULLIF(1,1) FROM RecursiveEntity e\n"
                + ")\n"
                + "SELECT testAdvancedCTE1 FROM TestAdvancedCTE1 testAdvancedCTE1 ORDER BY testAdvancedCTE1.id ASC";

        assertEquals(expected, cb.getQueryString());

        List<TestAdvancedCTE1> results = cb.getResultList();
        assertEquals(5, results.size());
        for (TestAdvancedCTE1 result : results) {
            assertEquals("desc", result.getEmbeddable().getDescription());
        }
        assertEquals("root1", results.get(0).getEmbeddable().getName());
        assertEquals("child1_1", results.get(1).getEmbeddable().getName());
        assertEquals("child1_2", results.get(2).getEmbeddable().getName());
        assertEquals("child1_1_1", results.get(3).getEmbeddable().getName());
        assertEquals("child1_2_1", results.get(4).getEmbeddable().getName());
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class })
    public void testWithStartSetEmptyRightSide() {
        CriteriaBuilder<TestAdvancedCTE1> cb = cbf.create(em, TestAdvancedCTE1.class);
        cb.withStartSet(TestAdvancedCTE1.class)
                .from(RecursiveEntity.class, "e")
                .bind("id").select("e.id")
                .bind("embeddable.name").select("e.name")
                .bind("embeddable.description").select("'desc'")
                .bind("embeddable.recursiveEntity").select("NULL")
                .bind("level").select("0")
                .bind("parent").select("NULL")
                .endSet()
            .endSet()
        .end();

        String expected = ""
                + "WITH " + TestAdvancedCTE1.class.getSimpleName() + "(id, embeddable.name, embeddable.description, embeddable.recursiveEntity, level, parent) AS(\n"
                + "SELECT e.id, e.name, 'desc', NULLIF(1,1), 0, NULLIF(1,1) FROM RecursiveEntity e\n"
                + ")\n"
                + "SELECT testAdvancedCTE1 FROM TestAdvancedCTE1 testAdvancedCTE1";
        assertEquals(expected, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class })
    public void testWithStartSetEmptyRightSideLeaf() {
        CriteriaBuilder<TestAdvancedCTE1> cb = cbf.create(em, TestAdvancedCTE1.class);
        cb.withStartSet(TestAdvancedCTE1.class)
                .from(RecursiveEntity.class, "e")
                .bind("id").select("e.id")
                .bind("embeddable.name").select("e.name")
                .bind("embeddable.description").select("''")
                .bind("embeddable.recursiveEntity").select("NULL")
                .bind("level").select("0")
                .bind("parent").select("NULL")
                .endSet()
                .unionAll()
            .endSet()
        .end();

        String expected = ""
                + "WITH " + TestAdvancedCTE1.class.getSimpleName() + "(id, embeddable.name, embeddable.description, embeddable.recursiveEntity, level, parent) AS(\n"
                + "SELECT e.id, e.name, '', NULLIF(1,1), 0, NULLIF(1,1) FROM RecursiveEntity e\n"
                + ")\n"
                + "SELECT testAdvancedCTE1 FROM TestAdvancedCTE1 testAdvancedCTE1";
        assertEquals(expected, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    // NOTE: H2 does not support the PARTITION clause in the ROW_NUMBER function, so we can't emulate EXCEPT ALL
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoH2.class, NoMySQL.class })
    public void testWithStartSetEmptyLeftSideLeaf() {
        CriteriaBuilder<TestCTE> cb = cbf.create(em, TestCTE.class).withStartSet(TestCTE.class)
            .from(RecursiveEntity.class, "e")
            .bind("id").select("e.id")
            .bind("name").select("e.name")
            .bind("level").select("0")
        .exceptAll()
            .from(RecursiveEntity.class, "e")
            .bind("id").select("e.id")
            .bind("name").select("e.name")
            .bind("level").select("0")
        .endSet()
        .startUnionAll().endSet()
        .endSet().end();

        String expected = ""
                + "WITH " + TestCTE.class.getSimpleName() + "(id, name, level) AS(\n"
                + "(SELECT e.id, e.name, 0 FROM RecursiveEntity e\n"
                + "EXCEPT ALL\n"
                + "SELECT e.id, e.name, 0 FROM RecursiveEntity e)\n"
                + ")\n"
                + "SELECT testCTE FROM " + TestCTE.class.getSimpleName() + " testCTE";
        assertEquals(expected, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class })
    public void testBuilderEndTracking() {
        FullSelectCTECriteriaBuilder<CriteriaBuilder<TestCTE>> cb = cbf.create(em, TestCTE.class).with(TestCTE.class);
        cb.from(RecursiveEntity.class, "e")
            .bind("id").select("e.id")
            .bind("name").select("e.name")
            .bind("level").select("0")
        .unionAll()
            .from(RecursiveEntity.class, "e")
            .bind("id").select("e.id")
            .bind("name").select("e.name")
            .bind("level").select("0")
        .endSet();

        verifyException(cb, BuilderChainingException.class).end();
    }

    // from issue #513
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class })
    public void testNestedSizeInCte() {
        CriteriaBuilder<Long> cb = cbf.create(em, Long.class)
                .with(TestCTE.class)
                    .from(RecursiveEntity.class, "r")
                    .bind("id").select("r.id")
                    .bind("level").select("FUNCTION('greatest', SIZE(r.children), 1) * r.id")
                    .bind("name").select("''")
                .end()
                .from(TestCTE.class)
                .select("level");

        String expected = "WITH TestCTE(id, level, name) AS(\n" +
                "SELECT r.id, " + function("greatest", function("count_tuple", "children_1.id"), "1") + " * r.id, '' FROM RecursiveEntity r LEFT JOIN r.children children_1 GROUP BY r.id\n" +
                ")\n" +
                "SELECT testCTE.level FROM TestCTE testCTE";
        assertEquals(expected, cb.getQueryString());
    }

    private String innerJoin(String entityFragment, String onPredicate) {
        if (jpaProvider.supportsEntityJoin()) {
            return " JOIN " + entityFragment + onClause(onPredicate);
        } else {
            return ", " + entityFragment + " WHERE " + onPredicate;
        }
    }

    private String innerJoin(String entityFragment, String onPredicate, String wherePredicate) {
        if (jpaProvider.supportsEntityJoin()) {
            return " JOIN " + entityFragment + onClause(onPredicate) + " WHERE " + wherePredicate;
        } else {
            return ", " + entityFragment + " WHERE " + wherePredicate + " AND " + onPredicate;
        }
    }

    private String innerJoinRecursive(String entityFragment, String onPredicate) {
        if (jpaProvider.supportsEntityJoin() && dbmsDialect.supportsJoinsInRecursiveCte()) {
            return " JOIN " + entityFragment + onClause(onPredicate);
        } else {
            return ", " + entityFragment + " WHERE " + onPredicate;
        }
    }
}
