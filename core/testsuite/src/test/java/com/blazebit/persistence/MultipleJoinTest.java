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
import com.blazebit.persistence.entity.Version;
import com.blazebit.persistence.entity.Workflow;
import java.util.Locale;
import javax.persistence.Tuple;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class MultipleJoinTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            Workflow.class,
            Document.class,
            Version.class,
            Person.class
        };
    }

    @Test
    public void testExcplicitMultipleJoins() {
        CriteriaBuilder<Tuple> cb = cbf.from(em, Workflow.class)
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
        CriteriaBuilder<Tuple> cb = cbf.from(em, Workflow.class)
            .leftJoinOn("localized", "l1").on("KEY(l1)").eqExpression(":locale").end()
            .leftJoinDefault("localized", "l2")
            .select("localized[:locale].name")
            .select("localized.name");
        String expectedQuery = "SELECT l1.name, l2.name FROM Workflow workflow"
            + " LEFT JOIN workflow.localized l1 " + ON_CLAUSE + " KEY(l1) = :locale"
            + " LEFT JOIN workflow.localized l2"
            // TODO: remove when #45 or rather HHH-9329 has been fixed
            + " WHERE l1.description IS NOT NULL AND l1.name IS NOT NULL";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.setParameter("locale", Locale.GERMAN)
            .getResultList();
    }

    @Test
    public void testOneImplicitJoinAndOneImplicitDefaultJoin() {
        CriteriaBuilder<Tuple> cb = cbf.from(em, Workflow.class)
            .select("localized[:locale].name")
            .select("localized.name");
        String expectedQuery = "SELECT localized_locale.name, localized.name FROM Workflow workflow"
            + " LEFT JOIN workflow.localized localized"
            + " LEFT JOIN workflow.localized localized_locale " + ON_CLAUSE + " KEY(localized_locale) = :locale"
            // TODO: remove when #45 or rather HHH-9329 has been fixed
            + " WHERE localized_locale.description IS NOT NULL AND localized_locale.name IS NOT NULL";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.setParameter("locale", Locale.GERMAN)
            .getResultList();
    }

    @Test
    public void testFirstOneImplicitJoinAndOneImplicitDefaultJoinThenExplicit() {
        CriteriaBuilder<Tuple> cb = cbf.from(em, Workflow.class)
            .select("localized[:locale].name")
            .select("localized.name")
            .leftJoinOn("localized", "l1").on("KEY(l1)").eqExpression(":locale").end()
            .leftJoinDefault("localized", "l2");
        String expectedQuery = "SELECT l1.name, l2.name FROM Workflow workflow"
            + " LEFT JOIN workflow.localized l1 " + ON_CLAUSE + " KEY(l1) = :locale"
            + " LEFT JOIN workflow.localized l2"
            // TODO: remove when #45 or rather HHH-9329 has been fixed
            + " WHERE l1.description IS NOT NULL AND l1.name IS NOT NULL";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.setParameter("locale", Locale.GERMAN)
            .getResultList();
    }

    @Test
    public void testExcplicitMultipleJoinsWithParameterMatch() {
        CriteriaBuilder<Tuple> cb = cbf.from(em, Workflow.class)
            .leftJoinOn("localized", "l1").on("KEY(l1)").eqExpression(":locale").end()
            .leftJoinOn("localized", "l2").on("KEY(l2)").eqExpression("workflow.defaultLanguage").end()
            .select("localized[:locale].name")
            .select("localized[defaultLanguage].name");
        String expectedQuery = "SELECT l1.name, l2.name FROM Workflow workflow"
            + " LEFT JOIN workflow.localized l1 " + ON_CLAUSE + " KEY(l1) = :locale"
            + " LEFT JOIN workflow.localized l2 " + ON_CLAUSE + " KEY(l2) = workflow.defaultLanguage"
            // TODO: remove when #45 or rather HHH-9329 has been fixed
            + " WHERE l1.description IS NOT NULL AND l1.name IS NOT NULL AND l2.description IS NOT NULL AND l2.name IS NOT NULL";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.setParameter("locale", Locale.GERMAN)
            .getResultList();
    }

    @Test
    public void testMultipleNestedJoins() {
        CriteriaBuilder<Tuple> cb = cbf.from(em, Document.class)
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
            // TODO: not sure if the order is correct
            + " LEFT JOIN document.partners p2"
            + " LEFT JOIN p2.localized l21"
            + " LEFT JOIN p2.localized l22";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testMultipleNestedJoinsWithDefault() {
        CriteriaBuilder<Tuple> cb = cbf.from(em, Document.class)
            .leftJoin("partners", "p1")
            .leftJoin("partners.localized", "l")
            .where("p1.partnerDocument.name").eq("doc")
            .select("l");

        String expectedQuery = "SELECT " + joinAliasValue("l") + " FROM Document document"
            + " LEFT JOIN document.partners p1"
            + " LEFT JOIN p1.partnerDocument partnerDocument"
            // TODO: shouldn't we maybe consider to do the prefixing??
//            + " LEFT JOIN p1.partnerDocument p1_partnerDocument"
            // TODO: not sure if the order is correct
            + " LEFT JOIN document.partners partners"
            + " LEFT JOIN partners.localized l"
            + " WHERE partnerDocument.name = :param_0";
//            + " WHERE p1_partnerDocument.name = :param_0";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }
}
