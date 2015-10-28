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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import javax.persistence.EntityTransaction;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.entity.Document;
import com.blazebit.persistence.entity.IdHolderCTE;
import com.blazebit.persistence.entity.IntIdEntity;
import com.blazebit.persistence.entity.Person;
import com.blazebit.persistence.entity.PolymorphicBase;
import com.blazebit.persistence.entity.Version;
import com.blazebit.persistence.testsuite.base.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.category.NoFirebird;
import com.blazebit.persistence.testsuite.base.category.NoH2;
import com.blazebit.persistence.testsuite.base.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.base.category.NoOracle;
import com.blazebit.persistence.testsuite.base.category.NoSQLite;
import com.blazebit.persistence.tx.TxVoidWork;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public class UpdateTest extends AbstractCoreTest {

    Document doc1;
    Document doc2;
    Document doc3;

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
            Document.class,
            Version.class,
            IntIdEntity.class,
            Person.class, 
            IdHolderCTE.class,
            PolymorphicBase.class
        };
    }
    
	@Before
	public void setUp() {
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			doc1 = new Document("D1");
			doc2 = new Document("D2");
			doc3 = new Document("D3");

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
		final UpdateCriteriaBuilder<Document> cb = cbf.update(em, Document.class, "d");
		cb.set("name", "NewD1");
		cb.where("name").eq("D1");
		String expected = "UPDATE Document d SET name = :param_0 WHERE d.name = :param_1";

		assertEquals(expected, cb.getQueryString());

        transactional(new TxVoidWork() {
            @Override
            public void work() {
    			int updateCount = cb.executeUpdate();
    			assertEquals(1, updateCount);
            }
        });
	}

    /* Returning */

    // NOTE: H2 and MySQL only support returning generated keys
    @Test
    @Category({ NoH2.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testReturningAll() {
        final UpdateCriteriaBuilder<Document> cb = cbf.update(em, Document.class, "d");
        cb.set("name", "NewD1");
        cb.where("id").in(doc1.getId(), doc2.getId());
        String expected = "UPDATE Document d SET name = :param_0 WHERE d.id IN (:param_1)";

        assertEquals(expected, cb.getQueryString());

        transactional(new TxVoidWork() {
            @Override
            public void work() {
                ReturningResult<Long> result = cb.executeWithReturning("id", Long.class);
                assertEquals(2, result.getUpdateCount());
                assertEquals(2, result.getResultList().size());
                List<Long> orderedList = new ArrayList<Long>(new TreeSet<Long>(result.getResultList()));
                assertEquals(doc1.getId(), orderedList.get(0));
                assertEquals(doc2.getId(), orderedList.get(1));
            }
        });
    }

    // NOTE: H2 and MySQL only support returning generated keys
    @Test
    @Category({ NoH2.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testReturningLast() {
        final UpdateCriteriaBuilder<Document> cb = cbf.update(em, Document.class, "d");
        cb.set("name", "NewD1");
        cb.where("id").in(doc1.getId(), doc2.getId());
        String expected = "UPDATE Document d SET name = :param_0 WHERE d.id IN (:param_1)";

        assertEquals(expected, cb.getQueryString());

        transactional(new TxVoidWork() {
            @Override
            public void work() {
                ReturningResult<Long> result = cb.executeWithReturning("id", Long.class);
                assertEquals(2, result.getUpdateCount());
                List<Long> list = Arrays.asList(doc1.getId(), doc2.getId());
                assertTrue(list.contains(result.getLastResult()));
            }
        });
    }

    // NOTE: H2 only supports with clause in select statement
    // NOTE: MySQL does not support CTEs
    @Test
    @Category({ NoH2.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testReturningLastWithCte() {
        final UpdateCriteriaBuilder<Document> cb = cbf.update(em, Document.class, "d");
        cb.set("name", "NewD1");
        cb.with(IdHolderCTE.class)
            .from(Document.class, "subDoc")
            .bind("id").select("subDoc.id")
            .orderByAsc("subDoc.id")
            .setMaxResults(2)
        .end();
        cb.where("id").in()
            .from(IdHolderCTE.class, "idHolder")
            .select("idHolder.id")
        .end();
        String expected = "WITH IdHolderCTE(id) AS(\n"
            + "SELECT subDoc.id FROM Document subDoc ORDER BY " + renderNullPrecedence("subDoc.id", "ASC", "LAST") + " LIMIT 2\n"
            + ")\n"
            + "UPDATE Document d SET name = :param_0 WHERE d.id IN (SELECT idHolder.id FROM IdHolderCTE idHolder)";

        assertEquals(expected, cb.getQueryString());

        transactional(new TxVoidWork() {
            @Override
            public void work() {
                ReturningResult<Long> result = cb.executeWithReturning("id", Long.class);
                assertEquals(2, result.getUpdateCount());
                List<Long> list = Arrays.asList(doc1.getId(), doc2.getId());
                assertTrue(list.contains(result.getLastResult()));
            }
        });
    }

    // NOTE: Currently only PostgreSQL and DB2 support returning from within a CTE
    @Test
    @Category({ NoH2.class, NoOracle.class, NoSQLite.class, NoFirebird.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testUpdateReturningSelectOld() {
        final CriteriaBuilder<Document> cb = cbf.create(em, Document.class);
        cb.withReturning(IdHolderCTE.class)
            .update(Document.class, "updateDoc")
            .set("name", "NewD1")
            .where("updateDoc.id").eq(doc1.getId())
            .returning("id", "id")
        .end();
        cb.fromOld(Document.class, "doc");
        cb.from(IdHolderCTE.class, "idHolder");
        cb.select("doc");
        cb.where("doc.id").eqExpression("idHolder.id");
        
        String expected = "WITH IdHolderCTE(id) AS(\n"
            + "UPDATE Document updateDoc SET name = :param_0 WHERE updateDoc.id = :param_1 RETURNING id\n"
            + ")\n"
            + "SELECT doc FROM Document doc, IdHolderCTE idHolder WHERE doc.id = idHolder.id";

        assertEquals(expected, cb.getQueryString());

        transactional(new TxVoidWork() {
            @Override
            public void work() {
                String name = cb.getSingleResult().getName();
                assertEquals("D1", name);
                em.clear();
                // Of course the next statement would see the changes
                assertEquals("NewD1", em.find(Document.class, doc1.getId()).getName());
            }
        });
    }

    // NOTE: Currently only PostgreSQL and DB2 support returning from within a CTE
    @Test
    @Category({ NoH2.class, NoOracle.class, NoSQLite.class, NoFirebird.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testUpdateReturningSelectNew() {
        final CriteriaBuilder<Document> cb = cbf.create(em, Document.class);
        cb.withReturning(IdHolderCTE.class)
            .update(Document.class, "updateDoc")
            .set("name", "NewD1")
            .where("updateDoc.id").eq(doc1.getId())
            .returning("id", "id")
        .end();
        cb.fromNew(Document.class, "doc");
        cb.from(IdHolderCTE.class, "idHolder");
        cb.select("doc");
        cb.where("doc.id").eqExpression("idHolder.id");
        
        String expected = "WITH IdHolderCTE(id) AS(\n"
            + "UPDATE Document updateDoc SET name = :param_0 WHERE updateDoc.id = :param_1 RETURNING id\n"
            + ")\n"
            + "SELECT doc FROM Document doc, IdHolderCTE idHolder WHERE doc.id = idHolder.id";

        assertEquals(expected, cb.getQueryString());

        transactional(new TxVoidWork() {
            @Override
            public void work() {
                em.clear();
                String name = cb.getSingleResult().getName();
                assertEquals("NewD1", name);
                em.clear();
                // Of course the next statement would see the changes
                assertEquals("NewD1", em.find(Document.class, doc1.getId()).getName());
            }
        });
    }
    
    @Test
    public void testUpdateQueryWithNamedParameters(){
    	final long ownerId = 0;
    	final int pageSize = 500;
    	cbf.update(em, Document.class, "d").set("archived", true)
			.where("d.id").nonPortable().in("alias", "FUNCTION('LIMIT',alias,:pageSize)").from(Document.class, "d2")
				.select("d2.id")
				.where("d2.owner.id").eq(ownerId)
				.where("d2.age").ge(18l)
				.whereNotExists().from(Person.class, "e")
					.select("e.id")
					.where("e.name").eq("tom")
				.end()
				.orderByAsc("d2.id")
			.end()
			.setParameter("pageSize", pageSize)
			.executeWithReturning("id", Long.class);
    }
}
