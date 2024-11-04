/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria;

import static org.junit.Assert.assertEquals;

import jakarta.persistence.Tuple;
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
public class GroupByTest extends AbstractCoreTest {

    @Test
    public void simpleGroupBy() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");
        
        cq.select(cb.count(root.get(Document_.id)));
        cq.groupBy(root.get(Document_.age));
        
        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT COUNT(document.id) FROM Document document GROUP BY document.age", criteriaBuilder.getQueryString());
    }
    
    @Test
    public void groupByWithHaving() {
        BlazeCriteriaQuery<Tuple> cq = BlazeCriteria.get(cbf, Tuple.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");
        
        cq.multiselect(root.get(Document_.age), cb.count(root.get(Document_.id)));
        cq.groupBy(root.get(Document_.age));
        cq.having(cb.gt(cb.count(root.get(Document_.id)), 1));
        
        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT document.age, COUNT(document.id) FROM Document document GROUP BY document.age HAVING COUNT(document.id) > :generated_param_0", criteriaBuilder.getQueryString());
    }
    
}
