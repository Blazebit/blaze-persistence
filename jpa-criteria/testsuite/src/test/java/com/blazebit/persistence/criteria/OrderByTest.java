/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria;

import static org.junit.Assert.assertEquals;

import jakarta.persistence.criteria.Root;

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
