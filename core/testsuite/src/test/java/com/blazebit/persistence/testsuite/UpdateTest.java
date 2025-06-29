/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.UpdateCriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus4;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoFirebird;
import com.blazebit.persistence.testsuite.base.jpa.category.NoH2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMSSQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQLOld;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.base.jpa.category.NoSQLite;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.DocumentNodeCTE;
import com.blazebit.persistence.testsuite.entity.IdHolderCTE;
import com.blazebit.persistence.testsuite.entity.NameObject;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
                doc1.setResponsiblePerson(o1);
                doc2.setOwner(o1);
                doc2.setResponsiblePerson(o1);
                doc3.setOwner(o1);
                doc3.setResponsiblePerson(o1);

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

    // NOTE: EclipseLink can update neither d.nameObject.intIdEntity nor d.nameObject.intIdEntity.id so associations in embeddables don't work here
    // NOTE: DN4 also doesn't seem to support this
    @Test
    @Category({ NoEclipselink.class, NoDatanucleus4.class })
    public void testSetEmbeddable() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final UpdateCriteriaBuilder<Document> cb = cbf.update(em, Document.class, "d");
                cb.set("nameObject", new NameObject("D1", "D2"));
                cb.where("name").eq("D1");
                String expected = "UPDATE Document d SET d.nameObject.intIdEntity.id = :_param_0_intIdEntity_id, d.nameObject.primaryName = :_param_0_primaryName, d.nameObject.secondaryName = :_param_0_secondaryName WHERE d.name = :param_1";

                assertEquals(expected, cb.getQueryString());

                int updateCount = cb.executeUpdate();
                assertEquals(1, updateCount);
            }
        });
    }

    // NOTE: This requires advanced SQL support
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testImplicitJoin() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final UpdateCriteriaBuilder<Document> cb = cbf.update(em, Document.class, "d");
                cb.set("name", "NewD1");
                cb.where("name").eq("D1");
                cb.where("owner.name").eq("P1");
                String expected = "UPDATE Document d SET d.name = :param_0 FROM Document d JOIN d.owner owner_1 WHERE d.name = :param_1 AND owner_1.name = :param_2";

                assertEquals(expected, cb.getQueryString());

                int updateCount = cb.executeUpdate();
                assertEquals(1, updateCount);
            }
        });
    }

    // NOTE: This requires advanced SQL support
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testMultipleDeepImplicitJoin() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final UpdateCriteriaBuilder<Document> cb = cbf.update(em, Document.class, "d");
                cb.set("name", "NewD1");
                cb.where("name").eq("D1");
                cb.where("owner.name").eq("P1");
                cb.where("responsiblePerson.ownedDocuments.name").eq("D1");
                String expected = "UPDATE Document d SET d.name = :param_0 " +
                        "FROM Document d JOIN d.owner owner_1 " +
                        "LEFT JOIN d.responsiblePerson responsiblePerson_1 " +
                        "LEFT JOIN responsiblePerson_1.ownedDocuments ownedDocuments_1 " +
                        "WHERE d.name = :param_1 " +
                        "AND owner_1.name = :param_2 " +
                        "AND ownedDocuments_1.name = :param_3";

                assertEquals(expected, cb.getQueryString());

                int updateCount = cb.executeUpdate();
                assertEquals(1, updateCount);
            }
        });
    }

    // Test for issue #1615
    // NOTE: This requires advanced SQL support
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testValuesJoin() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final UpdateCriteriaBuilder<Document> cb = cbf.update(em, Document.class, "d");
                cb.fromValues(IdHolderCTE.class, "i", Arrays.asList(new IdHolderCTE(doc1.getId()), new IdHolderCTE(doc2.getId())));
                cb.set("name", "NewD1");
                cb.where("d.id").eqExpression("i.id");
                String expected = "UPDATE Document d SET d.name = :param_0 " +
                    "FROM Document d, IdHolderCTE(2 VALUES) i " +
                    "WHERE d.id = i.id";

                assertEquals(expected, cb.getQueryString());

                int updateCount = cb.executeUpdate();
                assertEquals(2, updateCount);
            }
        });
    }

    // Test for issue #2047
    // NOTE: This requires advanced SQL support
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testValuesJoinInSet() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final UpdateCriteriaBuilder<Document> cb = cbf.update(em, Document.class, "d");
                cb.fromValues(IdHolderCTE.class, "i", Arrays.asList(new IdHolderCTE(doc1.getId()), new IdHolderCTE(doc2.getId())));
                cb.setExpression("name", "CONCAT(d.name,CAST_STRING(i.id))");
                cb.where("d.id").eqExpression("i.id");
                String expected = "UPDATE Document d SET d.name = CONCAT(d.name,cast_string(i.id)) " +
                        "FROM Document d, IdHolderCTE(2 VALUES) i " +
                        "WHERE d.id = i.id";

                assertEquals(expected, cb.getQueryString());

                int updateCount = cb.executeUpdate();
                assertEquals(2, updateCount);
                assertEquals("D1" + doc1.getId(), em.find(Document.class, doc1.getId()).getName());
                assertEquals("D2" + doc2.getId(), em.find(Document.class, doc2.getId()).getName());
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

    // NOTE: MySQL does not like referencing the table that is being updated in a subquery in the set clause
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

    // NOTE: H2 and MySQL only support returning generated keys
    // NOTE: MySQL does not support CTEs
    @Test
    @Category({ NoH2.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testReturningLastWithCte() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final UpdateCriteriaBuilder<Document> cb = cbf.update(em, Document.class, "d");
                cb.set("name", "NewD1");
                cb.with(IdHolderCTE.class, false)
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

    // NOTE: H2 and MySQL only support returning generated keys
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
                    .set("age", 0L)
                    .executeWithReturning("id", Long.class)
                    .getResultList();
            }
        };

        transactional(work);
        transactional(work);
    }

}
