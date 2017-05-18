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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.UpdateCriteriaBuilder;
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
import com.blazebit.persistence.testsuite.entity.DocumentNodeCTE;
import com.blazebit.persistence.testsuite.entity.IdHolderCTE;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;

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
        return concat(super.getEntityClasses(), new Class<?>[] {
            DocumentNodeCTE.class,
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
                final UpdateCriteriaBuilder<Document> cb = cbf.update(em, Document.class, "d");
                cb.set("name", "NewD1");
                cb.where("name").eq("D1");
                String expected = "UPDATE Document d SET d.name = :param_0 WHERE d.name = :param_1";

                assertEquals(expected, cb.getQueryString());

                int updateCount = cb.executeUpdate();
                assertEquals(1, updateCount);
            }
        });
    }

    @Test
    public void testParameterExpression() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final UpdateCriteriaBuilder<Document> cb = cbf.update(em, Document.class, "d");
                cb.setExpression("name", ":newName");
                cb.where("name").eq("D1");
                cb.setParameter("newName", "NewD1");
                String expected = "UPDATE Document d SET d.name = :newName WHERE d.name = :param_0";

                assertEquals(expected, cb.getQueryString());

                int updateCount = cb.executeUpdate();
                assertEquals(1, updateCount);
            }
        });
    }

    // NOTE: MySQL does not like subqueries in the set
    @Test
    @Category({ NoMySQL.class, NoEclipselink.class })
    // Eclipselink seems to not support subqueries in update
    public void testSubquery() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final UpdateCriteriaBuilder<Document> cb = cbf.update(em, Document.class, "d");
                cb.set("name")
                        .from(Document.class, "subD")
                        .select("CONCAT('New', subD.name)")
                        .where("subD.id").eqExpression("d.id")
                        .end();
                cb.where("name").eq("D1");
                String expected = "UPDATE Document d SET d.name = (SELECT CONCAT('New',subD.name) FROM Document subD WHERE subD.id = d.id) WHERE d.name = :param_0";

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
                final UpdateCriteriaBuilder<Document> cb = cbf.update(em, Document.class, "d");
                cb.set("name", "NewD1");
                cb.where("id").in(doc1.getId(), doc2.getId());
                String expected = "UPDATE Document d SET d.name = :param_0 WHERE d.id IN (:param_1)";

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
    public void testReturningLast() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final UpdateCriteriaBuilder<Document> cb = cbf.update(em, Document.class, "d");
                cb.set("name", "NewD1");
                cb.where("id").in(doc1.getId(), doc2.getId());
                String expected = "UPDATE Document d SET d.name = :param_0 WHERE d.id IN " + listParameter("param_1");

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
                        + "SELECT subDoc.id FROM Document subDoc ORDER BY subDoc.id ASC LIMIT 2\n"
                        + ")\n"
                        + "UPDATE Document d SET d.name = :param_0 WHERE d.id IN (SELECT idHolder.id FROM IdHolderCTE idHolder)";

                assertEquals(expected, cb.getQueryString());

                ReturningResult<Long> result = cb.executeWithReturning("id", Long.class);
                assertEquals(2, result.getUpdateCount());
                List<Long> list = Arrays.asList(doc1.getId(), doc2.getId());
                assertTrue(list.contains(result.getLastResult()));
            }
        });
    }

    // NOTE: Currently only PostgreSQL and DB2 support returning from within a CTE
    @Test
    @Category({ NoH2.class, NoOracle.class, NoMSSQL.class, NoSQLite.class, NoFirebird.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testUpdateReturningSelectOld() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
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
                        + "UPDATE Document updateDoc SET updateDoc.name = :param_0 WHERE updateDoc.id = :param_1 RETURNING id\n"
                        + ")\n"
                        + "SELECT doc FROM OLD(Document) doc, IdHolderCTE idHolder WHERE doc.id = idHolder.id";

                assertEquals(expected, cb.getQueryString());

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
    @Category({ NoH2.class, NoOracle.class, NoMSSQL.class, NoSQLite.class, NoFirebird.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testUpdateReturningSelectNew() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
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
                        + "UPDATE Document updateDoc SET updateDoc.name = :param_0 WHERE updateDoc.id = :param_1 RETURNING id\n"
                        + ")\n"
                        + "SELECT doc FROM NEW(Document) doc, IdHolderCTE idHolder WHERE doc.id = idHolder.id";

                assertEquals(expected, cb.getQueryString());

                em.clear();
                String name = cb.getSingleResult().getName();
                assertEquals("NewD1", name);
                em.clear();
                // Of course the next statement would see the changes
                assertEquals("NewD1", em.find(Document.class, doc1.getId()).getName());
            }
        });
    }

    // NOTE: H2 only supports with clause in select statement
    // NOTE: MySQL does not support CTEs
    @Test
    @Category({ NoH2.class, NoOracle.class, NoSQLite.class, NoFirebird.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testQueryCaching() {
        TxVoidWork work = new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                cbf.update(em, Document.class, "document")
                    .withRecursive(DocumentNodeCTE.class)
                            .from(Document.class, "d")
                            .where("d.id").eq(1l)
                            .bind("id").select("d.id")
                            .bind("parentId").select("d.parent.id")
                    .unionAll()
                            .from(DocumentNodeCTE.class, "dRec")
                            .from(Document.class, "d")
                            .where("d.parent.id").eqExpression("dRec.id")
                            .bind("id").select("d.id")
                            .bind("parentId").select("d.parent.id")
                    .end()
                    .where("document.id").in()
                        .from(Document.class, "d")
                        .select("d.id")
                        .where("d.id").in()
                            .from(DocumentNodeCTE.class, "dNode")
                            .select("dNode.id")
                        .end()
                    .end()
                    .set("age", 0l)
                    .executeWithReturning("id", Long.class)
                    .getResultList();
            }
        };

        transactional(work);
        transactional(work);
    }

}
