/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQLOld;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.entity.RecursiveEntity;
import com.blazebit.persistence.testsuite.entity.TestCTE;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test for issue #2091.
 * <p>
 * Oracle CTE pagination previously wrapped the entire statement (including {@code WITH}) with a
 * {@code ROWNUM} filter via the JPA provider limit handler, causing invalid SQL / invalid column index
 * when offset+limit were used. Pagination must keep the CTE at the top level and still return the
 * correct page.
 * <p>
 * Oracle recursive CTEs need a cycle clause (#295), so runtime is skipped on Oracle; the SQL-shape
 * unit coverage lives in {@code OracleDbmsLimitHandlerTest}.
 *
 * @author arimu1
 * @since 1.6.21
 */
public class Issue2091Test extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            RecursiveEntity.class,
            TestCTE.class
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

    // TODO: Oracle requires a cycle clause #295 — SQL shape is covered by OracleDbmsLimitHandlerTest
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQLOld.class, NoOracle.class })
    public void testCtePaginationWithOffsetAndLimit() {
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
                .orderByAsc("t.id")
                .setFirstResult(2)
                .setMaxResults(3);

        List<TestCTE> resultList = cb.getResultList();
        // levels < 2: root1, child1_1, child1_2 — offset 2, limit 3 → only child1_2
        assertEquals(1, resultList.size());
        assertEquals("child1_2", resultList.get(0).getName());
    }

    // TODO: Oracle requires a cycle clause #295
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQLOld.class, NoOracle.class })
    public void testCtePaginationLimitOnly() {
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
                .orderByAsc("t.id")
                .setMaxResults(2);

        List<TestCTE> resultList = cb.getResultList();
        assertEquals(2, resultList.size());
        assertEquals("root1", resultList.get(0).getName());
        assertEquals("child1_1", resultList.get(1).getName());
    }
}
