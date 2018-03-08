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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.DeleteCriteriaBuilder;
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoFirebird;
import com.blazebit.persistence.testsuite.base.jpa.category.NoH2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMSSQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.base.jpa.category.NoSQLite;
import com.blazebit.persistence.testsuite.entity.IdHolderCTE;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.PolymorphicBase;
import com.blazebit.persistence.testsuite.entity.PolymorphicPropertyBase;
import com.blazebit.persistence.testsuite.entity.PolymorphicPropertySub1;
import com.blazebit.persistence.testsuite.entity.PolymorphicPropertySub2;
import com.blazebit.persistence.testsuite.entity.PolymorphicSub1;
import com.blazebit.persistence.testsuite.entity.PolymorphicSub2;
import com.blazebit.persistence.testsuite.entity.StringIdCTE;
import com.blazebit.persistence.testsuite.entity.TPCBase;
import com.blazebit.persistence.testsuite.entity.TPCSub2;
import com.blazebit.persistence.testsuite.entity.TPCSub1;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Ignore("Has to be implemented as part of #345")
public class DeletePolymorphicTest extends AbstractCoreTest {

    TPCSub1 tpc1;
    TPCSub2 tpc2;
    PolymorphicSub1 joined1;
    PolymorphicSub2 joined2;
    PolymorphicPropertySub1 st1;
    PolymorphicPropertySub2 st2;

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
            IdHolderCTE.class,
            StringIdCTE.class,
            IntIdEntity.class,
            TPCBase.class,
            TPCSub1.class,
            TPCSub2.class,
            PolymorphicBase.class,
            PolymorphicSub1.class,
            PolymorphicSub2.class,
            PolymorphicPropertyBase.class,
            PolymorphicPropertySub1.class,
            PolymorphicPropertySub2.class
        };
    }

    @Before
    public void setUp() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                // Table per class data
                tpc1 = new TPCSub1(1L, "TPC1");
                tpc2 = new TPCSub2(2L, "TPC2");

                em.persist(tpc1);
                em.persist(tpc2);

                // Joined data
                joined1 = new PolymorphicSub1();
                joined2 = new PolymorphicSub2();

                joined1.setName("JOINED1");
                joined2.setName("JOINED2");

                em.persist(joined1);
                em.persist(joined2);

                // Single table data
                st1 = new PolymorphicPropertySub1();
                st2 = new PolymorphicPropertySub2();

                st1.setName("ST1");
                st2.setName("ST2");

                em.persist(st1);
                em.persist(st2);
            }
        });
    }

    @Test
    public void testTablePerClass() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final DeleteCriteriaBuilder<TPCBase> cb = cbf.delete(em, TPCBase.class, "t");
                cb.where("base").isNotNull();
                String expected = "DELETE FROM TablePerClassBase t WHERE t.base IS NOT NULL";

                assertEquals(expected, cb.getQueryString());

                int updateCount = cb.executeUpdate();
                assertEquals(2, updateCount);
                assertTrue(cbf.create(em, TPCBase.class).getResultList().isEmpty());
            }
        });
    }

    @Test
    public void testJoined() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final DeleteCriteriaBuilder<PolymorphicBase> cb = cbf.delete(em, PolymorphicBase.class, "t");
                cb.where("name").isNotNull();
                String expected = "DELETE FROM PolymorphicBase t WHERE t.name IS NOT NULL";

                assertEquals(expected, cb.getQueryString());

                int updateCount = cb.executeUpdate();
                assertEquals(2, updateCount);
                assertTrue(cbf.create(em, PolymorphicBase.class).getResultList().isEmpty());
            }
        });
    }

    @Test
    public void testSingleTable() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final DeleteCriteriaBuilder<PolymorphicPropertyBase> cb = cbf.delete(em, PolymorphicPropertyBase.class, "t");
                cb.where("name").isNotNull();
                String expected = "DELETE FROM PolymorphicPropertyBase t WHERE t.name IS NOT NULL";

                assertEquals(expected, cb.getQueryString());

                int updateCount = cb.executeUpdate();
                assertEquals(2, updateCount);
                assertTrue(cbf.create(em, PolymorphicPropertyBase.class).getResultList().isEmpty());
            }
        });
    }

    @Test
    // NOTE: MySQL does not support CTEs
    // NOTE: H2 only supports with clause in select statement
    @Category({ NoH2.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testTablePerClassWithCte() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final DeleteCriteriaBuilder<TPCBase> cb = cbf.delete(em, TPCBase.class, "t");
                cb.with(IdHolderCTE.class)
                    .from(TPCBase.class, "t")
                    .bind("id").select("t.id")
                .end();
                cb.where("id").in()
                    .from(IdHolderCTE.class)
                    .select("id")
                .end();
                String expected = "WITH IdHolderCTE(id) AS(\n" +
                        "SELECT t.id FROM TablePerClassBase t\n" +
                        ")\n" +
                        "DELETE FROM TablePerClassBase t WHERE t.id IN (" +
                        "SELECT idHolderCTE.id FROM IdHolderCTE idHolderCTE" +
                        ")";

                assertEquals(expected, cb.getQueryString());

                int updateCount = cb.executeUpdate();
                assertEquals(2, updateCount);
                assertTrue(cbf.create(em, TPCBase.class).getResultList().isEmpty());
            }
        });
    }

    @Test
    // NOTE: MySQL does not support CTEs
    // NOTE: H2 only supports with clause in select statement
    @Category({ NoH2.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testJoinedWithCte() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final DeleteCriteriaBuilder<PolymorphicBase> cb = cbf.delete(em, PolymorphicBase.class, "t");
                cb.with(IdHolderCTE.class)
                    .from(PolymorphicBase.class, "t")
                    .bind("id").select("t.id")
                .end();
                cb.where("id").in()
                    .from(IdHolderCTE.class)
                    .select("id")
                .end();
                String expected = "WITH IdHolderCTE(id) AS(\n" +
                        "SELECT t.id FROM PolymorphicBase t\n" +
                        ")\n" +
                        "DELETE FROM PolymorphicBase t WHERE t.id IN (" +
                        "SELECT idHolderCTE.id FROM IdHolderCTE idHolderCTE" +
                        ")";

                assertEquals(expected, cb.getQueryString());

                int updateCount = cb.executeUpdate();
                assertEquals(2, updateCount);
                assertTrue(cbf.create(em, PolymorphicBase.class).getResultList().isEmpty());
            }
        });
    }

    @Test
    // NOTE: MySQL does not support CTEs
    // NOTE: H2 only supports with clause in select statement
    @Category({ NoH2.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testSingleTableWithCte() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final DeleteCriteriaBuilder<PolymorphicPropertyBase> cb = cbf.delete(em, PolymorphicPropertyBase.class, "t");
                cb.with(IdHolderCTE.class)
                    .from(PolymorphicPropertyBase.class, "t")
                    .bind("id").select("t.id")
                .end();
                cb.where("id").in()
                    .from(IdHolderCTE.class)
                    .select("id")
                .end();
                String expected = "WITH IdHolderCTE(id) AS(\n" +
                        "SELECT t.id FROM PolymorphicPropertyBase t\n" +
                        ")\n" +
                        "DELETE FROM PolymorphicPropertyBase t WHERE t.id IN (" +
                        "SELECT idHolderCTE.id FROM IdHolderCTE idHolderCTE" +
                        ")";

                assertEquals(expected, cb.getQueryString());

                int updateCount = cb.executeUpdate();
                assertEquals(2, updateCount);
                assertTrue(cbf.create(em, PolymorphicPropertyBase.class).getResultList().isEmpty());
            }
        });
    }

    @Test
    // NOTE: H2 and MySQL only support returning generated keys
    @Category({ NoH2.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testTablePerClassReturning() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final DeleteCriteriaBuilder<TPCBase> cb = cbf.delete(em, TPCBase.class, "t");
                cb.where("base").isNotNull();
                String expected = "DELETE FROM TablePerClassBase t WHERE t.base IS NOT NULL";

                assertEquals(expected, cb.getQueryString());

                ReturningResult<String> returningResult = cb.executeWithReturning("base", String.class);
                assertEquals(2, returningResult.getUpdateCount());
                assertTrue(returningResult.getResultList().contains("TPC1"));
                assertTrue(returningResult.getResultList().contains("TPC2"));
                assertTrue(cbf.create(em, TPCBase.class).getResultList().isEmpty());
            }
        });
    }

    @Test
    // NOTE: H2 and MySQL only support returning generated keys
    @Category({ NoH2.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testJoinedReturning() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final DeleteCriteriaBuilder<PolymorphicBase> cb = cbf.delete(em, PolymorphicBase.class, "t");
                cb.where("name").isNotNull();
                String expected = "DELETE FROM PolymorphicBase t WHERE t.name IS NOT NULL";

                assertEquals(expected, cb.getQueryString());

                ReturningResult<String> returningResult = cb.executeWithReturning("name", String.class);
                assertEquals(2, returningResult.getUpdateCount());
                assertTrue(returningResult.getResultList().contains("JOINED1"));
                assertTrue(returningResult.getResultList().contains("JOINED2"));
                assertTrue(cbf.create(em, PolymorphicBase.class).getResultList().isEmpty());
            }
        });
    }

    @Test
    // NOTE: H2 and MySQL only support returning generated keys
    @Category({ NoH2.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testSingleTableReturning() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final DeleteCriteriaBuilder<PolymorphicPropertyBase> cb = cbf.delete(em, PolymorphicPropertyBase.class, "t");
                cb.where("name").isNotNull();
                String expected = "DELETE FROM PolymorphicPropertyBase t WHERE t.name IS NOT NULL";

                assertEquals(expected, cb.getQueryString());

                ReturningResult<String> returningResult = cb.executeWithReturning("name", String.class);
                assertEquals(2, returningResult.getUpdateCount());
                assertTrue(returningResult.getResultList().contains("ST1"));
                assertTrue(returningResult.getResultList().contains("ST2"));
                assertTrue(cbf.create(em, PolymorphicPropertyBase.class).getResultList().isEmpty());
            }
        });
    }

    // NOTE: Currently only PostgreSQL and DB2 support returning from within a CTE
    @Test
    @Category({ NoH2.class, NoOracle.class, NoMSSQL.class, NoSQLite.class, NoFirebird.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testTablePerClassReturningInCTE() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final CriteriaBuilder<String> cb = cbf.create(em, String.class)
                        .withReturning(StringIdCTE.class)
                            .delete(TPCBase.class, "t")
                            .where("base").isNotNull()
                            .returning("id", "base")
                        .end()
                        .from(StringIdCTE.class, "t")
                        .select("t.id");

                String expected = "WITH StringIdCTE(id) AS(\n" +
                        "DELETE FROM TablePerClassBase t WHERE t.base IS NOT NULL RETURNING base\n" +
                        ")\n" +
                        "SELECT t.id FROM StringIdCTE t";

                assertEquals(expected, cb.getQueryString());

                List<String> result = cb.getResultList();
                assertEquals(2, result.size());
                assertTrue(result.contains("TPC1"));
                assertTrue(result.contains("TPC2"));
                assertTrue(cbf.create(em, TPCBase.class).getResultList().isEmpty());
            }
        });
    }

    // NOTE: Currently only PostgreSQL and DB2 support returning from within a CTE
    @Test
    @Category({ NoH2.class, NoOracle.class, NoMSSQL.class, NoSQLite.class, NoFirebird.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testJoinedReturningInCTE() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final CriteriaBuilder<String> cb = cbf.create(em, String.class)
                        .withReturning(StringIdCTE.class)
                            .delete(PolymorphicBase.class, "t")
                            .where("name").isNotNull()
                            .returning("id", "name")
                        .end()
                        .from(StringIdCTE.class, "t")
                        .select("t.id");

                String expected = "WITH StringIdCTE(id) AS(\n" +
                        "DELETE FROM PolymorphicBase t WHERE t.name IS NOT NULL RETURNING name\n" +
                        ")\n" +
                        "SELECT t.id FROM StringIdCTE t";

                assertEquals(expected, cb.getQueryString());

                List<String> result = cb.getResultList();
                assertEquals(2, result.size());
                assertTrue(result.contains("JOINED1"));
                assertTrue(result.contains("JOINED2"));
                assertTrue(cbf.create(em, PolymorphicBase.class).getResultList().isEmpty());
            }
        });
    }

    // NOTE: Currently only PostgreSQL and DB2 support returning from within a CTE
    @Test
    @Category({ NoH2.class, NoOracle.class, NoMSSQL.class, NoSQLite.class, NoFirebird.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testSingleTableReturningInCTE() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final CriteriaBuilder<String> cb = cbf.create(em, String.class)
                        .withReturning(StringIdCTE.class)
                            .delete(PolymorphicPropertyBase.class, "t")
                            .where("name").isNotNull()
                            .returning("id", "name")
                        .end()
                        .from(StringIdCTE.class, "t")
                        .select("t.id");

                String expected = "WITH StringIdCTE(id) AS(\n" +
                        "DELETE FROM PolymorphicPropertyBase t WHERE t.name IS NOT NULL RETURNING name\n" +
                        ")\n" +
                        "SELECT t.id FROM StringIdCTE t";

                assertEquals(expected, cb.getQueryString());

                List<String> result = cb.getResultList();
                assertEquals(2, result.size());
                assertTrue(result.contains("ST1"));
                assertTrue(result.contains("ST2"));
                assertTrue(cbf.create(em, PolymorphicPropertyBase.class).getResultList().isEmpty());
            }
        });
    }

    /*********************************************
     * select OLD and NEW tests
     *********************************************/

    // NOTE: Currently only PostgreSQL and DB2 support returning from within a CTE
    @Test
    @Category({ NoH2.class, NoOracle.class, NoMSSQL.class, NoSQLite.class, NoFirebird.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testTablePerClassReturningInCTESelectOld() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final CriteriaBuilder<String> cb = cbf.create(em, String.class)
                        .withReturning(IdHolderCTE.class)
                            .delete(TPCBase.class, "t")
                            .where("base").isNotNull()
                            .returning("id", "id")
                        .end()
                        .from(IdHolderCTE.class, "cte")
                        .fromOld(TPCBase.class, "t")
                        .where("t.id").eqExpression("cte.id")
                        .select("t.base");

                String expected = "WITH IdHolderCTE(id) AS(\n" +
                        "DELETE FROM TablePerClassBase t WHERE t.base IS NOT NULL RETURNING id\n" +
                        ")\n" +
                        "SELECT t.base FROM IdHolderCTE cte, OLD(TablePerClassBase) t WHERE t.id = cte.id";

                assertEquals(expected, cb.getQueryString());

                List<String> result = cb.getResultList();
                assertEquals(2, result.size());
                assertTrue(result.contains("TPC1"));
                assertTrue(result.contains("TPC2"));
                assertTrue(cbf.create(em, TPCBase.class).getResultList().isEmpty());
            }
        });
    }

    // NOTE: Currently only PostgreSQL and DB2 support returning from within a CTE
    @Test
    @Category({ NoH2.class, NoOracle.class, NoMSSQL.class, NoSQLite.class, NoFirebird.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testJoinedReturningInCTESelectOld() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final CriteriaBuilder<String> cb = cbf.create(em, String.class)
                        .withReturning(IdHolderCTE.class)
                            .delete(PolymorphicBase.class, "t")
                            .where("name").isNotNull()
                            .returning("id", "id")
                        .end()
                        .from(IdHolderCTE.class, "cte")
                        .fromOld(PolymorphicBase.class, "t")
                        .where("t.id").eqExpression("cte.id")
                        .select("t.name");

                String expected = "WITH IdHolderCTE(id) AS(\n" +
                        "DELETE FROM PolymorphicBase t WHERE t.name IS NOT NULL RETURNING id\n" +
                        ")\n" +
                        "SELECT t.name FROM IdHolderCTE cte, OLD(PolymorphicBase) t WHERE t.id = cte.id";

                assertEquals(expected, cb.getQueryString());

                List<String> result = cb.getResultList();
                assertEquals(2, result.size());
                assertTrue(result.contains("JOINED1"));
                assertTrue(result.contains("JOINED2"));
                assertTrue(cbf.create(em, PolymorphicBase.class).getResultList().isEmpty());
            }
        });
    }

    // NOTE: Currently only PostgreSQL and DB2 support returning from within a CTE
    @Test
    @Category({ NoH2.class, NoOracle.class, NoMSSQL.class, NoSQLite.class, NoFirebird.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testSingleTableReturningInCTESelectOld() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final CriteriaBuilder<String> cb = cbf.create(em, String.class)
                        .withReturning(IdHolderCTE.class)
                            .delete(PolymorphicPropertyBase.class, "t")
                            .where("name").isNotNull()
                            .returning("id", "id")
                        .end()
                        .from(IdHolderCTE.class, "cte")
                        .fromOld(PolymorphicPropertyBase.class, "t")
                        .where("t.id").eqExpression("cte.id")
                        .select("t.name");

                String expected = "WITH IdHolderCTE(id) AS(\n" +
                        "DELETE FROM PolymorphicPropertyBase t WHERE t.name IS NOT NULL RETURNING id\n" +
                        ")\n" +
                        "SELECT t.name FROM IdHolderCTE cte, OLD(PolymorphicPropertyBase) t WHERE t.id = cte.id";


                assertEquals(expected, cb.getQueryString());

                List<String> result = cb.getResultList();
                assertEquals(2, result.size());
                assertTrue(result.contains("ST1"));
                assertTrue(result.contains("ST2"));
                assertTrue(cbf.create(em, PolymorphicPropertyBase.class).getResultList().isEmpty());
            }
        });
    }

    // NOTE: Currently only PostgreSQL and DB2 support returning from within a CTE
    @Test
    @Category({ NoH2.class, NoOracle.class, NoMSSQL.class, NoSQLite.class, NoFirebird.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testTablePerClassReturningInCTESelectNew() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class)
                        .withReturning(IdHolderCTE.class)
                            .delete(TPCBase.class, "t")
                            .where("base").isNotNull()
                            .returning("id", "id")
                        .end()
                        .fromNew(TPCBase.class, "t")
                        .rightJoinOn(IdHolderCTE.class, "cte")
                            .on("t.id").eqExpression("cte.id")
                        .end()
                        .select("cte.id")
                        .select("t.base")
                        .orderByAsc("cte.id");

                String expected = "WITH IdHolderCTE(id) AS(\n" +
                        "DELETE FROM TablePerClassBase t WHERE t.base IS NOT NULL RETURNING id\n" +
                        ")\n" +
                        "SELECT cte.id, t.base FROM NEW(TablePerClassBase) t RIGHT JOIN IdHolderCTE cte ON t.id = cte.id ORDER BY cte.id ASC NULLS LAST";

                assertEquals(expected, cb.getQueryString());

                List<Tuple> result = cb.getResultList();
                assertEquals(2, result.size());
                assertEquals(tpc1.getId(), result.get(0).get(0));
                assertNull(result.get(0).get(1));
                assertEquals(tpc2.getId(), result.get(1).get(0));
                assertNull(result.get(1).get(1));
                assertTrue(cbf.create(em, TPCBase.class).getResultList().isEmpty());
            }
        });
    }

    // NOTE: Currently only PostgreSQL and DB2 support returning from within a CTE
    @Test
    @Category({ NoH2.class, NoOracle.class, NoMSSQL.class, NoSQLite.class, NoFirebird.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testJoinedReturningInCTESelectNew() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class)
                        .withReturning(IdHolderCTE.class)
                            .delete(PolymorphicBase.class, "t")
                            .where("name").isNotNull()
                            .returning("id", "id")
                        .end()
                        .fromNew(PolymorphicBase.class, "t")
                        .rightJoinOn(IdHolderCTE.class, "cte")
                            .on("t.id").eqExpression("cte.id")
                        .end()
                        .select("cte.id")
                        .select("t.name")
                        .orderByAsc("cte.id");

                String expected = "WITH IdHolderCTE(id) AS(\n" +
                        "DELETE FROM PolymorphicBase t WHERE t.name IS NOT NULL RETURNING id\n" +
                        ")\n" +
                        "SELECT cte.id, t.name FROM NEW(PolymorphicBase) t RIGHT JOIN IdHolderCTE cte ON t.id = cte.id ORDER BY cte.id ASC NULLS LAST";

                assertEquals(expected, cb.getQueryString());

                List<Tuple> result = cb.getResultList();
                assertEquals(2, result.size());
                assertEquals(joined1.getId(), result.get(0).get(0));
                assertNull(result.get(0).get(1));
                assertEquals(joined2.getId(), result.get(1).get(0));
                assertNull(result.get(1).get(1));
                assertTrue(cbf.create(em, PolymorphicBase.class).getResultList().isEmpty());
            }
        });
    }

    // NOTE: Currently only PostgreSQL and DB2 support returning from within a CTE
    @Test
    @Category({ NoH2.class, NoOracle.class, NoMSSQL.class, NoSQLite.class, NoFirebird.class, NoMySQL.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testSingleTableReturningInCTESelectNew() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class)
                        .withReturning(IdHolderCTE.class)
                            .delete(PolymorphicPropertyBase.class, "t")
                            .where("name").isNotNull()
                            .returning("id", "id")
                        .end()
                        .fromNew(PolymorphicPropertyBase.class, "t")
                        .rightJoinOn(IdHolderCTE.class, "cte")
                            .on("t.id").eqExpression("cte.id")
                        .end()
                        .select("cte.id")
                        .select("t.name")
                        .orderByAsc("cte.id");

                String expected = "WITH IdHolderCTE(id) AS(\n" +
                        "DELETE FROM PolymorphicPropertyBase t WHERE t.name IS NOT NULL RETURNING id\n" +
                        ")\n" +
                        "SELECT cte.id, t.name FROM NEW(PolymorphicPropertyBase) t RIGHT JOIN IdHolderCTE cte ON t.id = cte.id ORDER BY cte.id ASC NULLS LAST";

                assertEquals(expected, cb.getQueryString());

                List<Tuple> result = cb.getResultList();
                assertEquals(2, result.size());
                assertEquals(st1.getId(), result.get(0).get(0));
                assertNull(result.get(0).get(1));
                assertEquals(st2.getId(), result.get(1).get(0));
                assertNull(result.get(1).get(1));
                assertTrue(cbf.create(em, PolymorphicPropertyBase.class).getResultList().isEmpty());
            }
        });
    }

}
