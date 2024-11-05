/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoH2;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class DateTruncTest extends AbstractCoreTest {

    private Calendar c1;
    private Calendar truncatedDate;
    private Calendar lastModified;

    public DateTruncTest() {
        c1 = Calendar.getInstance();
        c1.set(2000, 2, 3, 4, 5, 6);
        c1.set(Calendar.MILLISECOND, 7);
        c1.setMinimalDaysInFirstWeek(4);
        c1.setFirstDayOfWeek(Calendar.MONDAY);

        truncatedDate = Calendar.getInstance();
        truncatedDate.set(2000, 2, 3, 4, 5, 6);
        truncatedDate.set(Calendar.MILLISECOND, 7);
        truncatedDate.setMinimalDaysInFirstWeek(4);
        truncatedDate.setFirstDayOfWeek(Calendar.MONDAY);

        lastModified = Calendar.getInstance();
        lastModified.set(2000, 2, 3, 4, 5, 6);
        lastModified.set(Calendar.MILLISECOND, 7);
        lastModified.setMinimalDaysInFirstWeek(4);
        lastModified.setFirstDayOfWeek(Calendar.MONDAY);
    }

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Person p = new Person("Pers1");
                p.setAge(20L);
                em.persist(p);

                Version v1 = new Version();
                em.persist(v1);

                Document doc1 = new Document("Doc1", p, v1);

                doc1.setCreationDate(c1);
                doc1.setCreationDate2(c1);

                doc1.setLastModified(c1.getTime());
                doc1.setLastModified2(c1.getTime());

                em.persist(doc1);
            }
        });
    }

    @Test
    public void testDateTruncDay() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Document.class, "doc")
            .select("FUNCTION('trunc_day', lastModified)")
            .select("lastModified")
            ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());
        
        Tuple actual = list.get(0);
        truncatedDate.setTime((Date) actual.get(0));
        lastModified.setTime((Date) actual.get(1));

        assertNotEquals(0, lastModified.get(Calendar.HOUR));
        assertEquals(0, truncatedDate.get(Calendar.HOUR));
        assertEquals(0, truncatedDate.get(Calendar.MINUTE));
        assertEquals(0, truncatedDate.get(Calendar.SECOND));
        assertEquals(0, truncatedDate.get(Calendar.MILLISECOND));
    }

    @Test
    public void testDateTruncHour() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Document.class, "doc")
            .select("FUNCTION('trunc_hour', lastModified)")
            .select("lastModified")
            ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);
        truncatedDate.setTime((Date) actual.get(0));
        lastModified.setTime((Date) actual.get(1));

        assertNotEquals(0, lastModified.get(Calendar.MINUTE));
        assertEquals(0, truncatedDate.get(Calendar.MINUTE));
        assertEquals(0, truncatedDate.get(Calendar.SECOND));
        assertEquals(0, truncatedDate.get(Calendar.MILLISECOND));
    }

    @Test
    public void testDateTruncMicroseconds() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Document.class, "doc")
            .select("FUNCTION('trunc_microseconds', lastModified)")
            .select("lastModified")
            ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);

        assertNotNull(actual.get(0));
    }

    @Test
    public void testDateTruncMilliseconds() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Document.class, "doc")
            .select("FUNCTION('trunc_milliseconds', lastModified)")
            .select("lastModified")
            ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);

        assertNotNull(actual.get(0));
    }

    @Test
    public void testDateTruncMinute() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Document.class, "doc")
            .select("FUNCTION('trunc_minute', lastModified)")
            .select("lastModified")
            ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);
        truncatedDate.setTime((Date) actual.get(0));
        lastModified.setTime((Date) actual.get(1));

        assertNotEquals(0, lastModified.get(Calendar.SECOND));
        assertEquals(0, truncatedDate.get(Calendar.SECOND));
        assertEquals(0, truncatedDate.get(Calendar.MILLISECOND));
    }

    @Test
    public void testDateTruncMonth() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Document.class, "doc")
            .select("FUNCTION('trunc_month', lastModified)")
            .select("lastModified")
            ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);
        truncatedDate.setTime((Date) actual.get(0));
        lastModified.setTime((Date) actual.get(1));

        assertNotEquals(1, lastModified.get(Calendar.DAY_OF_MONTH));
        assertEquals(1, truncatedDate.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, truncatedDate.get(Calendar.HOUR));
        assertEquals(0, truncatedDate.get(Calendar.MINUTE));
        assertEquals(0, truncatedDate.get(Calendar.SECOND));
        assertEquals(0, truncatedDate.get(Calendar.MILLISECOND));
    }

    @Test
    public void testDateTruncQuarter() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Document.class, "doc")
            .select("FUNCTION('trunc_quarter', lastModified)")
            .select("lastModified")
            ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);
        truncatedDate.setTime((Date) actual.get(0));
        lastModified.setTime((Date) actual.get(1));

        assertNotEquals(0, lastModified.get(Calendar.HOUR));
        assertEquals(0, truncatedDate.get(Calendar.HOUR));
        assertEquals(0, truncatedDate.get(Calendar.MINUTE));
        assertEquals(0, truncatedDate.get(Calendar.SECOND));
        assertEquals(0, truncatedDate.get(Calendar.MILLISECOND));
    }

    @Test
    public void testDateTruncSecond() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Document.class, "doc")
            .select("FUNCTION('trunc_second', lastModified)")
            .select("lastModified")
            ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);
        truncatedDate.setTime((Date) actual.get(0));
        lastModified.setTime((Date) actual.get(1));

        assertNotEquals(0, lastModified.get(Calendar.MILLISECOND));
        assertEquals(0, truncatedDate.get(Calendar.MILLISECOND));
    }

    // NOTE: H2 2.0 changed the meaning of "week" to be locale sensitive: https://github.com/h2database/h2database/issues/3922
    @Test
    @Category({ NoH2.class })
    public void testDateTruncWeek() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Document.class, "doc")
            .select("FUNCTION('trunc_week', lastModified)")
            .select("lastModified")
            ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);
        
        truncatedDate.setTime((Date) actual.get(0));
        lastModified.setTime((Date) actual.get(1));

        assertNotEquals(Calendar.MONDAY, lastModified.get(Calendar.DAY_OF_WEEK));
        assertEquals(Calendar.MONDAY, truncatedDate.get(Calendar.DAY_OF_WEEK));
        assertEquals(0, truncatedDate.get(Calendar.HOUR));
        assertEquals(0, truncatedDate.get(Calendar.MINUTE));
        assertEquals(0, truncatedDate.get(Calendar.SECOND));
        assertEquals(0, truncatedDate.get(Calendar.MILLISECOND));
    }

    @Test
    public void testDateTruncYear() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Document.class, "doc")
            .select("FUNCTION('trunc_year', lastModified)")
            .select("lastModified")
            ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);
        
        truncatedDate.setTime((Date) actual.get(0));
        lastModified.setTime((Date) actual.get(1));

        assertNotEquals(1, lastModified.get(Calendar.MONTH) + 1);
        assertEquals(1, truncatedDate.get(Calendar.MONTH) + 1);
        assertEquals(1, truncatedDate.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, truncatedDate.get(Calendar.HOUR));
        assertEquals(0, truncatedDate.get(Calendar.MINUTE));
        assertEquals(0, truncatedDate.get(Calendar.SECOND));
        assertEquals(0, truncatedDate.get(Calendar.MILLISECOND));
    }

}
