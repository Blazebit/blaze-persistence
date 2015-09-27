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

package com.blazebit.persistence;

import static org.junit.Assert.assertEquals;

import javax.persistence.EntityTransaction;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.entity.Document;
import com.blazebit.persistence.entity.Person;
import com.blazebit.persistence.testsuite.base.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.category.NoOpenJPA;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public class InsertTest extends AbstractCoreTest {

	@Before
	public void setUp() {
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			Person o1 = new Person("P1");
			em.persist(o1);
			em.flush();

			Person o2 = new Person("P2");
			em.persist(o2);
			em.flush();
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			throw new RuntimeException(e);
		}
	}
	
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testSimple() {
        InsertCriteriaBuilder<Document> cb = cbf.insert(em, Document.class);
        cb.from(Person.class, "p");
        cb.bind("name").select("CONCAT(p.name,'s document')");
        cb.bind("age").select("p.age");
        cb.bind("idx").select("1");
        cb.bind("owner").select("p");
        cb.orderByAsc("p.id");
        String expected = "INSERT INTO Document(age, idx, name, owner)\n"
                + "SELECT p.age, 1, CONCAT(p.name,'s document'), p FROM Person p ORDER BY p.id ASC NULLS LAST";

        assertEquals(expected, cb.getQueryString());

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            int updateCount = cb.executeUpdate();
            assertEquals(2, updateCount);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }
    }
    
    @Test
    // TODO: Implement
    @Ignore("Not yet implemented")
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testSimpleWithLimit() {
        InsertCriteriaBuilder<Document> cb = cbf.insert(em, Document.class);
        cb.from(Person.class, "p");
        cb.bind("name").select("CONCAT(p.name,'s document')");
        cb.bind("age").select("p.age");
        cb.bind("idx").select("1");
        cb.bind("owner").select("p");
        cb.orderByAsc("p.id");
        cb.setMaxResults(1);
        String expected = "INSERT INTO Document(age, idx, name, owner)\n"
                + "SELECT p.age, 1, CONCAT(p.name,'s document'), p FROM Person p ORDER BY p.id ASC NULLS LAST";

        assertEquals(expected, cb.getQueryString());

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            int updateCount = cb.executeUpdate();
            assertEquals(1, updateCount);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new RuntimeException(e);
        }
    }

	// NOTE: hibernate 4.2 does not support using parameters in the select clause
	@Test
	@Category({ NoHibernate42.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
	public void testSimpleWithParameters() {
		InsertCriteriaBuilder<Document> cb = cbf.insert(em, Document.class);
		cb.from(Person.class, "p");
		cb.bind("name").select("CONCAT(p.name,'s document')");
		cb.bind("age", 1L);
		cb.bind("idx", 1);
		cb.bind("owner").select("p");
        cb.orderByAsc("p.id");
		String expected = "INSERT INTO Document(age, idx, name, owner)\n"
				+ "SELECT :age, :idx, CONCAT(p.name,'s document'), p FROM Person p ORDER BY p.id ASC NULLS LAST";

		assertEquals(expected, cb.getQueryString());

		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			int updateCount = cb.executeUpdate();
			assertEquals(2, updateCount);
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			throw new RuntimeException(e);
		}
	}
}
