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
import com.blazebit.persistence.ReturningResult;
import com.blazebit.persistence.UpdateCriteriaBuilder;
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
import com.blazebit.persistence.testsuite.entity.TPCSub1;
import com.blazebit.persistence.testsuite.entity.TPCSub2;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Ignore("Has to be implemented as part of #345")
public class UpdatePolymorphicTest extends AbstractCoreTest {

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
                TPCSub1 tpc1 = new TPCSub1(1L, "TPC1");
                TPCSub2 tpc2 = new TPCSub2(2L, "TPC2");

                em.persist(tpc1);
                em.persist(tpc2);

                // Joined data
                PolymorphicSub1 joined1 = new PolymorphicSub1();
                PolymorphicSub2 joined2 = new PolymorphicSub2();

                joined1.setName("JOINED1");
                joined2.setName("JOINED2");

                em.persist(joined1);
                em.persist(joined2);

                // Single table data
                PolymorphicPropertySub1 st1 = new PolymorphicPropertySub1();
                PolymorphicPropertySub2 st2 = new PolymorphicPropertySub2();

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
                final UpdateCriteriaBuilder<TPCBase> cb = cbf.update(em, TPCBase.class, "t");
                cb.setExpression("base", "CONCAT(base, ' - 1')");
                String expected = "UPDATE TablePerClassBase t SET t.base = CONCAT(base,' - 1')";

                assertEquals(expected, cb.getQueryString());

