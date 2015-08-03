/*
 * Copyright 2014 Blazebit.
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

import com.blazebit.persistence.entity.Document;
import com.blazebit.persistence.entity.Person;
import com.blazebit.persistence.function.ConcatenateFunction;
import com.blazebit.persistence.function.ZeroFunction;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.spi.JpqlFunctionGroup;
import com.blazebit.persistence.testsuite.base.category.NoDB2;

import static com.googlecode.catchexception.CatchException.verifyException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Tuple;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class SelectTest extends AbstractCoreTest {

    @Override
    protected CriteriaBuilderConfiguration configure(CriteriaBuilderConfiguration config) {
        config = super.configure(config);
        config.registerFunction(new JpqlFunctionGroup("array", new ZeroFunction()));
        config.registerFunction(new JpqlFunctionGroup("unnest", new ZeroFunction()));
        return config;
    }
    
    @Override
    protected Set<String> getRegisteredFunctions() {
        Set<String> functions = super.getRegisteredFunctions();
        functions.addAll(Arrays.asList(
                "array",
                "unnest"));
        return functions;
    }
    
    @Test
    public void testSelectCountStar() {
        CriteriaBuilder<Integer> criteria = cbf.create(em, Integer.class).from(Document.class, "d");
        criteria.select("COUNT(*)");

        assertEquals("SELECT COUNT(*) FROM Document d", criteria.getQueryString());
        criteria.getResultList();
    }
    
    @Test
    public void testSelectNonEntity() {
        CriteriaBuilder<Integer> criteria = cbf.create(em, Integer.class).from(Document.class, "d");
        criteria.select("d.age");

        assertEquals("SELECT d.age FROM Document d", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectNonJoinable() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("nonJoinable");

        assertEquals("SELECT d.nonJoinable FROM Document d", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectNonJoinablePrefixed() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("d.nonJoinable");

        assertEquals("SELECT d.nonJoinable FROM Document d", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectJoinable() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("partners");

        assertEquals("SELECT partners_1 FROM Document d LEFT JOIN d.partners partners_1", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectJoinablePrefixed() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("d.partners");

        assertEquals("SELECT partners_1 FROM Document d LEFT JOIN d.partners partners_1", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectScalarExpression() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("d.partners + 1");

        assertEquals("SELECT partners_1 + 1 FROM Document d LEFT JOIN d.partners partners_1", criteria.getQueryString());
    }

    @Test
    public void testSelectMultiple() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("d.partners").select("d.versions");

        assertEquals("SELECT partners_1, versions_1"
                + " FROM Document d LEFT JOIN d.partners partners_1 LEFT JOIN d.versions versions_1", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectAlias() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("d.partners", "p").where("p").isNull();

        assertEquals("SELECT partners_1 AS p FROM Document d LEFT JOIN d.partners partners_1 WHERE "
                + "partners_1 IS NULL", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectAliasReplacement() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("d.partners", "p").where("partners").isNull();

        assertEquals("SELECT partners_1 AS p FROM Document d LEFT JOIN d.partners partners_1 WHERE "
                + "partners_1 IS NULL", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectAliasJoin() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("d.partners", "p").where("p").isNull();

        assertEquals("SELECT partners_1 AS p FROM Document d LEFT JOIN d.partners partners_1 WHERE "
                + "partners_1 IS NULL", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectAliasJoin2() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("d.versions.date", "x").where("SIZE(d.partners)").eq(2);

        assertEquals(
                "SELECT versions_1.date AS x FROM Document d LEFT JOIN d.versions versions_1 WHERE SIZE(d.partners) = :param_0",
                criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectAliasJoin3() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("d").select("C.name").innerJoin("d.versions", "B").innerJoin("B.document", "C");

        assertEquals("SELECT d, C.name FROM Document d JOIN d.versions B JOIN B.document C", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectAliasJoin4() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("d").select("C.id", "X").innerJoin("d.versions", "B").innerJoin("B.document", "C").where("X")
                .eqExpression("B.id");

        assertEquals("SELECT d, C.id AS X FROM Document d JOIN d.versions B JOIN B.document C WHERE C.id = B.id", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectAliasJoin5() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("d.partners.name");

        assertEquals("SELECT partners_1.name FROM Document d LEFT JOIN d.partners partners_1", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectAliasJoin6() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "a");
        criteria.select("a.versions");

        assertEquals("SELECT versions_1 FROM Document a LEFT JOIN a.versions versions_1", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectAliasJoin7() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");

        // we have already solved this for join aliases so we should also solve it here
        criteria.select("test.name", "fieldAlias").where("test.name").eq("bla").join("owner", "test", JoinType.LEFT, false);

        assertEquals("SELECT test.name AS fieldAlias FROM Document d LEFT JOIN d.owner test WHERE test.name = :param_0", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testAmbiguousAliasReplacement() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("name", "name").where("d.name").eq("abc").getQueryString();

        String expected = "SELECT d.name AS name FROM Document d WHERE d.name = :param_0";

        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectSingleEmpty() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria, IllegalArgumentException.class).select("");
    }

    @Test
    public void testSelectMultipleEmpty() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria, IllegalArgumentException.class).select("", "");
    }

    @Test
    public void testSelectNull() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria, NullPointerException.class).select((String) null);
    }

    @Test
    public void testSelectArrayNull() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria, NullPointerException.class).select((String) null);
    }

    @Test
    public void testSelectSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.selectSubquery("alias", "1 + alias").from(Person.class, "p").select("COUNT(id)").end();

        assertEquals("SELECT 1 + (SELECT COUNT(p.id) FROM Person p) FROM Document d", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectSubqueryWithSurroundingExpressionWithAlias() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.selectSubquery("alias", "1 + alias", "alias").from(Person.class, "p").select("COUNT(id)").end();

        assertEquals("SELECT 1 + (SELECT COUNT(p.id) FROM Person p) AS alias FROM Document d", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectMultipleSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.selectSubquery("alias", "alias * alias", "alias").from(Person.class, "p").select("COUNT(id)").end();

        assertEquals("SELECT (SELECT COUNT(p.id) FROM Person p) * (SELECT COUNT(p.id) FROM Person p) AS alias FROM Document d", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testGetSingleResult() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class).from(Document.class, "d");
        CriteriaBuilder<Tuple> t = criteria.select("COUNT(d.id)");

        assertEquals("SELECT COUNT(d.id) FROM Document d", criteria.getQueryString());
        assertEquals(0L, t.getSingleResult().get(0));
    }

    @Test
    public void testSelectSizeAsCount() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("CASE WHEN SIZE(d.contacts) > 2 THEN 2 ELSE SIZE(d.contacts) END");

        String expected = "SELECT CASE WHEN COUNT(" + joinAliasValue("contacts_1") + ") > 2 THEN 2 ELSE COUNT(" + joinAliasValue("contacts_1") + ") END FROM Document d LEFT JOIN d.contacts contacts_1 GROUP BY d.id";
        assertEquals(expected, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testSelectSizeAsSubexpression() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("CASE WHEN SIZE(d.contacts) > 2 THEN 2 ELSE 0 END")
                .where("d.partners.name").like().expression("'%onny'").noEscape();

        String expectedSubquery = "SELECT COUNT(" + joinAliasValue("contacts") + ") FROM Document document LEFT JOIN document.contacts contacts WHERE document = d";
        String expected = "SELECT CASE WHEN (" + expectedSubquery + ") > 2 THEN 2 ELSE 0 END FROM Document d LEFT JOIN d.partners partners_1 WHERE partners_1.name LIKE '%onny'";
        assertEquals(expected, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testSelectAggregate() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("SIZE(versions)")
                .select("owner.name")
                .orderByDesc("id");

        String objectQuery = "SELECT COUNT(versions_1), owner_1.name FROM Document d JOIN d.owner owner_1 LEFT JOIN d.versions versions_1 GROUP BY d.id, owner_1.name ORDER BY " + renderNullPrecedence("d.id", "DESC", "LAST");
        assertEquals(objectQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testSelectAggregatePaginated() {
        PaginatedCriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .select("SIZE(versions)")
                .select("owner.name")
                .orderByDesc("id")
                .page(0, 10);

        String countQuery = "SELECT COUNT(DISTINCT d.id) FROM Document d";
        String idQuery = "SELECT d.id FROM Document d GROUP BY d.id ORDER BY " + renderNullPrecedence("d.id", "DESC", "LAST");
        String objectQuery = "SELECT COUNT(versions_1), owner_1.name FROM Document d JOIN d.owner owner_1 LEFT JOIN d.versions versions_1 WHERE d.id IN :ids GROUP BY d.id, owner_1.name ORDER BY " + renderNullPrecedence("d.id", "DESC", "LAST");

        assertEquals(countQuery, cb.getPageCountQueryString());
        assertEquals(idQuery, cb.getPageIdQueryString());
        assertEquals(objectQuery, cb.getQueryString());

        cb.getResultList();
    }
    
    @Test
    @Category(NoDB2.class)
    public void testSelectNestedAggregate() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .selectCase().when("MIN(lastModified)").gtExpression("creationDate").thenExpression("MIN(lastModified)").otherwiseExpression("CURRENT_TIMESTAMP")
                .select("owner.name")
                .orderByDesc("id");

        // TODO: DB2 wants us to also put creationDate and lastModified into the group by...
        String objectQuery = "SELECT CASE WHEN MIN(d.lastModified) > d.creationDate THEN MIN(d.lastModified) ELSE CURRENT_TIMESTAMP() END, owner_1.name "
        		+ "FROM Document d JOIN d.owner owner_1 "
        		+ "GROUP BY owner_1.name, d.id "
        		+ "ORDER BY " + renderNullPrecedence("d.id", "DESC", "LAST");
        assertEquals(objectQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testSelectNestedAggregatePaginated() {
        PaginatedCriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .selectCase().when("MIN(lastModified)").gtExpression("creationDate").thenExpression("MIN(lastModified)").otherwiseExpression("CURRENT_TIMESTAMP")
                .select("owner.name")
                .orderByDesc("id")
                .page(0, 10);

        String countQuery = "SELECT COUNT(DISTINCT d.id) FROM Document d";
        String idQuery = "SELECT d.id FROM Document d GROUP BY d.id ORDER BY " + renderNullPrecedence("d.id", "DESC", "LAST");
        String objectQuery = "SELECT CASE WHEN MIN(d.lastModified) > d.creationDate THEN MIN(d.lastModified) ELSE CURRENT_TIMESTAMP() END, owner_1.name FROM Document d JOIN d.owner owner_1 "
                + "GROUP BY owner_1.name, d.id ORDER BY " + renderNullPrecedence("d.id", "DESC", "LAST");

        assertEquals(countQuery, cb.getPageCountQueryString());
        assertEquals(idQuery, cb.getPageIdQueryString());
        assertEquals(objectQuery, cb.getQueryString());

        cb.getResultList();
    }
    
    @Test
    @Category(NoDB2.class)
    public void testSelectAggregateEntitySelect() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .selectCase().when("MIN(lastModified)").gtExpression("creationDate").thenExpression("MIN(lastModified)").otherwiseExpression("CURRENT_TIMESTAMP")
                .select("owner")
                .orderByDesc("id");

        // TODO: DB2 wants us to also put creationDate and lastModified into the group by...
        String objectQuery = "SELECT CASE WHEN MIN(d.lastModified) > d.creationDate THEN MIN(d.lastModified) ELSE CURRENT_TIMESTAMP() END, owner_1 FROM Document d "
                + "JOIN d.owner owner_1 "
                + "GROUP BY owner_1.age, owner_1.id, owner_1.name, owner_1.partnerDocument, d.id "
                + "ORDER BY " + renderNullPrecedence("d.id", "DESC", "LAST");

        assertEquals(objectQuery, cb.getQueryString());
        
        cb.getResultList();
    }
    
    @Test
    public void testSelectAggregateEntitySelectPaginated() {
        PaginatedCriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
                .selectCase().when("MIN(lastModified)").gtExpression("creationDate").thenExpression("MIN(lastModified)").otherwiseExpression("CURRENT_TIMESTAMP")
                .select("owner")
                .orderByDesc("id")
                .page(0, 10);

        String countQuery = "SELECT COUNT(DISTINCT d.id) FROM Document d";
        String idQuery = "SELECT d.id FROM Document d GROUP BY d.id ORDER BY " + renderNullPrecedence("d.id", "DESC", "LAST");
        String objectQuery = "SELECT CASE WHEN MIN(d.lastModified) > d.creationDate THEN MIN(d.lastModified) ELSE CURRENT_TIMESTAMP() END, owner_1 FROM Document d "
                + "JOIN d.owner owner_1 "
                + "GROUP BY owner_1.age, owner_1.id, owner_1.name, owner_1.partnerDocument, d.id "
                + "ORDER BY " + renderNullPrecedence("d.id", "DESC", "LAST");

        assertEquals(countQuery, cb.getPageCountQueryString());
        assertEquals(idQuery, cb.getPageIdQueryString());
        assertEquals(objectQuery, cb.getQueryString());

        cb.getResultList();
    }
    
    @Test
    public void testSelectSuperExpressionSubquery(){
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class);
        cb.selectSubquery("alias", "FUNCTION('ARRAY',alias)")
                .from(Document.class, "d2")
                .select("FUNCTION('UNNEST',d2.creationDate)")
                .distinct()
        .end();
        
        assertEquals("SELECT " + function("ARRAY", "(SELECT DISTINCT " + function("UNNEST","d2.creationDate") + " FROM Document d2)") + " FROM Document document", cb.getQueryString());
    }
    // TODO: whats with this test?
//    @Test
//    public void testSelectAggregateEntitySelectLazyAssociation() {
//        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class, "d")
//                .selectCase().when("MIN(lastModified)").gtExpression("creationDate").then("MIN(lastModified)").otherwiseExpression("CURRENT_TIMESTAMP")
//                .select("owner")
//                .orderByDesc("id");
//
//        String objectQuery = "SELECT CASE WHEN MIN(d.lastModified) > d.creationDate THEN MIN(d.lastModified) ELSE CURRENT_TIMESTAMP() END, owner_1 FROM Document d "
//                + "JOIN d.owner owner_1 "
//                + "GROUP BY d.creationDate, owner_1.age, owner_1.id, owner_1.name, owner_1.partnerDocument, d.id "
//                + "ORDER BY d.id DESC NULLS LAST";
//
//        assertEquals(objectQuery, cb.getQueryString());
//        
//        cb.getResultList();
//    }
    
}
