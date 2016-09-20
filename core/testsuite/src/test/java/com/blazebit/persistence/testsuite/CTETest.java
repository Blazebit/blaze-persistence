/*
 * Copyright 2015 Blazebit.
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

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.persistence.EntityTransaction;

import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.base.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.category.NoH2;
import com.blazebit.persistence.testsuite.base.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.category.NoOpenJPA;
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
			TestCTE.class
		};
	}

	@Before
    public void setUp() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
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

            em.flush();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }
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
        	.setMaxResults(1)
        .end();
        String expected = ""
        		+ "WITH " + TestCTE.class.getSimpleName() + "(id, name, level) AS(\n"
        		+ "SELECT e.id, e.name, 0 FROM RecursiveEntity e WHERE e.parent IS NULL LIMIT 1"
        		+ "\n)\n"
        		+ "SELECT t FROM " + TestCTE.class.getSimpleName() + " t";
        
        assertEquals(expected, cb.getQueryString());
        List<TestCTE> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals("root1", resultList.get(0).getName());
    }
	
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class })
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
        	.from(RecursiveEntity.class, "e")
        	.bind("id").select("e.id")
        	.bind("name").select("e.name")
        	.bind("level").select("t.level + 1")
        	.where("t.id").eqExpression("e.parent.id")
    	.end();
        String expected = ""
        		+ "WITH RECURSIVE " + TestCTE.class.getSimpleName() + "(id, name, level) AS(\n"
        		+ "SELECT e.id, e.name, 0 FROM RecursiveEntity e WHERE e.parent IS NULL"
        		+ "\nUNION ALL\n"
        		+ "SELECT e.id, e.name, t.level + 1 FROM " + TestCTE.class.getSimpleName() + " t, RecursiveEntity e WHERE t.id = e.parent.id"
        		+ "\n)\n"
        		+ "SELECT t FROM " + TestCTE.class.getSimpleName() + " t WHERE t.level < 2";
        
        assertEquals(expected, cb.getQueryString());
        List<TestCTE> resultList = cb.getResultList();
        assertEquals(3, resultList.size());
        assertEquals("root1", resultList.get(0).getName());
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class })
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
                .from(RecursiveEntity.class, "e")
                .bind("id").select("e.id")
                .bind("name").select("e.name")
                .bind("level").select("t.level + 1")
                .where("t.id").eqExpression("e.parent.id")
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
                + "SELECT e.id, e.name, t.level + 1 FROM " + TestCTE.class.getSimpleName() + " t, RecursiveEntity e WHERE t.id = e.parent.id"
                + "\n)\n"
                + "SELECT " + countPaginated("t.id", false) + " FROM " + TestCTE.class.getSimpleName() + " t WHERE t.level < 2";

        String expectedObjectQuery = ""
                + "WITH RECURSIVE " + TestCTE.class.getSimpleName() + "(id, name, level) AS(\n"
                + "SELECT e.id, e.name, 0 FROM RecursiveEntity e WHERE e.parent IS NULL"
                + "\nUNION ALL\n"
                + "SELECT e.id, e.name, t.level + 1 FROM " + TestCTE.class.getSimpleName() + " t, RecursiveEntity e WHERE t.id = e.parent.id"
                + "\n)\n"
                + "SELECT t FROM " + TestCTE.class.getSimpleName() + " t WHERE t.level < 2 ORDER BY " + renderNullPrecedence("t.level", "ASC", "LAST") + ", " + renderNullPrecedence("t.id", "ASC", "LAST");

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
}
