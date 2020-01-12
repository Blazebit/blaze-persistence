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
import com.blazebit.persistence.FullSelectCTECriteriaBuilder;
import com.blazebit.persistence.LeafOngoingSetOperationCTECriteriaBuilder;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.impl.BuilderChainingException;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoH2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQLOld;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.entity.RecursiveEntity;
import com.blazebit.persistence.testsuite.entity.TestAdvancedCTE1;
import com.blazebit.persistence.testsuite.entity.TestAdvancedCTE2;
import com.blazebit.persistence.testsuite.entity.TestCTE;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import java.util.Collections;
import java.util.List;

import static com.googlecode.catchexception.CatchException.caughtException;
import static com.googlecode.catchexception.CatchException.verifyException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public class InlineCTETest extends AbstractCoreTest {
    
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
                root1.getChildren2().add(child1_1);
                root1.getChildren2().add(child1_2);
            }
        });
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

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
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
        String subquery = "SELECT t.name, t.parent.id, t.id FROM RecursiveEntity t LEFT JOIN t.parent parent_1 WHERE parent_1.name = :param_0 ORDER BY t.name ASC LIMIT 1";
        String expected = ""
                + "SELECT t FROM RecursiveEntity(" + subquery + ") t(name, parent.id, id)";

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
        String subquery = "SELECT t.name, t.parent.id, t.id FROM RecursiveEntity t LEFT JOIN t.parent parent_1 WHERE parent_1.name = :param_0 ORDER BY t.name ASC LIMIT 1";
        String expected = ""
                + "SELECT t FROM String(1 VALUES LIKE RecursiveEntity.name) val LEFT JOIN RecursiveEntity(" + subquery + ") t(name, parent.id, id)" + onClause("val = t.name");

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
        String subquery = "SELECT r.name, r.parent.id, r.id FROM RecursiveEntity r LEFT JOIN r.parent parent_1 WHERE parent_1.name = :param_0 AND val = r.name ORDER BY r.name ASC LIMIT 1";
        String expected = ""
                + "SELECT t FROM String(1 VALUES LIKE RecursiveEntity.name) val LEFT JOIN LATERAL RecursiveEntity(" + subquery + ") t(name, parent.id, id)" + onClause("1 = 1");

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
        String expectedSubquery = "SELECT r.name, r.parent.id, r.id FROM RecursiveEntity r WHERE r.parent.id = x.id ORDER BY r.name ASC LIMIT 1";
        String expected = ""
                + "SELECT t FROM RecursiveEntity x LEFT JOIN LATERAL RecursiveEntity(" + expectedSubquery + ") t(name, parent.id, id)" + onClause("1 = 1") + " WHERE x.name = :param_0";

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
        String expectedSubquery = "SELECT r.name, r.parent.id, r.id FROM x.children2 r ORDER BY r.name ASC LIMIT 1";
        String expected = ""
                + "SELECT t FROM RecursiveEntity x LEFT JOIN LATERAL RecursiveEntity(" + expectedSubquery + ") t(name, parent.id, id)" + onClause("1 = 1") + " WHERE x.name = :param_0";

        assertEquals(expected, cb.getQueryString());
        List<RecursiveEntity> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
    }
}
