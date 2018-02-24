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

import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.entity.Document;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class DistinctTest extends AbstractCoreTest {

    @Test
    public void testDistinct() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.select("d.partners.name").distinct();

        assertEquals("SELECT DISTINCT partners_1.name FROM Document d LEFT JOIN d.partners partners_1", criteria.getQueryString());
    }

    @Test
    public void testDistinctWithoutSelect() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.distinct();
        assertEquals("SELECT DISTINCT d FROM Document d", criteria.getQueryString());
    }

}
