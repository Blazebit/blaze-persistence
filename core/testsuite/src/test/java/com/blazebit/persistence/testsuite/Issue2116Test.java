/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.testsuite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import javax.persistence.EntityManager;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatement;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDB2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoFirebird;
import com.blazebit.persistence.testsuite.base.jpa.category.NoH2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMSSQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.base.jpa.category.NoSQLite;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;

/**
 * Test for issue #2116: {@code cast_boolean} rendered {@code cast(... as number(1,0))} on PostgreSQL,
 * which fails because PostgreSQL has no {@code number} type.
 *
 * @author SoSo-MIH
 * @since 1.6.21
 */
public class Issue2116Test extends AbstractCoreTest {

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Person p1 = new Person("P1");
                em.persist(p1);

                Document d1 = new Document("D1");
                d1.setAge(5L);
                d1.setOwner(p1);
                em.persist(d1);
            }
        });
    }

    @Test
    @Category({ NoMySQL.class, NoH2.class, NoDB2.class, NoOracle.class, NoSQLite.class, NoFirebird.class, NoMSSQL.class, NoEclipselink.class })
    public void castBooleanUsesNativeBooleanTypeOnPostgreSQL() {
        CriteriaBuilder<Boolean> cb = cbf.create(em, Boolean.class).from(Document.class, "d")
                .select("FUNCTION('cast_boolean', CASE WHEN d.age > 1 THEN 1 ELSE 0 END)")
                .where("d.name").eq("D1");

        enableQueryCollecting();
        try {
            clearQueries();
            Boolean result = cb.getSingleResult();
            assertUnorderedQuerySequence()
                    .addStatement(new AssertStatement() {
                        @Override
                        public void validate(String query) {
                            assertTrue(query, Pattern.matches("(?is).*cast\\(.*as boolean\\).*", query));
                        }
                    })
                    .validate();

            assertEquals(Boolean.TRUE, result);
        } finally {
            disableQueryCollecting();
        }
    }
}
