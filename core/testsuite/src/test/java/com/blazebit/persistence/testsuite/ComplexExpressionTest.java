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

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import javax.persistence.Tuple;

import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.Workflow;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@RunWith(Parameterized.class)
public class ComplexExpressionTest extends AbstractCoreTest {
    
    private final String caseExp1;
    private final String caseExp2;

    public ComplexExpressionTest(String caseExp1, String caseExp2, String additionalJoins) {
        this.caseExp1 = caseExp1;
        this.caseExp2 = caseExp2;
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            Workflow.class
        };
    }

    @Parameterized.Parameters
    public static Collection<?> expressionOperatorUses() {
        return Arrays.asList(new Object[][]{
// TODO: Basic ElementCollection seems broken in datanucleus: https://github.com/datanucleus/datanucleus-rdbms/issues/86
//            { "localized[:locale].name NOT MEMBER OF tags", staticJoinAliasValue("localized_locale_1", "name") + " NOT MEMBER OF workflow.tags", "" },
//            { "localized[:locale].name MEMBER OF tags", staticJoinAliasValue("localized_locale_1", "name") + " MEMBER OF workflow.tags", "" },
// TODO: IS EMPTY seems to be broken in hibernate for element collections. Also see https://hibernate.atlassian.net/browse/HHH-6686
//            { "localized[:locale] IS NOT EMPTY", "localized IS NOT EMPTY", "" },
//            { "localized[:locale] IS EMPTY", "localized IS EMPTY", "" },
            { "localized[:locale].name IS NOT NULL", staticJoinAliasValue("localized_locale_1", "name") + " IS NOT NULL", "" },
            { "localized[:locale].name IS NULL", staticJoinAliasValue("localized_locale_1", "name") + " IS NULL", "" },
            { "localized[:locale].name NOT LIKE '%a'", staticJoinAliasValue("localized_locale_1", "name") + " NOT LIKE '%a'", "" },
            { "localized[:locale].name LIKE '%a'", staticJoinAliasValue("localized_locale_1", "name") + " LIKE '%a'", "" },
            { "localized[:locale].name NOT IN ('a', 'b')", staticJoinAliasValue("localized_locale_1", "name") + " NOT IN ('a', 'b')", "" },
            { "localized[:locale].name IN ('a', 'b')", staticJoinAliasValue("localized_locale_1", "name") + " IN ('a', 'b')", "" },
            { "NOT(localized[:locale].name = localized[:locale].description)", staticJoinAliasValue("localized_locale_1", "name") + " <> " + staticJoinAliasValue("localized_locale_1", "description"), "" },
            { "localized[:locale].name <> localized[:locale].description", staticJoinAliasValue("localized_locale_1", "name") + " <> " + staticJoinAliasValue("localized_locale_1", "description"), "" },
            { "localized[:locale].name != localized[:locale].description", staticJoinAliasValue("localized_locale_1", "name") + " <> " + staticJoinAliasValue("localized_locale_1", "description"), "" },
            { "localized[:locale].name = localized[:locale].description", staticJoinAliasValue("localized_locale_1", "name") + " = " + staticJoinAliasValue("localized_locale_1", "description"), "" },
            { "LENGTH(localized[:locale].name) NOT BETWEEN 1 AND 5", "LENGTH(" + staticJoinAliasValue("localized_locale_1", "name") + ") NOT BETWEEN 1 AND 5", "" },
            { "LENGTH(localized[:locale].name) BETWEEN 1 AND 5", "LENGTH(" + staticJoinAliasValue("localized_locale_1", "name") + ") BETWEEN 1 AND 5", "" },
            { "-LENGTH(localized[:locale].name) = -LENGTH(localized[:locale].description)", "-LENGTH(" + staticJoinAliasValue("localized_locale_1", "name") + ") = -LENGTH(" + staticJoinAliasValue("localized_locale_1", "description") + ")", "" },
            { "LENGTH(localized[:locale].name) >= LENGTH(localized[:locale].description)", "LENGTH(" + staticJoinAliasValue("localized_locale_1", "name") + ") >= LENGTH(" + staticJoinAliasValue("localized_locale_1", "description") + ")", "" },
            { "LENGTH(localized[:locale].name) > LENGTH(localized[:locale].description)", "LENGTH(" + staticJoinAliasValue("localized_locale_1", "name") + ") > LENGTH(" + staticJoinAliasValue("localized_locale_1", "description") + ")", "" },
            { "LENGTH(localized[:locale].name) <= LENGTH(localized[:locale].description)", "LENGTH(" + staticJoinAliasValue("localized_locale_1", "name") + ") <= LENGTH(" + staticJoinAliasValue("localized_locale_1", "description") + ")", "" },
            { "LENGTH(localized[:locale].name) < LENGTH(localized[:locale].description)", "LENGTH(" + staticJoinAliasValue("localized_locale_1", "name") + ") < LENGTH(" + staticJoinAliasValue("localized_locale_1", "description") + ")", "" },
        });
    }

    @Test
    @Category(NoEclipselink.class)
    // Eclipselink does not support dereferencing of VALUE() functions
    public void testCaseWhenExpressionOperatorUsesInSelect() {
        // TODO: Report that EclipseLink has a bug in case when handling
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Workflow.class)
                .select("CASE WHEN " + caseExp1 + " THEN true ELSE false END");
        String expectedQuery = 
                "SELECT CASE WHEN " + caseExp2 + " THEN true ELSE false END"
                + " FROM Workflow workflow"
                + " LEFT JOIN workflow.localized localized_locale_1"
                + onClause("KEY(localized_locale_1) = :locale");
        
        assertEquals(expectedQuery, cb.getQueryString());
        cb.setParameter("locale", Locale.GERMAN)
            .getResultList();
    }
}
