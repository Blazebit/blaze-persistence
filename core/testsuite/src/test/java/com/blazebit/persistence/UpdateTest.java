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
import org.junit.Test;

import com.blazebit.persistence.entity.Document;
import com.blazebit.persistence.entity.Person;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public class UpdateTest extends AbstractCoreTest {

	@Before
	public void setUp() {
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			Document doc1 = new Document("D1");
			Document doc2 = new Document("D2");
			Document doc3 = new Document("D3");

			Person o1 = new Person("P1");

			doc1.setOwner(o1);
			doc2.setOwner(o1);
			doc3.setOwner(o1);

			em.persist(o1);

			em.persist(doc1);
			em.persist(doc2);
			em.persist(doc3);

			em.flush();
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testSimple() {
		UpdateCriteriaBuilder<Document> cb = cbf.update(em, Document.class, "d");
		cb.set("name", "NewD1");
		cb.where("name").eq("D1");
		String expected = "UPDATE Document d SET name = :name WHERE d.name = :param_0";

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
}
