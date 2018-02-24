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

import java.util.Locale;

import javax.persistence.Tuple;

import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Workflow;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class MultipleJoinTest extends AbstractCoreTest {

    @Override
    protected void setUpOnce() {
        // TODO: Remove me when DataNucleus fixes map value access: https://github.com/datanucleus/datanucleus-rdbms/issues/230
        cleanDatabase();
    }

    @Test
    public void testExcplicitMultipleJoins() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Workflow.class)
            .leftJoin("localized", "l1")
            .leftJoin("localized", "l2")
            .select("id");
        String expectedQuery = "SELECT workflow.id FROM Workflow workflow"
            + " LEFT JOIN workflow.localized l1"
            + " LEFT JOIN workflow.localized l2";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testOneExplicitJoinAndOneExplicitDefaultJoin() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Workflow.class)
            .leftJoinOn("localized", "l1").on("KEY(l1)").eqExpression(":locale").end()
            .leftJoinDefault("localized", "l2")
            .select("localized[:locale].name")
            .select("localized.name");
        String expectedQuery = "SELECT " + joinAliasValue("l1", "name") + ", " + joinAliasValue("l2", "name") + " FROM Workflow workflow"
            + " LEFT JOIN workflow.localized l1"
                + onClause("KEY(l1) = :locale")
            + " LEFT JOIN workflow.localized l2";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.setParameter("locale", Locale.GERMAN)
            .getResultList();
    }

    @Test
    public void testOneImplicitJoinAndOneImplicitDefaultJoin() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Workflow.class)
            .select("localized[:locale].name")
            .select("localized.name");
        String expectedQuery = "SELECT " + joinAliasValue("localized_locale_1", "name") + ", " + joinAliasValue("localized_1", "name") + " FROM Workflow workflow"
            + " LEFT JOIN workflow.localized localized_1"
            + " LEFT JOIN workflow.localized localized_locale_1"
                + onClause("KEY(localized_locale_1) = :locale");
        assertEquals(expectedQuery, cb.getQueryString());
        cb.setParameter("locale", Locale.GERMAN)
            .getResultList();
    }

    @Test
    public void testFirstOneImplicitJoinAndOneImplicitDefaultJoinThenExplicit() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Workflow.class)
            .select("localized[:locale].name")
            .select("localized.name")
            .leftJoinOn("localized", "l1").on("KEY(l1)").eqExpression(":locale").end()
            .leftJoinDefault("localized", "l2");
        String expectedQuery = "SELECT " + joinAliasValue("l1", "name") + ", " + joinAliasValue("l2", "name") + " FROM Workflow workflow"
            + " LEFT JOIN workflow.localized l1"
                + onClause("KEY(l1) = :locale")
            + " LEFT JOIN workflow.localized l2";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.setParameter("locale", Locale.GERMAN)
            .getResultList();
    }

    @Test
    public void testExcplicitMultipleJoinsWithParameterMatch() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Workflow.class)
            .leftJoinOn("localized", "l1").on("KEY(l1)").eqExpression(":locale").end()
            .leftJoinOn("localized", "l2").on("KEY(l2)").eqExpression("workflow.defaultLanguage").end()
            .select("localized[:locale].name")
            .select("localized[defaultLanguage].name");
        String expectedQuery = "SELECT " + joinAliasValue("l1", "name") + ", " + joinAliasValue("l2", "name") + " FROM Workflow workflow"
            + " LEFT JOIN workflow.localized l1"
                + onClause("KEY(l1) = :locale")
            + " LEFT JOIN workflow.localized l2"
                + onClause("KEY(l2) = workflow.defaultLanguage");
        assertEquals(expectedQuery, cb.getQueryString());
        cb.setParameter("locale", Locale.GERMAN)
            .getResultList();
    }

    @Test
    public void testMultipleNestedJoins() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class)
            .leftJoin("partners", "p1")
            .leftJoin("partners", "p2")
            .leftJoin("p1.localized", "l11")
            .leftJoin("p1.localized", "l12")
            .leftJoin("p2.localized", "l21")
            .leftJoin("p2.localized", "l22")
            .select("l11").select("l12")
            .select("l21").select("l22");
        String expectedQuery = "SELECT "
            + joinAliasValue("l11") + ", "
            + joinAliasValue("l12") + ", "
            + joinAliasValue("l21") + ", "
            + joinAliasValue("l22") + " "
            + "FROM Document document"
            + " LEFT JOIN document.partners p1"
            + " LEFT JOIN p1.localized l11"
            + " LEFT JOIN p1.localized l12"
            + " LEFT JOIN document.partners p2"
            + " LEFT JOIN p2.localized l21"
            + " LEFT JOIN p2.localized l22";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testMultipleNestedJoinsWithDefault() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class)
            .leftJoin("partners", "p1")
            .leftJoin("partners.localized", "l")
            .where("p1.partnerDocument.name").eq("doc")
            .select("l");

        String expectedQuery = "SELECT " + joinAliasValue("l") + " FROM Document document"
            + " LEFT JOIN document.partners p1"
            + " LEFT JOIN p1.partnerDocument partnerDocument_1"
            + " LEFT JOIN document.partners partners_1"
            + " LEFT JOIN partners_1.localized l"
            + " WHERE partnerDocument_1.name = :param_0";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }
}
