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
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class EmbeddableTest extends AbstractCoreTest {
    
    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            Workflow.class
        };
    }
    
    @Test
    public void testEmbeddableSelect() {
        CriteriaBuilder<Tuple> cb = cbf.from(em, Workflow.class)
            .select("localized[:locale].name");
        String expectedQuery = "SELECT localized.name FROM Workflow workflow LEFT JOIN workflow.localized localized " + ON_CLAUSE + " KEY(localized) = :locale";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.setParameter("locale", Locale.GERMAN)
            .getResultList();
    }
    
    @Test
    public void testEmbeddableWhere() {
        CriteriaBuilder<Workflow> cb = cbf.from(em, Workflow.class)
            .where("localized[:locale].name").eq("bla");
        String expectedQuery = "SELECT workflow FROM Workflow workflow LEFT JOIN workflow.localized localized " + ON_CLAUSE + " KEY(localized) = :locale WHERE localized.name = :param_0";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.setParameter("locale", Locale.GERMAN)
            .getResultList();
    }
    
    @Test
    public void testEmbeddableOrderBy() {
        CriteriaBuilder<Workflow> cb = cbf.from(em, Workflow.class)
            .orderByAsc("localized[:locale].name");
        String expectedQuery = "SELECT workflow FROM Workflow workflow LEFT JOIN workflow.localized localized " + ON_CLAUSE + " KEY(localized) = :locale ORDER BY localized.name ASC NULLS LAST";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.setParameter("locale", Locale.GERMAN)
            .getResultList();
    }
}
