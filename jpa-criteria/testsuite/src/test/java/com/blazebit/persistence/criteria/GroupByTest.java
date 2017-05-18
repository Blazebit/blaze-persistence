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

import javax.persistence.Tuple;
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
        assertEquals("SELECT document.age, COUNT(document.id) FROM Document document GROUP BY document.age HAVING COUNT(document.id) > 1L", criteriaBuilder.getQueryString());
    }
    
}
