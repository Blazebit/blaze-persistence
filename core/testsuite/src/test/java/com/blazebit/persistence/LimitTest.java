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

import java.util.List;

import javax.persistence.EntityTransaction;

import org.junit.Before;
import org.junit.Test;

import com.blazebit.persistence.entity.Person;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public class LimitTest extends AbstractCoreTest {

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
    public void testSubqueryLimit() {
        CriteriaBuilder<Person> cb = cbf.create(em, Person.class, "p");
        cb.where("p.id").in()
        	.from(Person.class, "pSub")
        	.select("pSub.id")
        	.orderByAsc("pSub.id")
        	.setMaxResults(1)
        .end()
        .setMaxResults(1);
        String expected = "SELECT p FROM Person p WHERE p.id IN (" + function("LIMIT", "(SELECT pSub.id FROM Person pSub ORDER BY " + renderNullPrecedence("pSub.id", "ASC", "LAST") +  ")", "1") + ")";

        assertEquals(expected, cb.getQueryString());
        List<Person> result = cb.getResultList();
        assertEquals(1, result.size());
        assertEquals("P1", result.get(0).getName());
    }
	
    @Test
    public void testSubqueryAndOuterQueryLimit() {
        CriteriaBuilder<Person> cb = cbf.create(em, Person.class, "p");
        cb.where("p.id").in()
        	.from(Person.class, "pSub")
        	.select("pSub.id")
        	.orderByAsc("pSub.id")
        	.setMaxResults(2)
        .end()
    	.orderByAsc("p.id")
        .setMaxResults(1);
        String expected = "SELECT p FROM Person p WHERE p.id IN (" + function("LIMIT", "(SELECT pSub.id FROM Person pSub ORDER BY " + renderNullPrecedence("pSub.id", "ASC", "LAST") +  ")", "2") + ") ORDER BY " + renderNullPrecedence("p.id", "ASC", "LAST");

        assertEquals(expected, cb.getQueryString());
        List<Person> result = cb.getResultList();
        assertEquals(1, result.size());
        assertEquals("P1", result.get(0).getName());
    }
}
