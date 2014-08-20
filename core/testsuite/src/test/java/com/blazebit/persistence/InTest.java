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

import com.blazebit.persistence.entity.Document;
import static com.googlecode.catchexception.CatchException.verifyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class InTest extends AbstractCoreTest {

    @Test
    public void testIn() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        List<Long> ages = new ArrayList<Long>(Arrays.asList(new Long[]{ 1L, 2L, 3L, 4L, 5L }));
        criteria.where("d.age").in(ages);

        assertEquals("SELECT d FROM Document d WHERE d.age IN :param_0", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testInNull() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        verifyException(criteria.where("d.age"), NullPointerException.class).in((List<?>) null);
    }

    @Test
    public void testNotIn() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        List<Long> ages = new ArrayList<Long>(Arrays.asList(new Long[]{ 1L, 2L, 3L, 4L, 5L }));
        criteria.where("d.age").notIn(ages);
        assertEquals("SELECT d FROM Document d WHERE d.age NOT IN :param_0", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testNotInNull() {
        CriteriaBuilder<Document> criteria = cbf.from(em, Document.class, "d");
        verifyException(criteria.where("d.age"), NullPointerException.class).notIn((List<?>) null);
    }

}
