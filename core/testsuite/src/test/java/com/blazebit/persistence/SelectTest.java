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
import static com.googlecode.catchexception.CatchException.verifyException;
import javax.persistence.Tuple;
import static org.junit.Assert.assertEquals;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class SelectTest extends AbstractCoreTest {

    @Test
    public void testSelectNonJoinable() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("nonJoinable");

        assertEquals("SELECT d.nonJoinable FROM Document d", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectNonJoinablePrefixed() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d.nonJoinable");

        assertEquals("SELECT d.nonJoinable FROM Document d", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectJoinable() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("partners");

        assertEquals("SELECT " + joinAliasValue("partners_1") + " FROM Document d LEFT JOIN d.partners partners_1", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectJoinablePrefixed() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d.partners");

        assertEquals("SELECT " + joinAliasValue("partners_1") + " FROM Document d LEFT JOIN d.partners partners_1", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectScalarExpression() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d.partners + 1");

        assertEquals("SELECT " + joinAliasValue("partners_1") + " + 1 FROM Document d LEFT JOIN d.partners partners_1", criteria.getQueryString());
    }

    @Test
    public void testSelectMultiple() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d.partners").select("d.versions");

        assertEquals("SELECT " + joinAliasValue("partners_1") + ", " + joinAliasValue("versions_1")
            + " FROM Document d LEFT JOIN d.partners partners_1 LEFT JOIN d.versions versions_1", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectAlias() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d.partners", "p").where("p").isNull();

        assertEquals("SELECT " + joinAliasValue("partners_1") + " AS p FROM Document d LEFT JOIN d.partners partners_1 WHERE "
            + joinAliasValue("partners_1") + " IS NULL", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectAliasReplacement() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d.partners", "p").where("partners").isNull();

        assertEquals("SELECT " + joinAliasValue("partners_1") + " AS p FROM Document d LEFT JOIN d.partners partners_1 WHERE "
            + joinAliasValue("partners_1") + " IS NULL", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectAliasJoin() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d.partners", "p").where("p").isNull();

        assertEquals("SELECT " + joinAliasValue("partners_1") + " AS p FROM Document d LEFT JOIN d.partners partners_1 WHERE "
            + joinAliasValue("partners_1") + " IS NULL", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectAliasJoin2() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d.versions.date", "x").where("SIZE(d.partners)").eq(2);

        assertEquals(
            "SELECT versions_1.date AS x FROM Document d LEFT JOIN d.versions versions_1 WHERE SIZE(d.partners) = :param_0",
            criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectAliasJoin3() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d").select("C.name").innerJoin("d.versions", "B").innerJoin("B.document", "C");

        assertEquals("SELECT d, C.name FROM Document d JOIN d.versions B JOIN B.document C", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectAliasJoin4() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d").select("C.name", "X").innerJoin("d.versions", "B").innerJoin("B.document", "C").where("X")
            .eqExpression("B.id");

        assertEquals("SELECT d, C.name AS X FROM Document d JOIN d.versions B JOIN B.document C WHERE C.name = B.id", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectAliasJoin5() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("d.partners.name");

        assertEquals("SELECT partners_1.name FROM Document d LEFT JOIN d.partners partners_1", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectAliasJoin6() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "a");
        criteria.select("a.versions");

        assertEquals("SELECT " + joinAliasValue("versions_1") + " FROM Document a LEFT JOIN a.versions versions_1", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectAliasJoin7() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");

        // we have already solved this for join aliases so we should also solve it here
        criteria.select("test.name", "fieldAlias").where("test.name").eq("bla").join("owner", "test", JoinType.LEFT, false);

        assertEquals("SELECT test.name AS fieldAlias FROM Document d LEFT JOIN d.owner test WHERE test.name = :param_0", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testAmbiguousAliasReplacement() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.select("name", "name").where("d.name").eq("abc").getQueryString();

        String expected = "SELECT d.name AS name FROM Document d WHERE d.name = :param_0";

        assertEquals(expected, criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testSelectSingleEmpty() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        verifyException(criteria, IllegalArgumentException.class).select("");
    }

    @Test
    public void testSelectMultipleEmpty() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        verifyException(criteria, IllegalArgumentException.class).select("", "");
    }

    @Test
    public void testSelectNull() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        verifyException(criteria, NullPointerException.class).select((String) null);
    }

    @Test
    public void testSelectArrayNull() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        verifyException(criteria, NullPointerException.class).select((String) null);
    }
    
    @Test
    public void testSelectSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.selectSubquery("alias", "1 + alias").from(Person.class, "p").select("COUNT(id)").end();     
        
        assertEquals("SELECT 1 + (SELECT COUNT(p.id) FROM Person p) FROM Document d", criteria.getQueryString());
        criteria.getResultList();
    }
    
    @Test
    public void testSelectSubqueryWithSurroundingExpressionWithAlias() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.selectSubquery("alias", "1 + alias", "alias").from(Person.class, "p").select("COUNT(id)").end();     
        
        assertEquals("SELECT 1 + (SELECT COUNT(p.id) FROM Person p) AS alias FROM Document d", criteria.getQueryString());
        criteria.getResultList();
    }
    
    @Test
    public void testSelectMultipleSubqueryWithSurroundingExpression() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        criteria.selectSubquery("alias", "alias * alias", "alias").from(Person.class, "p").select("COUNT(id)").end();     
        
        assertEquals("SELECT (SELECT COUNT(p.id) FROM Person p) * (SELECT COUNT(p.id) FROM Person p) AS alias FROM Document d", criteria.getQueryString());
        criteria.getResultList();
    }
    
    @Test
    public void testGetSingleResult() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        CriteriaBuilder<Tuple> t = criteria.select("COUNT(d.id)");
        
        assertEquals("SELECT COUNT(d.id) FROM Document d", criteria.getQueryString());
        assertEquals(0L, t.getSingleResult().get(0));
    }
    
    @Test
    public void testSelectSizeAsCount() {
        CriteriaBuilder<Tuple> cb = cbf.from(em, Document.class, "d")
                .select("CASE WHEN SIZE(d.contacts) > 2 THEN 2 ELSE SIZE(d.contacts) END");
        
        String expectedSubquery = "SELECT COUNT(contacts) FROM Document document LEFT JOIN document.contacts contacts WHERE document = d";
        String expected = "SELECT CASE WHEN COUNT(contacts) > 2 THEN 2 ELSE COUNT(contacts) END FROM Document d LEFT JOIN d.contacts contacts";
        assertEquals(expected, cb.getQueryString());
    }
    
    @Test
    public void testSelectSizeAsSubexpression() {
        CriteriaBuilder<Tuple> cb = cbf.from(em, Document.class, "d")
                .select("CASE WHEN SIZE(d.contacts) > 2 THEN 2 ELSE SIZE(d.contacts) END");
        
        String expectedSubquery = "SELECT COUNT(contacts) FROM Document document LEFT JOIN document.contacts contacts WHERE document = d";
        String expected = "SELECT CASE WHEN (" + expectedSubquery + ") > 2 THEN 2 ELSE (" + expectedSubquery + ") END FROM Document d";
        assertEquals(expected, cb.getQueryString());
    }
}
