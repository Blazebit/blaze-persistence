/*
 * Copyright 2014 - 2019 Blazebit.
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

import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus4;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import org.junit.experimental.categories.Category;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class JoinOnTest extends AbstractCoreTest {

    @Test
    public void testLeftJoinOn() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.leftJoinOn("d.partners.localized", "l").on("l").like().value("%dld").noEscape().end();

        assertEquals(
            "SELECT d FROM Document d LEFT JOIN d.partners partners_1 LEFT JOIN partners_1.localized l"
                    + onClause(joinAliasValue("l") + " LIKE :param_0"), crit.getQueryString());
        crit.getResultList();
    }

    @Test
    @Category(NoEclipselink.class)
    // Eclipselink does not support right joins
    public void testRightJoinOn() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.rightJoinOn("d.partners.localized", "l").on("l").like().value("%dld").noEscape().end();

        assertEquals(
            "SELECT d FROM Document d LEFT JOIN d.partners partners_1 RIGHT JOIN partners_1.localized l"
                    + onClause(joinAliasValue("l") + " LIKE :param_0"), crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testInnerJoinOn() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.innerJoinOn("d.partners.localized", "l").on("l").like().value("%dld").noEscape().end();

        assertEquals("SELECT d FROM Document d LEFT JOIN d.partners partners_1" +
                        " JOIN partners_1.localized l"
                        + onClause(joinAliasValue("l") + " LIKE :param_0"),
                crit.getQueryString());
        crit.getResultList();
    }

    @Test
    public void testLeftJoinOnComplex() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.leftJoinOn("d.partners.localized", "l").on("l").like().value("%dld").noEscape()
            .on("l").gt("1")
            .onOr()
                .on("l").eq("2")
                .onAnd()
                    .on("l").eq("3")
                    .onOr()
                        .on("l").eq("4")
                    .endOr()
                .endAnd()
            .endOr().end();

        assertEquals(
            "SELECT d FROM Document d LEFT JOIN d.partners partners_1 LEFT JOIN partners_1.localized l"
                    + onClause(joinAliasValue("l") + " LIKE :param_0 AND " + joinAliasValue("l") + " > :param_1 AND (" + joinAliasValue("l") + " = :param_2 OR (" + joinAliasValue("l") + " = :param_3 AND (" + joinAliasValue("l") + " = :param_4)))"),
            crit.getQueryString());
        crit.getResultList();
    }
    
    @Test
    public void testJoinOnOuterRoot(){
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d")
                .select("d")
                .selectSubquery()
                    .from(Person.class, "p")
                    .select("partnerDoc.id")
                    .select("p.name")
                    .innerJoinOn("p.partnerDocument", "partnerDoc")
                        .on("p.id").eqExpression("OUTER(d.idx)")
                    .end()
                .end()
                .where("d.id").eq(1);
        
        assertEquals("SELECT d, (SELECT partnerDoc.id, p.name FROM Person p" +
                " JOIN p.partnerDocument partnerDoc"
                + onClause("p.id = d.idx")
                + ") FROM Document d WHERE d.id = :param_0", crit.getQueryString());
        // the query causes an exception in Hibernate so we do not run it here
    }

    @Test
    public void testLeftJoinOnSubquery() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class, "d");
        crit.leftJoinDefaultOn("d.partners.localized", "l")
                .on("l").in()
                    .from(Person.class, "p")
                    .select("p.name")
                .end()
                .end();

        assertEquals(
                "SELECT d FROM Document d LEFT JOIN d.partners partners_1 LEFT JOIN partners_1.localized l"
                        + onClause(joinAliasValue("l") + " IN (SELECT p.name FROM Person p)"), crit.getQueryString());
        crit.getResultList();
    }

    @Test
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus4.class, NoOpenJPA.class})
    public void testJoinAvoidance() {
        CriteriaBuilder<String> crit = cbf.create(em, String.class).from(Document.class, "e");
        crit.select("e.responsiblePerson.name");
        crit.orderByAsc("e.id");

        CriteriaBuilder<String> criteriaBuilder = crit.copy(String.class);
        criteriaBuilder.innerJoinOn("e.responsiblePerson.friend", Document.class, "d2")
                .on("e.responsiblePerson.friend.name").eqExpression("d2.name")
                .end();
        criteriaBuilder.select("d2.name");

        String expectedObjectQuery = "SELECT d2.name FROM Document e "
                + "LEFT JOIN e.responsiblePerson responsiblePerson_1 "
                + "LEFT JOIN responsiblePerson_1.friend friend_1 "
                + "JOIN Document d2" + onClause("friend_1.name = d2.name")
                + " ORDER BY e.id ASC";

        assertEquals(expectedObjectQuery, criteriaBuilder.getQueryString());
        criteriaBuilder.getResultList();
    }

    @Test
    @Category({ NoEclipselink.class, NoHibernate42.class, NoHibernate43.class, NoHibernate50.class })
    // NOTE: Subquery support in ON clause is only possible with Hibernate 5.1+
    // TODO: report eclipselink does not support subqueries in functions
    public void testJoinWithSubqueryBuilder() {
        CriteriaBuilder<String> crit = cbf.create(em, String.class).from(Document.class, "e");
        crit.innerJoinOn("e.owner", "o")
                .onOr()
                    .on("o.name").eq("Test")
                    .on("o.age").eqSubqueries("maximize")
                        .with("maximize")
                            .from(Person.class, "p")
                            .select("p.age")
                            .orderByDesc("p.age")
                            .setMaxResults(1)
                        .end()
                    .end()
                .endOr()
            .end();
        crit.getResultList();
    }
}
