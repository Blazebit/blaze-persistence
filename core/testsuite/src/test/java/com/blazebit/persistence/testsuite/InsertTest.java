/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.InsertCriteriaBuilder;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoH2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMSSQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.entity.DeletePersonCTE;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.IdHolderCTE;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.PersonCTE;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.testsuite.tx.TxWork;
import jakarta.persistence.EntityManager;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
// NOTE: Oracle is problematic due to #306
public class InsertTest extends AbstractCoreTest {
    
    private Person p1;
    private Person p2;

    @Override
    protected Class<?>[] getEntityClasses() {
        return concat(super.getEntityClasses(), new Class<?>[] {
            PersonCTE.class,
            DeletePersonCTE.class, 
            IdHolderCTE.class
        });
    }

    @Before
    public void setUp() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                p1 = new Person("P1");
                em.persist(p1);
                em.flush();

                p2 = new Person("P2");
                em.persist(p2);
            }
        });
    }
    
    @Test
    @Category({ NoOracle.class, NoEclipselink.class })
    public void testSimple() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final InsertCriteriaBuilder<Document> cb = cbf.insert(em, Document.class);
                cb.from(Person.class, "p");
                cb.bind("name").select("CONCAT(p.name,'s document')");
                cb.bind("age").select("p.age");
                cb.bind("idx").select("1");
                cb.bind("owner").select("p");
                cb.orderByAsc("p.id");
                String expected = "INSERT INTO Document(age, idx, name, owner)\n"
                        + "SELECT p.age, 1, CONCAT(p.name,'s document'), p FROM Person p ORDER BY p.id ASC";

                assertEquals(expected, cb.getQueryString());

                int updateCount = cb.executeUpdate();
                assertEquals(2, updateCount);
            }
        });
    }
    
    @Test
    @Category({ NoOracle.class, NoEclipselink.class })
    public void testSimpleWithLimit() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final InsertCriteriaBuilder<Document> cb = cbf.insert(em, Document.class);
                cb.from(Person.class, "p");
                cb.bind("name").select("CONCAT(p.name,'s document')");
                cb.bind("age").select("p.age");
                cb.bind("idx").select("1");
                cb.bind("owner").select("p");
                cb.orderByAsc("p.id");
                cb.setMaxResults(1);
                String expected = "INSERT INTO Document(age, idx, name, owner)\n"
                        + "SELECT p.age, 1, CONCAT(p.name,'s document'), p FROM Person p ORDER BY p.id ASC";

                assertEquals(expected, cb.getQueryString());

                int updateCount = cb.executeUpdate();
                assertEquals(1, updateCount);
            }
        });
    }

    @Test
    @Category({ NoOracle.class, NoEclipselink.class })
    public void testSimpleWithLimitAndOffset() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final InsertCriteriaBuilder<Document> cb = cbf.insert(em, Document.class);
                cb.from(Person.class, "p");
                cb.bind("name").select("CONCAT(p.name,'s document')");
                cb.bind("age").select("p.age");
                cb.bind("idx").select("1");
                cb.bind("owner").select("p");
                cb.orderByAsc("p.id");
                cb.setFirstResult(1);
                cb.setMaxResults(1);
                String expected = "INSERT INTO Document(age, idx, name, owner)\n"
                        + "SELECT p.age, 1, CONCAT(p.name,'s document'), p FROM Person p ORDER BY p.id ASC";

                assertEquals(expected, cb.getQueryString());

                int updateCount = cb.executeUpdate();
                assertEquals(1, updateCount);
            }
        });
    }

    @Test
    @Category({ NoOracle.class, NoEclipselink.class })
    public void testSimpleWithParameters() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final InsertCriteriaBuilder<Document> cb = cbf.insert(em, Document.class);
                cb.from(Person.class, "p");
                cb.bind("name").select("CONCAT(p.name,'s document')");
                cb.bind("age", 1L);
                cb.bind("idx", 1);
                cb.bind("owner").select("p");
                cb.orderByAsc("p.id");
                String expected = "INSERT INTO Document(age, idx, name, owner)\n"
                        + "SELECT :param_0, :param_1, CONCAT(p.name,'s document'), p FROM Person p ORDER BY p.id ASC";

                assertEquals(expected, cb.getQueryString());

                int updateCount = cb.executeUpdate();
                assertEquals(2, updateCount);
            }
        });
    }

    /* Returning */
    
    // NOTE: H2 does not support returning all generated keys
    @Test
    @Category({ NoH2.class, NoOracle.class, NoEclipselink.class })
    public void testReturningAll() {
        ReturningResult<Long> result = transactional(new TxWork<ReturningResult<Long>>() {
            @Override
            public ReturningResult<Long> work(EntityManager em) {
                final InsertCriteriaBuilder<Document> cb = cbf.insert(em, Document.class);
                cb.from(Person.class, "p");
                cb.bind("name").select("CONCAT(p.name,'s document')");
                cb.bind("age").select("p.age");
                cb.bind("idx").select("1");
                cb.bind("owner").select("p");
                cb.orderByAsc("p.id");

                String expected = "INSERT INTO Document(age, idx, name, owner)\n"
                        + "SELECT p.age, 1, CONCAT(p.name,'s document'), p FROM Person p ORDER BY p.id ASC";

                assertEquals(expected, cb.getQueryString());

                return cb.executeWithReturning("id", Long.class);
            }
        });

        assertEquals(2, result.getUpdateCount());
        assertEquals(2, result.getResultList().size());
        assertEquals(byOwner(p1).getId(), result.getResultList().get(0));
        assertEquals(byOwner(p2).getId(), result.getResultList().get(1));
    }
    
    @Test
    @Category({ NoOracle.class, NoEclipselink.class })
    public void testReturningLast() {
        ReturningResult<Long> result = transactional(new TxWork<ReturningResult<Long>>() {
            @Override
            public ReturningResult<Long> work(EntityManager em) {
                final InsertCriteriaBuilder<Document> cb = cbf.insert(em, Document.class);
                cb.from(Person.class, "p");
                cb.bind("name").select("CONCAT(p.name,'s document')");
                cb.bind("age").select("p.age");
                cb.bind("idx").select("1");
                cb.bind("owner").select("p");
                cb.orderByAsc("p.id");

                String expected = "INSERT INTO Document(age, idx, name, owner)\n"
                        + "SELECT p.age, 1, CONCAT(p.name,'s document'), p FROM Person p ORDER BY p.id ASC";

                assertEquals(expected, cb.getQueryString());

                return cb.executeWithReturning("id", Long.class);
            }
        });

        assertEquals(2, result.getUpdateCount());
        assertEquals(byOwner(p2).getId(), result.getLastResult());
    }
    
    @Test
    @Category({ NoOracle.class, NoEclipselink.class })
    public void testReturningLastWithParameter() {
        ReturningResult<Long> result = transactional(new TxWork<ReturningResult<Long>>() {
            @Override
            public ReturningResult<Long> work(EntityManager em) {
                final InsertCriteriaBuilder<Document> cb = cbf.insert(em, Document.class);
                cb.from(Person.class, "p");
                cb.bind("name").select("CONCAT(p.name,'s document')");
                cb.bind("age").select("p.age");
                cb.bind("idx").select("1");
                cb.bind("owner").select("p");
                cb.where("name").eq("P2");
                cb.orderByAsc("p.id");

                String expected = "INSERT INTO Document(age, idx, name, owner)\n"
                        + "SELECT p.age, 1, CONCAT(p.name,'s document'), p FROM Person p WHERE p.name = :param_0 ORDER BY p.id ASC";

                assertEquals(expected, cb.getQueryString());

                return cb.executeWithReturning("id", Long.class);
            }
        });

        assertEquals(1, result.getUpdateCount());
        assertEquals(byOwner(p2).getId(), result.getLastResult());
    }
    
    // TODO: This does not work with sequences for H2 because the next value of the sequence is evaluated regardless of the limit
    @Test
    @Category({ NoOracle.class, NoEclipselink.class })
    public void testReturningLastWithLimit() {
        ReturningResult<Long> result = transactional(new TxWork<ReturningResult<Long>>() {
            @Override
            public ReturningResult<Long> work(EntityManager em) {
                final InsertCriteriaBuilder<Document> cb = cbf.insert(em, Document.class);
                cb.from(Person.class, "p");
                cb.bind("name").select("CONCAT(p.name,'s document')");
                cb.bind("age").select("p.age");
                cb.bind("idx").select("1");
                cb.bind("owner").select("p");
                cb.orderByAsc("p.id");
                cb.setMaxResults(1);

                String expected = "INSERT INTO Document(age, idx, name, owner)\n"
                        + "SELECT p.age, 1, CONCAT(p.name,'s document'), p FROM Person p ORDER BY p.id ASC";

                assertEquals(expected, cb.getQueryString());

                return cb.executeWithReturning("id", Long.class);
            }
        });

        assertEquals(1, result.getUpdateCount());
        assertEquals(byOwner(p1).getId(), result.getLastResult());
    }

    // NOTE: H2 and MySQL only support returning generated keys
    @Test
    @Category({ NoH2.class, NoOracle.class, NoEclipselink.class })
    public void testReturningLastWithCte() {
        ReturningResult<Long> result = transactional(new TxWork<ReturningResult<Long>>() {
            @Override
            public ReturningResult<Long> work(EntityManager em) {
                final InsertCriteriaBuilder<Document> cb = cbf.insert(em, Document.class);
                cb.with(PersonCTE.class, false)
                        .from(Person.class, "p")
                        .bind("id").select("p.id")
                        .bind("name").select("CONCAT(p.name,'s document')")
                        .bind("age").select("p.age")
                        .bind("idx").select("1")
                        .bind("owner").select("p")
                        .end();
                cb.from(PersonCTE.class, "p");
                cb.bind("name").select("p.name");
                cb.bind("age").select("p.age");
                cb.bind("idx").select("p.idx");
                cb.bind("owner").select("p.owner");
                cb.where("p.name").eq("P2s document");
                cb.orderByAsc("p.id");

                String expected = "WITH PersonCTE(id, name, age, idx, owner.id) AS(\n"
                        + "SELECT p.id, CONCAT(p.name,'s document'), p.age, 1, p.id FROM Person p\n"
                        + ")\n"
                        + "INSERT INTO Document(age, idx, name, owner)\n"
                        + "SELECT p.age, p.idx, p.name, p.owner FROM PersonCTE p WHERE p.name = :param_0 ORDER BY p.id ASC";

                assertEquals(expected, cb.getQueryString());

                return cb.executeWithReturning("id", Long.class);
            }
        });

        assertEquals(1, result.getUpdateCount());
        assertEquals(byOwner(p2).getId(), result.getLastResult());
    }

    // NOTE: H2 and MySQL only support returning generated keys
    @Test
    @Category({ NoH2.class, NoOracle.class, NoEclipselink.class })
    public void testReturningLastWithCteAndLimit() {
        ReturningResult<Long> result = transactional(new TxWork<ReturningResult<Long>>() {
            @Override
            public ReturningResult<Long> work(EntityManager em) {
                final InsertCriteriaBuilder<Document> cb = cbf.insert(em, Document.class);
                cb.with(PersonCTE.class, false)
                        .from(Person.class, "p")
                        .bind("id").select("p.id")
                        .bind("name").select("CONCAT(p.name,'s document')")
                        .bind("age").select("p.age")
                        .bind("idx").select("1")
                        .bind("owner").select("p")
                        .end();
                cb.from(PersonCTE.class, "p");
                cb.bind("name").select("p.name");
                cb.bind("age").select("p.age");
                cb.bind("idx").select("p.idx");
                cb.bind("owner").select("p.owner");
                cb.orderByAsc("p.id");
                cb.setMaxResults(1);

                String expected = "WITH PersonCTE(id, name, age, idx, owner.id) AS(\n"
                        + "SELECT p.id, CONCAT(p.name,'s document'), p.age, 1, p.id FROM Person p\n"
                        + ")\n"
                        + "INSERT INTO Document(age, idx, name, owner)\n"
                        + "SELECT p.age, p.idx, p.name, p.owner FROM PersonCTE p ORDER BY p.id ASC";

                assertEquals(expected, cb.getQueryString());

                return cb.executeWithReturning("id", Long.class);
            }
        });

        assertEquals(1, result.getUpdateCount());
        assertEquals(byOwner(p1).getId(), result.getLastResult());
    }

    // NOTE: Currently only PostgreSQL and DB2 support returning from within a CTE
    @Test
    @Category({ NoH2.class, NoOracle.class, NoMSSQL.class, NoMySQL.class, NoEclipselink.class })
    public void testDeleteReturningWithCteAndLimitInto() {
        ReturningResult<Long> result = transactional(new TxWork<ReturningResult<Long>>() {
            @Override
            public ReturningResult<Long> work(EntityManager em) {
                final InsertCriteriaBuilder<Document> cb = cbf.insert(em, Document.class);
                cb.withReturning(DeletePersonCTE.class)
                        .delete(Person.class, "p")
                        .where("p.name").eq(p1.getName())
                        .returning("id", "id")
                        .returning("name", "name")
                        .returning("age", "age")
                        .returning("owner", "p")
                        .end();
                cb.from(DeletePersonCTE.class, "p");
                cb.bind("name").select("CONCAT(p.name,'s document')");
                cb.bind("age").select("p.age");
                cb.bind("idx").select("1");
                cb.bind("owner", p2);
                cb.bind("nonJoinable").select("CONCAT('PersonId=',p.owner.id)");
                cb.orderByAsc("p.id");
                // TODO: I think the limit logic is PostgreSQL specific
                cb.setMaxResults(1);

                String expected = "WITH DeletePersonCTE(id, name, age, owner) AS(\n"
                        + "DELETE FROM Person p WHERE p.name = :param_0 RETURNING id, name, age, id\n"
                        + ")\n"
                        + "INSERT INTO Document(age, idx, name, nonJoinable, owner)\n"
                        + "SELECT p.age, 1, CONCAT(p.name,'s document'), CONCAT('PersonId=',p.owner.id), :param_1 FROM DeletePersonCTE p ORDER BY p.id ASC";

                assertEquals(expected, cb.getQueryString());

                return cb.executeWithReturning("id", Long.class);
            }
        });
        assertEquals(1, result.getUpdateCount());
        assertEquals("PersonId=" + p1.getId(), em.find(Document.class, result.getLastResult()).getNonJoinable());
    }

    // NOTE: Currently only PostgreSQL and DB2 support returning from within a CTE
    @Test
    @Category({ NoH2.class, NoOracle.class, NoMSSQL.class, NoMySQL.class, NoEclipselink.class })
    public void testInsertReturningSelectOld() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final CriteriaBuilder<Document> cb = cbf.create(em, Document.class);
                cb.withReturning(IdHolderCTE.class)
                        .insert(Document.class)
                        .from(Person.class, "p")
                        .bind("name").select("CONCAT(p.name,'s document')")
                        .bind("age").select("p.age")
                        .bind("idx").select("1")
                        .bind("owner").select("p")
                        .where("p.name").eq(p1.getName())
                        .returning("id", "id")
                        .end();
                cb.fromOld(Document.class, "d");
                cb.from(IdHolderCTE.class, "idHolder");
                cb.select("d");
                cb.where("d.id").eqExpression("idHolder.id");

                String expected = "WITH IdHolderCTE(id) AS(\n"
                        + "INSERT INTO Document(age, idx, name, owner)\n"
                        + "SELECT p.age, 1, CONCAT(p.name,'s document'), p FROM Person p WHERE p.name = :param_0 RETURNING id\n"
                        + ")\n"
                        + "SELECT d FROM OLD(Document) d, IdHolderCTE idHolder WHERE d.id = idHolder.id";

                assertEquals(expected, cb.getQueryString());

                List<Document> result = cb.getResultList();
                assertEquals(0, result.size());
            }
        });

        // Of course the next statement would see the changes
        assertEquals(p1.getName() + "s document", byOwner(p1).getName());
    }

    // NOTE: Currently only PostgreSQL and DB2 support returning from within a CTE
    @Test
    @Category({ NoH2.class, NoOracle.class, NoMSSQL.class, NoMySQL.class, NoEclipselink.class })
    public void testInsertReturningSelectNew() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final CriteriaBuilder<Document> cb = cbf.create(em, Document.class);
                cb.withReturning(IdHolderCTE.class)
                        .insert(Document.class)
                        .from(Person.class, "p")
                        .bind("name").select("CONCAT(p.name,'s document')")
                        .bind("age").select("p.age")
                        .bind("idx").select("1")
                        .bind("owner").select("p")
                        .where("p.name").eq(p1.getName())
                        .returning("id", "id")
                        .end();
                cb.fromNew(Document.class, "d");
                cb.from(IdHolderCTE.class, "idHolder");
                cb.select("d");
                cb.where("d.id").eqExpression("idHolder.id");

                String expected = "WITH IdHolderCTE(id) AS(\n"
                        + "INSERT INTO Document(age, idx, name, owner)\n"
                        + "SELECT p.age, 1, CONCAT(p.name,'s document'), p FROM Person p WHERE p.name = :param_0 RETURNING id\n"
                        + ")\n"
                        + "SELECT d FROM NEW(Document) d, IdHolderCTE idHolder WHERE d.id = idHolder.id";

                assertEquals(expected, cb.getQueryString());

                em.clear();
                List<Document> result = cb.getResultList();
                assertEquals(1, result.size());
                assertEquals(p1.getName() + "s document", result.get(0).getName());
            }
        });
    }

    // Test for #1737
    @Test
    @Category({ NoOracle.class, NoEclipselink.class })
    public void testAssociationParameter() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final InsertCriteriaBuilder<Document> cb = cbf.insert(em, Document.class);
                cb.from(Person.class, "p");
                cb.bind("name").select("CONCAT(p.name,'s document')");
                cb.bind("age").select("p.age");
                cb.bind("idx").select("1");
                cb.bind("owner").select(":pers");
                cb.orderByAsc("p.id");
                String expected = "INSERT INTO Document(age, idx, name, owner)\n"
                    + "SELECT p.age, 1, CONCAT(p.name,'s document'), :pers FROM Person p ORDER BY p.id ASC";

                assertEquals(expected, cb.getQueryString());

                int updateCount = cb.setParameter("pers", p1).executeUpdate();
                assertEquals(2, updateCount);
            }
        });
    }
    
    private Document byOwner(Person p) {
        return cbf.create(em, Document.class).where("owner").eq(p).getSingleResult();
    }
}
