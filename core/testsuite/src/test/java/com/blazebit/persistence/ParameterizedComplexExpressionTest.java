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

import com.blazebit.persistence.entity.Workflow;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import javax.persistence.Tuple;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
@RunWith(Parameterized.class)
public class ParameterizedComplexExpressionTest extends AbstractCoreTest {
    
    private final String caseExp1;
    private final String caseExp2;

    public ParameterizedComplexExpressionTest(String caseExp1, String caseExp2) {
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
    public static Collection expressionOperatorUses() {
        return Arrays.asList(new Object[][]{
            { "KEY(localized[:locale]) IS NOT MEMBER OF supportedLocales", "KEY(localized) IS NOT MEMBER OF supportedLocales" },
            { "KEY(localized[:locale]) IS MEMBER OF supportedLocales", "KEY(localized) IS MEMBER OF supportedLocales" },
            { "localized[:locale] IS NOT EMPTY", "localized IS NOT EMPTY" },
            { "localized[:locale] IS EMPTY", "localized IS EMPTY" },
            { "localized[:locale].name IS NOT NULL", "localized.name IS NOT NULL" },
            { "localized[:locale].name IS NULL", "localized.name IS NULL" },
            { "localized[:locale].name NOT LIKE '%a'", "localized.name NOT LIKE '%a'" },
            { "localized[:locale].name LIKE '%a'", "localized.name LIKE '%a'" },
            { "localized[:locale].name NOT IN ('a', 'b')", "localized.name NOT IN ('a', 'b')" },
            { "localized[:locale].name IN ('a', 'b')", "localized.name IN ('a', 'b')" },
            { "NOT(localized[:locale].name = localized[:locale].description)", "NOT(localized.name = localized.description)" },
            { "localized[:locale].name <> localized[:locale].description", "localized.name <> localized.description" },
            { "localized[:locale].name != localized[:locale].description", "localized.name != localized.description" },
            { "localized[:locale].name = localized[:locale].description", "localized.name = localized.description" },
            { "LENGTH(localized[:locale].name) NOT BETWEEN 1 AND 5", "LENGTH(localized.name) NOT BETWEEN 1 AND 5" },
            { "LENGTH(localized[:locale].name) BETWEEN 1 AND 5", "LENGTH(localized.name) BETWEEN 1 AND 5" },
            { "-LENGTH(localized[:locale].name) = -LENGTH(localized[:locale].description)", "-LENGTH(localized.name) = -LENGTH(localized.description)" },
            { "+LENGTH(localized[:locale].name) = +LENGTH(localized[:locale].description)", "+LENGTH(localized.name) = +LENGTH(localized.description)" },
            { "LENGTH(localized[:locale].name) >= LENGTH(localized[:locale].description)", "LENGTH(localized.name) >= LENGTH(localized.description)" },
            { "LENGTH(localized[:locale].name) > LENGTH(localized[:locale].description)", "LENGTH(localized.name) > LENGTH(localized.description)" },
            { "LENGTH(localized[:locale].name) <= LENGTH(localized[:locale].description)", "LENGTH(localized.name) <= LENGTH(localized.description)" },
            { "LENGTH(localized[:locale].name) < LENGTH(localized[:locale].description)", "LENGTH(localized.name) < LENGTH(localized.description)" },
        });
    }

    @Test
    public void testCaseWhenExpressionOperatorUsesInSelect() {
        CriteriaBuilder<Tuple> cb = cbf.from(em, Workflow.class)
                .select("CASE WHEN " + caseExp1 + " THEN true ELSE false END");
        String expectedQuery = 
                "SELECT CASE WHEN " + caseExp2 + " THEN true ELSE false END"
                + " FROM Workflow workflow"
                + " LEFT JOIN workflow.localized localized " + ON_CLAUSE + " KEY(localized) = :locale";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.setParameter("locale", Locale.GERMAN)
            .getResultList();
    }
}
