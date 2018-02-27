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

import static org.junit.Assert.assertEquals;

import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
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
}
