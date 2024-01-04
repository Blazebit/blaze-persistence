/*
 * Copyright 2014 - 2024 Blazebit.
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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.parser.util.TypeUtils;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 */
public class TemporalLiteralRenderingTest extends AbstractCoreTest {

    @Test
    public void testTemporalLiteralEmulation() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2000, Calendar.JANUARY, 1, 12, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class, "d")
                .select("COUNT(*)")
                .selectCase()
                    .when("d.creationDate").ltExpression("CURRENT_TIMESTAMP")
                    .thenExpression("d.creationDate")
                    .otherwise(calendar);
        String expected = "SELECT " + countStar() + ", " +
                "CASE WHEN d.creationDate < CURRENT_TIMESTAMP THEN d.creationDate ELSE " + tsLiteral(calendar) + " END " +
                "FROM Document d GROUP BY " +
                "CASE WHEN d.creationDate < CURRENT_TIMESTAMP THEN d.creationDate ELSE " + tsLiteral(calendar) + " END";
        assertEquals(expected, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    public void testJava8TemporalLiteralEmulation() {
        LocalDateTime localDateTime = LocalDateTime.of(2000, 1, 1, 12, 0, 0);
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class, "d")
                .select("1")
                .where("d.creationDate").ltLiteral(localDateTime);
        String expected = "SELECT 1 " +
                "FROM Document d " +
                "WHERE d.creationDate < " + tsLiteral(localDateTime);
        assertEquals(expected, cb.getQueryString());
        cb.getResultList();
    }

}
