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
import java.util.Locale;
import javax.persistence.Tuple;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class MultipleJoinComplexExpressionTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            Workflow.class
        };
    }

    @Test
    public void testCaseWhenBooleanExpressionSelect() {
        CriteriaBuilder<Tuple> cb = cbf.from(em, Workflow.class)
                .select("CASE WHEN localized[:locale].name IS NULL THEN localized[defaultLanguage] ELSE localized[:locale] END");
        String expectedQuery = "SELECT CASE WHEN localized_locale.name IS NULL THEN localized_workflow_defaultLanguage ELSE localized_locale END FROM Workflow workflow"
                + " LEFT JOIN workflow.localized localized_locale " + ON_CLAUSE + " KEY(localized_locale) = :locale"
                + " LEFT JOIN workflow.localized localized_workflow_defaultLanguage " + ON_CLAUSE + " KEY(localized_workflow_defaultLanguage) = workflow.defaultLanguage"
                // TODO: remove when #45 or rather HHH-9329 has been fixed
                + " WHERE localized_locale.description IS NOT NULL AND localized_locale.name IS NOT NULL"
                + " AND localized_workflow_defaultLanguage.description IS NOT NULL AND localized_workflow_defaultLanguage.name IS NOT NULL";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.setParameter("locale", Locale.GERMAN)
            .getResultList();
    }
    
    @Test
    public void testCaseWhenWithFunctionsInSelectAndLiterals() {
        CriteriaBuilder<Tuple> cb = cbf.from(em, Workflow.class)
                .select("SUBSTRING(COALESCE(CASE WHEN localized[:locale].name IS NULL THEN localized[defaultLanguage] ELSE localized[:locale] END,' - '),0,20)");
        String expectedQuery = 
                "SELECT SUBSTRING(COALESCE(CASE WHEN localized_locale.name IS NULL THEN localized_workflow_defaultLanguage ELSE localized_locale END,' - '),0,20)"
                + " FROM Workflow workflow"
                + " LEFT JOIN workflow.localized localized_locale " + ON_CLAUSE + " KEY(localized_locale) = :locale"
                + " LEFT JOIN workflow.localized localized_workflow_defaultLanguage " + ON_CLAUSE + " KEY(localized_workflow_defaultLanguage) = workflow.defaultLanguage"
                // TODO: remove when #45 or rather HHH-9329 has been fixed
                + " WHERE localized_locale.description IS NOT NULL AND localized_locale.name IS NOT NULL"
                + " AND localized_workflow_defaultLanguage.description IS NOT NULL AND localized_workflow_defaultLanguage.name IS NOT NULL";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.setParameter("locale", Locale.GERMAN)
            .getResultList();
    }
}
