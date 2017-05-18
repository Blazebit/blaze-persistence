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

package com.blazebit.persistence.criteria;

import static org.junit.Assert.assertEquals;

import javax.persistence.criteria.Root;

import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Document_;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class OrderByTest extends AbstractCoreTest {

    @Test
    public void simpleDescAscOrderBy() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");
        BlazeSubquery<Long> subquery = cq.subquery(Long.class);
        Root<Document> subRoot = subquery.from(Document.class, "sub");
        subquery.select(subRoot.get(Document_.id));
        subquery.orderBy(
                cb.desc(subRoot.get(Document_.creationDate)),
                cb.asc(subRoot.get(Document_.id)),
                cb.desc(subRoot.get(Document_.creationDate), true),
                cb.asc(subRoot.get(Document_.id), true)
        );

        cq.select(subquery);
        cq.orderBy(
            cb.desc(root.get(Document_.creationDate)),
            cb.asc(root.get(Document_.id)),
            cb.desc(root.get(Document_.creationDate), true),
            cb.asc(root.get(Document_.id), true)
        );
        
        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT (" +
                        "SELECT sub.id FROM Document sub ORDER BY " +
                        renderNullPrecedence("sub.creationDate", "DESC", "LAST") + ", " +
                        "sub.id ASC, " +
                        renderNullPrecedence("sub.creationDate", "DESC", "FIRST") + ", " +
                        "sub.id ASC" +
                    ") FROM Document document ORDER BY " +
                    renderNullPrecedence("document.creationDate", "DESC", "LAST") + ", " +
                    "document.id ASC, " +
                    renderNullPrecedence("document.creationDate", "DESC", "FIRST") + ", " +
                    "document.id ASC",
                criteriaBuilder.getQueryString());
    }
    
}
