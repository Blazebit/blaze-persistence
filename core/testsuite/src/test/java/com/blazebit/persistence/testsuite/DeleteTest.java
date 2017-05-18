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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.EntityManager;

import com.blazebit.persistence.ReturningObjectBuilder;
import com.blazebit.persistence.SimpleReturningBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.DeleteCriteriaBuilder;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoFirebird;
import com.blazebit.persistence.testsuite.base.jpa.category.NoH2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.base.jpa.category.NoSQLite;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMSSQL;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.IdHolderCTE;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public class DeleteTest extends AbstractCoreTest {

    Document doc1;
    Document doc2;
    Document doc3;

    @Override
    protected Class<?>[] getEntityClasses() {
        return concat(super.getEntityClasses(), new Class<?>[] {
            IdHolderCTE.class
        });
    }

    @Before
    public void setUp() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
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
            }
        });
    }

    @Test
    public void testSimple() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final DeleteCriteriaBuilder<Document> cb = cbf.delete(em, Document.class, "d").where("d.name").eq("D1");
                String expected = "DELETE FROM Document d WHERE d.name = :param_0";

                assertEquals(expected, cb.getQueryString());

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
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final DeleteCriteriaBuilder<Document> cb = cbf.delete(em, Document.class, "d");
                cb.where("id").in(doc1.getId(), doc2.getId());
                String expected = "DELETE FROM Document d WHERE d.id IN (:param_0)";

                assertEquals(expected, cb.getQueryString());

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
    public void testReturningAllObjectBuilder() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final DeleteCriteriaBuilder<Document> cb = cbf.delete(em, Document.class, "d");
                cb.where("id").in(doc1.getId(), doc2.getId());
                String expected = "DELETE FROM Document d WHERE d.id IN (:param_0)";

                assertEquals(expected, cb.getQueryString());

                ReturningResult<String> result = cb.executeWithReturning(new ReturningObjectBuilder<String>() {
                    @Override
                    public void applyReturning(SimpleReturningBuilder returningBuilder) {
                        returningBuilder.returning("id");
                    }

                    @Override
                    public String build(Object[] tuple) {
                        return Long.toString((Long) tuple[0]);
                    }

                    @Override
                    public List<String> buildList(List<String> list) {
                        return list;
                    }
                });
                assertEquals(2, result.getUpdateCount());
                assertEquals(2, result.getResultList().size());
                Set<String> resultSet = new HashSet<>(result.getResultList());
                assertTrue(resultSet.contains(doc1.getId().toString()));
                assertTrue(resultSet.contains(doc2.getId().toString()));
            }
        });
    }

    // NOTE: H2 and MySQL only support returning generated keys
    @Test
    @Category({ NoH2.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testReturningLast() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final DeleteCriteriaBuilder<Document> cb = cbf.delete(em, Document.class, "d");
                cb.where("id").in(doc1.getId(), doc2.getId());
                String expected = "DELETE FROM Document d WHERE d.id IN (:param_0)";

                assertEquals(expected, cb.getQueryString());

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
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final DeleteCriteriaBuilder<Document> cb = cbf.delete(em, Document.class, "d");
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
                    + "SELECT subDoc.id FROM Document subDoc ORDER BY subDoc.id ASC LIMIT 2\n"
                    + ")\n"
                    + "DELETE FROM Document d WHERE d.id IN (SELECT idHolder.id FROM IdHolderCTE idHolder)";

                assertEquals(expected, cb.getQueryString());

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
    public void testSimpleWithCte() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final DeleteCriteriaBuilder<Document> cb = cbf.delete(em, Document.class, "d");
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
                    + "SELECT subDoc.id FROM Document subDoc ORDER BY subDoc.id ASC LIMIT 2\n"
                    + ")\n"
                    + "DELETE FROM Document d WHERE d.id IN (SELECT idHolder.id FROM IdHolderCTE idHolder)";

                assertEquals(expected, cb.getQueryString());

                assertEquals(2, cb.executeUpdate());
            }
        });
    }

    // NOTE: Currently only PostgreSQL and DB2 support returning from within a CTE
    @Test
    @Category({ NoH2.class, NoOracle.class, NoMSSQL.class, NoSQLite.class, NoFirebird.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testDeleteReturningSelectOld() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final CriteriaBuilder<Document> cb = cbf.create(em, Document.class);
                cb.withReturning(IdHolderCTE.class)
                    .delete(Document.class, "deletedDoc")
                    .where("deletedDoc.id").eq(doc1.getId())
                    .returning("id", "id")
                .end();
                cb.fromOld(Document.class, "doc");
                cb.from(IdHolderCTE.class, "idHolder");
                cb.select("doc");
                cb.where("doc.id").eqExpression("idHolder.id");

                String expected = "WITH IdHolderCTE(id) AS(\n"
                    + "DELETE FROM Document deletedDoc WHERE deletedDoc.id = :param_0 RETURNING id\n"
                    + ")\n"
                    + "SELECT doc FROM OLD(Document) doc, IdHolderCTE idHolder WHERE doc.id = idHolder.id";

                assertEquals(expected, cb.getQueryString());

                String name = cb.getSingleResult().getName();
                assertEquals("D1", name);
                em.clear();
                // Of course the next statement would see the changes
                assertNull(em.find(Document.class, doc1.getId()));
            }
        });
    }
    
    // NOTE: Currently only PostgreSQL and DB2 support returning from within a CTE
    @Test
    @Category({ NoH2.class, NoOracle.class, NoMSSQL.class, NoSQLite.class, NoFirebird.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testDeleteReturningSelectNew() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final CriteriaBuilder<Document> cb = cbf.create(em, Document.class);
                cb.withReturning(IdHolderCTE.class)
                    .delete(Document.class, "deletedDoc")
                    .where("deletedDoc.id").eq(doc1.getId())
                    .returning("id", "id")
                .end();
                cb.fromNew(Document.class, "doc");
                cb.from(IdHolderCTE.class, "idHolder");
                cb.select("doc");
                cb.where("doc.id").eqExpression("idHolder.id");

                String expected = "WITH IdHolderCTE(id) AS(\n"
                    + "DELETE FROM Document deletedDoc WHERE deletedDoc.id = :param_0 RETURNING id\n"
                    + ")\n"
                    + "SELECT doc FROM NEW(Document) doc, IdHolderCTE idHolder WHERE doc.id = idHolder.id";

                assertEquals(expected, cb.getQueryString());

                List<Document> resultList = cb.getResultList();
                assertTrue(resultList.isEmpty());
                em.clear();
                // Of course the next statement would see the changes
                assertNull(em.find(Document.class, doc1.getId()));
            }
        });
    }
}
