/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
