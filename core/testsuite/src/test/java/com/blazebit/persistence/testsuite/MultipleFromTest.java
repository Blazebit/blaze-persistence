/*
 * Copyright 2014 - 2022 Blazebit.
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
import com.blazebit.persistence.testsuite.entity.Person;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public class MultipleFromTest extends AbstractCoreTest {

    @Test
    public void testMultipleFrom() {
        CriteriaBuilder<Long> criteria = cbf.create(em, Long.class)
                .from(Document.class, "d")
                .from(Person.class, "p");
        criteria.where("d.owner").eqExpression("p");
        criteria.select("COUNT(*)");
        assertEquals("SELECT " + countStar() + " FROM Document d, Person p WHERE d.owner = p", criteria.getQueryString());
        criteria.getResultList();
    }
}
