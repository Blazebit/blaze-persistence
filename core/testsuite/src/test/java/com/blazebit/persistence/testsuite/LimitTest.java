/*
 * Copyright 2014 - 2022 Blazebit.
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
import com.blazebit.persistence.testsuite.base.jpa.category.NoDB2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoFirebird;
import com.blazebit.persistence.testsuite.base.jpa.category.NoH2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMSSQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.base.jpa.category.NoSQLite;
import com.blazebit.persistence.testsuite.entity.DeletePersonCTE;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.testsuite.entity.Workflow;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public class LimitTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            Document.class,
            Version.class,
            Person.class,
            Workflow.class,
            IntIdEntity.class,
            DeletePersonCTE.class
        };
    }

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Person o1 = new Person("P1");
                em.persist(o1);
                em.flush();

                Person o2 = new Person("P2");
                em.persist(o2);
                em.flush();
            }
        });
    }
    
    @Test
    @Category(NoEclipselink.class)
    // TODO: report eclipselink does not support subqueries in functions
    public void testSubqueryLimit() {
        CriteriaBuilder<Person> cb = cbf.create(em, Person.class, "p");
        cb.where("p.id").in()
            .from(Person.class, "pSub")
            .select("pSub.id")
            .orderByAsc("pSub.id")
            .setMaxResults(1)
        .end();
        String expected = "SELECT p FROM Person p WHERE p.id IN (SELECT pSub.id FROM Person pSub ORDER BY pSub.id ASC LIMIT 1)";

        assertEquals(expected, cb.getQueryString());
        List<Person> result = cb.getResultList();
        assertEquals(1, result.size());
        assertEquals("P1", result.get(0).getName());
    }
    
    @Test
    @Category(NoEclipselink.class)
    // TODO: report eclipselink does not support subqueries in functions
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
        String expected = "SELECT p FROM Person p WHERE p.id IN (SELECT pSub.id FROM Person pSub ORDER BY pSub.id ASC LIMIT 2) ORDER BY p.id ASC";

        assertEquals(expected, cb.getQueryString());
        List<Person> result = cb.getResultList();
        assertEquals(1, result.size());
        assertEquals("P1", result.get(0).getName());
    }

    // Test for issue #774
    @Test
    @Category({NoH2.class, NoDB2.class, NoFirebird.class, NoMSSQL.class, NoMySQL.class, NoOracle.class, NoSQLite.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class})
    public void testModificationCteQueryWithLimitInMainQuery() {
        List<DeletePersonCTE> deletedPersons = cbf.create(em, DeletePersonCTE.class)
                .withReturning(DeletePersonCTE.class)
                    .delete(Person.class, "p")
                    .returning("id", "id")
                    .returning("name", "name")
                    .returning("age", "age")
                    .returning("owner", "p")
                .end()
                .from(DeletePersonCTE.class)
                .orderByDesc("name")
                .setMaxResults(1)
                .getResultList();

        assertEquals(1, deletedPersons.size());
        assertEquals("P2", deletedPersons.get(0).getName());
    }
}