                int updateCount = cb.executeUpdate();
                assertEquals(2, updateCount);
            }
        });
    }

    @Test
    public void testJoined() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final UpdateCriteriaBuilder<PolymorphicBase> cb = cbf.update(em, PolymorphicBase.class, "t");
                cb.setExpression("name", "CONCAT(name, ' - 1')");
                String expected = "UPDATE PolymorphicBase t SET t.name = CONCAT(name,' - 1')";

                assertEquals(expected, cb.getQueryString());

                int updateCount = cb.executeUpdate();
                assertEquals(2, updateCount);
            }
        });
    }

    @Test
    public void testSingleTable() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                final UpdateCriteriaBuilder<PolymorphicPropertyBase> cb = cbf.update(em, PolymorphicPropertyBase.class, "t");
                cb.setExpression("name", "CONCAT(name, ' - 1')");
                String expected = "UPDATE PolymorphicPropertyBase t SET t.name = CONCAT(name,' - 1')";

                assertEquals(expected, cb.getQueryString());

                int updateCount = cb.executeUpdate();
                assertEquals(2, updateCount);
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
                final UpdateCriteriaBuilder<TPCBase> cb = cbf.update(em, TPCBase.class, "t");
                cb.with(IdHolderCTE.class)
                    .from(TPCBase.class, "t")
                    .bind("id").select("t.id")
                .end();
                cb.where("id").in()
                    .from(IdHolderCTE.class)
                    .select("id")
                .end();
                cb.setExpression("base", "CONCAT(base, ' - 1')");
                String expected = "WITH IdHolderCTE(id) AS(\n" +
                        "SELECT t.id FROM TablePerClassBase t\n" +
                        ")\n" +
                        "UPDATE TablePerClassBase t SET t.base = CONCAT(base,' - 1') WHERE t.id IN (" +
                        "SELECT idHolderCTE.id FROM IdHolderCTE idHolderCTE" +
                        ")";

                assertEquals(expected, cb.getQueryString());

                int updateCount = cb.executeUpdate();
                assertEquals(2, updateCount);
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
                final UpdateCriteriaBuilder<PolymorphicBase> cb = cbf.update(em, PolymorphicBase.class, "t");
                cb.with(IdHolderCTE.class)
                        .from(PolymorphicBase.class, "t")
                        .bind("id").select("t.id")
                        .end();
                cb.where("id").in()
                        .from(IdHolderCTE.class)
                        .select("id")
                        .end();
                cb.setExpression("name", "CONCAT(name, ' - 1')");
                String expected = "WITH IdHolderCTE(id) AS(\n" +
                        "SELECT t.id FROM PolymorphicBase t\n" +
                        ")\n" +
                        "UPDATE PolymorphicBase t SET t.name = CONCAT(name,' - 1') WHERE t.id IN (" +
                        "SELECT idHolderCTE.id FROM IdHolderCTE idHolderCTE" +
                        ")";

                assertEquals(expected, cb.getQueryString());

                int updateCount = cb.executeUpdate();
                assertEquals(2, updateCount);
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
                final UpdateCriteriaBuilder<PolymorphicPropertyBase> cb = cbf.update(em, PolymorphicPropertyBase.class, "t");
                cb.with(IdHolderCTE.class)
                        .from(PolymorphicPropertyBase.class, "t")
                        .bind("id").select("t.id")
                        .end();
                cb.where("id").in()
                        .from(IdHolderCTE.class)
                        .select("id")
                        .end();
                cb.setExpression("name", "CONCAT(name, ' - 1')");
                String expected = "WITH IdHolderCTE(id) AS(\n" +
                        "SELECT t.id FROM PolymorphicPropertyBase t\n" +
                        ")\n" +
                        "UPDATE PolymorphicPropertyBase t SET t.name = CONCAT(name,' - 1') WHERE t.id IN (" +
                        "SELECT idHolderCTE.id FROM IdHolderCTE idHolderCTE" +
                        ")";

                assertEquals(expected, cb.getQueryString());

                int updateCount = cb.executeUpdate();
                assertEquals(2, updateCount);
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
                final UpdateCriteriaBuilder<TPCBase> cb = cbf.update(em, TPCBase.class, "t");
                cb.setExpression("base", "CONCAT(base, ' - 1')");
                String expected = "UPDATE TablePerClassBase t SET t.base = CONCAT(base,' - 1')";

                assertEquals(expected, cb.getQueryString());

                ReturningResult<String> returningResult = cb.executeWithReturning("base", String.class);
                assertEquals(2, returningResult.getUpdateCount());
                assertTrue(returningResult.getResultList().contains("TPC1 - 1"));
                assertTrue(returningResult.getResultList().contains("TPC2 - 1"));
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
                final UpdateCriteriaBuilder<PolymorphicBase> cb = cbf.update(em, PolymorphicBase.class, "t");
                cb.setExpression("name", "CONCAT(name, ' - 1')");
                String expected = "UPDATE PolymorphicBase t SET t.name = CONCAT(name,' - 1')";

                assertEquals(expected, cb.getQueryString());

                ReturningResult<String> returningResult = cb.executeWithReturning("name", String.class);
                assertEquals(2, returningResult.getUpdateCount());
                assertTrue(returningResult.getResultList().contains("JOINED1 - 1"));
                assertTrue(returningResult.getResultList().contains("JOINED2 - 1"));
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
                final UpdateCriteriaBuilder<PolymorphicPropertyBase> cb = cbf.update(em, PolymorphicPropertyBase.class, "t");
                cb.setExpression("name", "CONCAT(name, ' - 1')");
                String expected = "UPDATE PolymorphicPropertyBase t SET t.name = CONCAT(name,' - 1')";

                assertEquals(expected, cb.getQueryString());

                ReturningResult<String> returningResult = cb.executeWithReturning("name", String.class);
                assertEquals(2, returningResult.getUpdateCount());
                assertTrue(returningResult.getResultList().contains("ST1 - 1"));
                assertTrue(returningResult.getResultList().contains("ST2 - 1"));
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
                            .update(TPCBase.class, "t")
                            .setExpression("base", "CONCAT(base, ' - 1')")
                            .returning("id", "base")
                        .end()
                        .from(StringIdCTE.class, "t")
                        .select("t.id");

                String expected = "WITH StringIdCTE(id) AS(\n" +
                        "UPDATE TablePerClassBase t SET t.base = CONCAT(base,' - 1') RETURNING base\n" +
                        ")\n" +
                        "SELECT t.id FROM StringIdCTE t";

                assertEquals(expected, cb.getQueryString());

                List<String> result = cb.getResultList();
                assertEquals(2, result.size());
                assertTrue(result.contains("TPC1 - 1"));
                assertTrue(result.contains("TPC2 - 1"));
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
                            .update(PolymorphicBase.class, "t")
                            .setExpression("name", "CONCAT(name, ' - 1')")
                            .returning("id", "name")
                        .end()
                        .from(StringIdCTE.class, "t")
                        .select("t.id");

                String expected = "WITH StringIdCTE(id) AS(\n" +
                        "UPDATE PolymorphicBase t SET t.name = CONCAT(name,' - 1') RETURNING name\n" +
                        ")\n" +
                        "SELECT t.id FROM StringIdCTE t";

                assertEquals(expected, cb.getQueryString());

                List<String> result = cb.getResultList();
                assertEquals(2, result.size());
                assertTrue(result.contains("JOINED1 - 1"));
                assertTrue(result.contains("JOINED2 - 1"));
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
                            .update(PolymorphicPropertyBase.class, "t")
                            .setExpression("name", "CONCAT(name, ' - 1')")
                            .returning("id", "name")
                        .end()
                        .from(StringIdCTE.class, "t")
                        .select("t.id");

                String expected = "WITH StringIdCTE(id) AS(\n" +
                        "UPDATE PolymorphicPropertyBase t SET t.name = CONCAT(name,' - 1') RETURNING name\n" +
                        ")\n" +
                        "SELECT t.id FROM StringIdCTE t";

                assertEquals(expected, cb.getQueryString());

                List<String> result = cb.getResultList();
                assertEquals(2, result.size());
                assertTrue(result.contains("ST1 - 1"));
                assertTrue(result.contains("ST2 - 1"));
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
                            .update(TPCBase.class, "t")
                            .setExpression("base", "CONCAT(base, ' - 1')")
                            .returning("id", "id")
                        .end()
                        .from(IdHolderCTE.class, "cte")
                        .fromOld(TPCBase.class, "t")
                        .where("t.id").eqExpression("cte.id")
                        .select("t.base");

                String expected = "WITH IdHolderCTE(id) AS(\n" +
                        "UPDATE TablePerClassBase t SET t.base = CONCAT(base,' - 1') RETURNING id\n" +
                        ")\n" +
                        "SELECT t.base FROM IdHolderCTE cte, OLD(TablePerClassBase) t WHERE t.id = cte.id";

                assertEquals(expected, cb.getQueryString());

                List<String> result = cb.getResultList();
                assertEquals(2, result.size());
                assertTrue(result.contains("TPC1"));
                assertTrue(result.contains("TPC2"));
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
                            .update(PolymorphicBase.class, "t")
                            .setExpression("name", "CONCAT(name, ' - 1')")
                            .returning("id", "id")
                        .end()
                        .from(IdHolderCTE.class, "cte")
                        .fromOld(PolymorphicBase.class, "t")
                        .where("t.id").eqExpression("cte.id")
                        .select("t.name");

                String expected = "WITH IdHolderCTE(id) AS(\n" +
                        "UPDATE PolymorphicBase t SET t.name = CONCAT(name,' - 1') RETURNING id\n" +
                        ")\n" +
                        "SELECT t.name FROM IdHolderCTE cte, OLD(PolymorphicBase) t WHERE t.id = cte.id";

                assertEquals(expected, cb.getQueryString());

                List<String> result = cb.getResultList();
                assertEquals(2, result.size());
                assertTrue(result.contains("JOINED1"));
                assertTrue(result.contains("JOINED2"));
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
                            .update(PolymorphicPropertyBase.class, "t")
                            .setExpression("name", "CONCAT(name, ' - 1')")
                            .returning("id", "id")
                        .end()
                        .from(IdHolderCTE.class, "cte")
                        .fromOld(PolymorphicPropertyBase.class, "t")
                        .where("t.id").eqExpression("cte.id")
                        .select("t.name");

                String expected = "WITH IdHolderCTE(id) AS(\n" +
                        "UPDATE PolymorphicPropertyBase t SET t.name = CONCAT(name,' - 1') RETURNING id\n" +
                        ")\n" +
                        "SELECT t.name FROM IdHolderCTE cte, OLD(PolymorphicPropertyBase) t WHERE t.id = cte.id";


                assertEquals(expected, cb.getQueryString());

                List<String> result = cb.getResultList();
                assertEquals(2, result.size());
                assertTrue(result.contains("ST1"));
                assertTrue(result.contains("ST2"));
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
                final CriteriaBuilder<String> cb = cbf.create(em, String.class)
                        .withReturning(IdHolderCTE.class)
                            .update(TPCBase.class, "t")
                            .setExpression("base", "CONCAT(base, ' - 1')")
                            .returning("id", "id")
                        .end()
                        .from(IdHolderCTE.class, "cte")
                        .fromNew(TPCBase.class, "t")
                        .where("t.id").eqExpression("cte.id")
                        .select("t.base");

                String expected = "WITH IdHolderCTE(id) AS(\n" +
                        "UPDATE TablePerClassBase t SET t.base = CONCAT(base,' - 1') RETURNING id\n" +
                        ")\n" +
                        "SELECT t.base FROM IdHolderCTE cte, NEW(TablePerClassBase) t WHERE t.id = cte.id";

                assertEquals(expected, cb.getQueryString());

                List<String> result = cb.getResultList();
                assertEquals(2, result.size());
                assertTrue(result.contains("TPC1 - 1"));
                assertTrue(result.contains("TPC2 - 1"));
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
                final CriteriaBuilder<String> cb = cbf.create(em, String.class)
                        .withReturning(IdHolderCTE.class)
                            .update(PolymorphicBase.class, "t")
                            .setExpression("name", "CONCAT(name, ' - 1')")
                            .returning("id", "id")
                        .end()
                        .from(IdHolderCTE.class, "cte")
                        .fromNew(PolymorphicBase.class, "t")
                        .where("t.id").eqExpression("cte.id")
                        .select("t.name");

                String expected = "WITH IdHolderCTE(id) AS(\n" +
                        "UPDATE PolymorphicBase t SET t.name = CONCAT(name,' - 1') RETURNING id\n" +
                        ")\n" +
                        "SELECT t.name FROM IdHolderCTE cte, NEW(PolymorphicBase) t WHERE t.id = cte.id";

                assertEquals(expected, cb.getQueryString());

                List<String> result = cb.getResultList();
                assertEquals(2, result.size());
                assertTrue(result.contains("JOINED1 - 1"));
                assertTrue(result.contains("JOINED2 - 1"));
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
                final CriteriaBuilder<String> cb = cbf.create(em, String.class)
                        .withReturning(IdHolderCTE.class)
                            .update(PolymorphicPropertyBase.class, "t")
                            .setExpression("name", "CONCAT(name, ' - 1')")
                            .returning("id", "id")
                        .end()
                        .from(IdHolderCTE.class, "cte")
                        .fromNew(PolymorphicPropertyBase.class, "t")
                        .where("t.id").eqExpression("cte.id")
                        .select("t.name");

                String expected = "WITH IdHolderCTE(id) AS(\n" +
                        "UPDATE PolymorphicPropertyBase t SET t.name = CONCAT(name,' - 1') RETURNING id\n" +
                        ")\n" +
                        "SELECT t.name FROM IdHolderCTE cte, NEW(PolymorphicPropertyBase) t WHERE t.id = cte.id";


                assertEquals(expected, cb.getQueryString());

                List<String> result = cb.getResultList();
                assertEquals(2, result.size());
                assertTrue(result.contains("ST1 - 1"));
                assertTrue(result.contains("ST2 - 1"));
            }
        });
    }

}
