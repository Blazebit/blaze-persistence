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

import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.Workflow;
import org.junit.experimental.categories.Category;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ElementCollectionTest extends AbstractCoreTest {
    
    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            Workflow.class
        };
    }
    
    @Test
    public void testElementCollectionSelect() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Workflow.class)
            .select("localized[:locale].name");
        String expectedQuery = "SELECT " + joinAliasValue("localized_locale_1", "name") + " FROM Workflow workflow"
            + " LEFT JOIN workflow.localized localized_locale_1"
            + onClause("KEY(localized_locale_1) = :locale");
        assertEquals(expectedQuery, cb.getQueryString());
        cb.setParameter("locale", Locale.GERMAN)
            .getResultList();
    }
    
    @Test
    public void testElementCollectionWhere() {
        CriteriaBuilder<Workflow> cb = cbf.create(em, Workflow.class)
            .where("localized[:locale].name").eq("bla");
        String expectedQuery = "SELECT workflow FROM Workflow workflow"
            + " LEFT JOIN workflow.localized localized_locale_1"
            + onClause("KEY(localized_locale_1) = :locale")
            + " WHERE " + joinAliasValue("localized_locale_1", "name") + " = :param_0";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.setParameter("locale", Locale.GERMAN)
            .getResultList();
    }
    
    @Test
    @Category(NoEclipselink.class)
    // Eclipselink does not support dereferencing of VALUE() functions
    public void testElementCollectionOrderBy() {
        CriteriaBuilder<Workflow> cb = cbf.create(em, Workflow.class)
            .orderByAsc("localized[:locale].name");
        String expectedQuery = "SELECT workflow FROM Workflow workflow"
            + " LEFT JOIN workflow.localized localized_locale_1"
            + onClause("KEY(localized_locale_1) = :locale")
            + " ORDER BY " + renderNullPrecedence(joinAliasValue("localized_locale_1", "name") + "", "ASC", "LAST");
        assertEquals(expectedQuery, cb.getQueryString());
        cb.setParameter("locale", Locale.GERMAN)
            .getResultList();
    }
}
