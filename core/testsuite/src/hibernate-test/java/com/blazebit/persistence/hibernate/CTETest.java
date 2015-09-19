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

package com.blazebit.persistence.hibernate;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.persistence.EntityTransaction;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.AbstractCoreTest;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.hibernate.entity.RecursiveEntity;
import com.blazebit.persistence.hibernate.entity.TestView;
import com.blazebit.persistence.testsuite.base.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.category.NoOpenJPA;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
@Ignore
public class CTETest extends AbstractCoreTest {
    
    @Override
	protected Class<?>[] getEntityClasses() {
		return new Class<?>[] {
			RecursiveEntity.class,
			TestView.class
		};
	}

	@Before
    public void setUp(){
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
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testCTE(){
        CriteriaBuilder<TestView> cb = cbf.create(em, TestView.class, "t").where("t.level").lt(2);
        cb.with(TestView.class)
        	.from(RecursiveEntity.class, "e")
        	.bind("id").select("e.id")
        	.bind("name").select("e.name")
        	.bind("level").select("0")
        	.where("e.parent").isNull()
        .end();
        String expected = ""
        		+ "WITH TestView(id, level, name) AS ("
        		+ "  SELECT e.id, 0, e.name FROM RecursiveEntity e WHERE e.parent IS NULL"
        		+ ") "
        		+ "SELECT t FROM TestView t WHERE t.level < 2";
        
        assertEquals(expected, cb.getQueryString());
        List<TestView> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals("root1", resultList.get(0).getName());
    }
	
    @Ignore
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testRecursiveCTE(){
        CriteriaBuilder<TestView> cb = cbf.create(em, TestView.class, "t").where("t.level").lt(2);
        cb.withRecursive(TestView.class)
        	.from(RecursiveEntity.class, "e")
        	.bind("id").select("e.id")
        	.bind("name").select("e.name")
        	.bind("level").select("0")
        	.where("e.parent").isNull()
        .unionAll()
        	.from(TestView.class, "t")
        	.from(RecursiveEntity.class, "e")
        	.bind("id").select("e.id")
        	.bind("name").select("e.name")
        	.bind("level").select("t.level + 1")
        	.where("t.id").eqExpression("e.parent.id")
    	.end();
        String expected = ""
        		+ "WITH TestView(id, level, name) AS ("
        		+ "  SELECT e.id, 0, e.name FROM RecursiveEntity e WHERE e.parent IS NULL"
        		+ "  "
        		+ "  UNION ALL"
        		+ "  "
        		+ "  SELECT e.id, t.level + 1, e.name FROM TestView t, RecursiveEntity e WHERE t.id = e.parent.id"
        		+ ") "
        		+ "SELECT t FROM TestView t WHERE t.level < 2";
        
        assertEquals(expected, cb.getQueryString());
        List<TestView> resultList = cb.getResultList();
        assertEquals(3, resultList.size());
        assertEquals("root1", resultList.get(0).getName());
    }
}
