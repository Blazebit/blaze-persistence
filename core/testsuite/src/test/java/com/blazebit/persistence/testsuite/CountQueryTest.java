/*
 * Copyright 2014 - 2021 Blazebit.
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
import com.blazebit.persistence.impl.function.alias.AliasFunction;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class CountQueryTest extends AbstractCoreTest {

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Document doc1 = new Document("doc1");
                Document doc2 = new Document("Doc2");
                Document doc3 = new Document("doC3");
                Document doc4 = new Document("dOc4");
                Document doc5 = new Document("DOC5");
                Document doc6 = new Document("bdoc");
                Document doc7 = new Document("adoc");

                Person o1 = new Person("Karl1");
                Person o2 = new Person("Karl2");
                Person o3 = new Person("Moritz");
                o1.getLocalized().put(1, "abra kadabra");
                o2.getLocalized().put(1, "ass");

                doc1.setOwner(o1);
                doc2.setOwner(o1);
                doc3.setOwner(o1);
                doc4.setOwner(o2);
                doc5.setOwner(o2);
                doc6.setOwner(o2);
                doc7.setOwner(o2);

                doc1.getContacts().put(1, o1);
                doc1.getContacts().put(2, o2);

                doc4.getContacts().put(1, o3);

                em.persist(o1);
                em.persist(o2);
                em.persist(o3);

                em.persist(doc1);
                em.persist(doc2);
                em.persist(doc3);
                em.persist(doc4);
                em.persist(doc5);
                em.persist(doc6);
                em.persist(doc7);
            }
        });
    }

    @Test
    public void countQueryFromSimpleQuery() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");

        // do not include joins that are only needed for the select clause
        String expectedCountQuery = "SELECT " + countStar() + " FROM Document d";
        String expectedQueryRootCountQuery = "SELECT " + countPaginated("d.id", false) + " FROM Document d";
        assertEquals(expectedCountQuery, crit.getCountQueryString());
        assertEquals(expectedQueryRootCountQuery, crit.getQueryRootCountQueryString());
        crit.getCountQuery().getResultList();
        crit.getQueryRootCountQuery().getResultList();
    }

    @Test
    public void countQueryFromCollectionJoinQuery() {
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class)
                .from(Document.class, "d")
                .select("d.id")
                .select("partners.id");

        // do not include joins that are only needed for the select clause
        String expectedCountQuery = "SELECT " + countStar() + " FROM Document d LEFT JOIN d.partners partners_1";
        String expectedQueryRootCountQuery = "SELECT " + countPaginated("d.id", false) + " FROM Document d";
        assertEquals(expectedCountQuery, crit.getCountQueryString());
        assertEquals(expectedQueryRootCountQuery, crit.getQueryRootCountQueryString());
        crit.getCountQuery().getResultList();
        crit.getQueryRootCountQuery().getResultList();
    }

    @Test
    @Category(NoEclipselink.class)
    // Eclipselink does not render the table alias necessary for the path expression in the count function...
    public void countQueryWithGroupBy() {
        CriteriaBuilder<Long> crit = cbf.create(em, Long.class)
                .from(Document.class, "d")
                .select("COUNT(*)")
                .groupBy("name");

        // do not include joins that are only needed for the select clause
        String expectedRootCountQuery = "SELECT " + countPaginated("d.name", true) + " FROM Document d";
        String expectedCountQuery;
        if (dbmsDialect.supportsCountTuple() || !supportsAdvancedSql()) {
            expectedCountQuery = expectedRootCountQuery;
        } else {
            expectedCountQuery = "SELECT COUNT(*) FROM (SELECT COUNT(*) FROM Document d GROUP BY d.name)";
        }
        assertEquals(expectedCountQuery, crit.getCountQueryString());
        assertEquals(expectedRootCountQuery, crit.getQueryRootCountQueryString());
        crit.getCountQuery().getResultList();
        crit.getQueryRootCountQuery().getResultList();
    }

    // NOTE: This requires advanced SQL support
    @Test
    @Category({ NoEclipselink.class, NoDatanucleus.class, NoOpenJPA.class })
    public void countQueryWithGroupByAndLimit() {
        CriteriaBuilder<Long> crit = cbf.create(em, Long.class)
                .from(Document.class, "d")
                .select("name")
                .groupBy("name")
                .orderByAsc("name")
                .setFirstResult(1)
                .setMaxResults(1);

        // do not include joins that are only needed for the select clause
        String expectedCountQuery = "SELECT COUNT(*) FROM (SELECT d.name FROM Document d GROUP BY d.name ORDER BY d.name ASC LIMIT 1 OFFSET 1)";
        assertEquals(expectedCountQuery, crit.getCountQueryString());
        assertEquals(expectedCountQuery, crit.getQueryRootCountQueryString());
        crit.getCountQuery().getResultList();
        crit.getQueryRootCountQuery().getResultList();
    }

    // NOTE: This requires advanced SQL support
    @Test
    @Category({ NoEclipselink.class, NoDatanucleus.class, NoOpenJPA.class })
    public void countQueryFromHavingQuery() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.select("d.id")
                .groupBy("d.id")
                .having("COUNT(partners.id)").eqExpression("1");
        String expectedCountQuery = "SELECT COUNT(*) FROM (SELECT d.id FROM Document d LEFT JOIN d.partners partners_1 GROUP BY d.id HAVING COUNT(partners_1.id) = 1)";
        assertEquals(expectedCountQuery, crit.getCountQueryString());
        assertEquals(expectedCountQuery, crit.getQueryRootCountQueryString());
        crit.getCountQuery().getResultList();
        crit.getQueryRootCountQuery().getResultList();
    }

    @Test
    @Category(NoEclipselink.class)
    // Eclipselink does not render the table alias necessary for the path expression in the count function...
    public void countQueryWithDistinct() {
        CriteriaBuilder<Long> crit = cbf.create(em, Long.class)
                .from(Document.class, "d")
                .select("name").distinct();

        // do not include joins that are only needed for the select clause
        String expectedRootCountQuery = "SELECT " + countPaginated("d.name", true) + " FROM Document d";
        String expectedCountQuery;
        if (dbmsDialect.supportsCountTuple() || !supportsAdvancedSql()) {
            expectedCountQuery = expectedRootCountQuery;
        } else {
            expectedCountQuery = "SELECT COUNT(*) FROM (SELECT DISTINCT d.name FROM Document d)";
        }
        assertEquals(expectedCountQuery, crit.getCountQueryString());
        assertEquals(expectedRootCountQuery, crit.getQueryRootCountQueryString());
        crit.getCountQuery().getResultList();
        crit.getQueryRootCountQuery().getResultList();
    }

    @Test
    @Category({ NoEclipselink.class, NoDatanucleus.class, NoOpenJPA.class })
    // TODO: report eclipselink does not support subqueries in functions
    public void boundedCountQueryFromSimpleQuery() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");

        // do not include joins that are only needed for the select clause
        String expectedCountQuery = "SELECT COUNT(*) FROM (SELECT " + function(AliasFunction.FUNCTION_NAME, "1", " 'c'") + " FROM Document d LIMIT 2)";
        String expectedQueryRootCountQuery = "SELECT COUNT(*) FROM (SELECT " + countPaginatedBounded(function(AliasFunction.FUNCTION_NAME, "d.id", " 'c0'"), false) + " FROM Document d LIMIT 2)";
        assertEquals(expectedCountQuery, crit.getCountQueryString(2));
        assertEquals(expectedQueryRootCountQuery, crit.getQueryRootCountQueryString(2));
        crit.getCountQuery(2).getResultList();
        crit.getQueryRootCountQuery(2).getResultList();
    }

    @Test
    @Category({ NoEclipselink.class, NoDatanucleus.class, NoOpenJPA.class })
    // TODO: report eclipselink does not support subqueries in functions
    public void boundedCountQueryFromCollectionJoinQuery() {
        CriteriaBuilder<Tuple> crit = cbf.create(em, Tuple.class)
                .from(Document.class, "d")
                .select("d.id")
                .select("partners.id");

        // do not include joins that are only needed for the select clause
        String expectedCountQuery = "SELECT COUNT(*) FROM (SELECT " + function(AliasFunction.FUNCTION_NAME, "1", " 'c'") + " FROM Document d LEFT JOIN d.partners partners_1 LIMIT 2)";
        String expectedQueryRootCountQuery = "SELECT COUNT(*) FROM (SELECT " + countPaginatedBounded(function(AliasFunction.FUNCTION_NAME, "d.id", " 'c0'"), false) + " FROM Document d LIMIT 2)";
        assertEquals(expectedCountQuery, crit.getCountQueryString(2));
        assertEquals(expectedQueryRootCountQuery, crit.getQueryRootCountQueryString(2));
        crit.getCountQuery(2).getResultList();
        crit.getQueryRootCountQuery(2).getResultList();
    }
}
