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


import com.blazebit.persistence.DeleteCriteriaBuilder;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Document_;
import org.junit.Test;

import javax.persistence.criteria.Root;

import static org.junit.Assert.assertEquals;

public class DeleteTest extends AbstractCoreTest {

    @Test
    public void simpleWhere() {
        BlazeCriteriaBuilder cb = BlazeCriteria.get(cbf);
        BlazeCriteriaDelete<Document> query = cb.createCriteriaDelete(Document.class, "d");
        Root<Document> root = query.getRoot();

        query.where(cb.equal(root.get(Document_.name), "abc"));
        DeleteCriteriaBuilder<Document> criteriaBuilder = query.createCriteriaBuilder(em);
        assertEquals("DELETE FROM Document d WHERE d.name = :generated_param_0", criteriaBuilder.getQueryString());
        criteriaBuilder.getQuery();
    }
}
