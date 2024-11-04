/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class DateAddTest extends AbstractCoreTest {

    private Calendar c1;
    private Calendar addedDate;
    private Calendar lastModified;

    public DateAddTest() {
        c1 = Calendar.getInstance();
        c1.set(2000, 2, 3, 4, 5, 6);
        c1.set(Calendar.MILLISECOND, 7);
        c1.setMinimalDaysInFirstWeek(4);
        c1.setFirstDayOfWeek(Calendar.MONDAY);

        addedDate = Calendar.getInstance();
        addedDate.set(2000, 2, 3, 4, 5, 6);
        addedDate.set(Calendar.MILLISECOND, 7);
        addedDate.setMinimalDaysInFirstWeek(4);
        addedDate.setFirstDayOfWeek(Calendar.MONDAY);

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
    public void testDateAddDay() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Document.class, "doc")
            .select("FUNCTION('add_day', lastModified, 1)")
            .select("lastModified")
            ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());
        
        Tuple actual = list.get(0);
        addedDate.setTime((Date) actual.get(0));
        lastModified.setTime((Date) actual.get(1));

        assertEquals(lastModified.get(Calendar.YEAR), addedDate.get(Calendar.YEAR));
        assertEquals(lastModified.get(Calendar.MONTH), addedDate.get(Calendar.MONTH));
        assertNotEquals(lastModified.get(Calendar.DATE), addedDate.get(Calendar.DATE));
        assertEquals(lastModified.get(Calendar.HOUR), addedDate.get(Calendar.HOUR));
        assertEquals(lastModified.get(Calendar.MINUTE), addedDate.get(Calendar.MINUTE));
        assertEquals(lastModified.get(Calendar.SECOND), addedDate.get(Calendar.SECOND));
        assertEquals(lastModified.get(Calendar.MILLISECOND), addedDate.get(Calendar.MILLISECOND));
    }

    @Test
    public void testDateAddHour() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("FUNCTION('add_hour', lastModified, 1)")
                .select("lastModified")
                ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);
        addedDate.setTime((Date) actual.get(0));
        lastModified.setTime((Date) actual.get(1));

        assertEquals(lastModified.get(Calendar.YEAR), addedDate.get(Calendar.YEAR));
        assertEquals(lastModified.get(Calendar.MONTH), addedDate.get(Calendar.MONTH));
        assertEquals(lastModified.get(Calendar.DATE), addedDate.get(Calendar.DATE));
        assertNotEquals(lastModified.get(Calendar.HOUR), addedDate.get(Calendar.HOUR));
        assertEquals(lastModified.get(Calendar.MINUTE), addedDate.get(Calendar.MINUTE));
        assertEquals(lastModified.get(Calendar.SECOND), addedDate.get(Calendar.SECOND));
        assertEquals(lastModified.get(Calendar.MILLISECOND), addedDate.get(Calendar.MILLISECOND));
    }

    @Test
    public void testDateAddMicroseconds() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("FUNCTION('add_microseconds', lastModified, 1)")
                .select("lastModified")
                ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);
        addedDate.setTime((Date) actual.get(0));
        lastModified.setTime((Date) actual.get(1));

        assertEquals(lastModified.get(Calendar.YEAR), addedDate.get(Calendar.YEAR));
        assertEquals(lastModified.get(Calendar.MONTH), addedDate.get(Calendar.MONTH));
        assertEquals(lastModified.get(Calendar.DATE), addedDate.get(Calendar.DATE));
        assertEquals(lastModified.get(Calendar.HOUR), addedDate.get(Calendar.HOUR));
        assertEquals(lastModified.get(Calendar.MINUTE), addedDate.get(Calendar.MINUTE));
        assertEquals(lastModified.get(Calendar.SECOND), addedDate.get(Calendar.SECOND));
        assertEquals(lastModified.get(Calendar.MILLISECOND), addedDate.get(Calendar.MILLISECOND));
    }

    @Test
    public void testDateAddMilliseconds() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("FUNCTION('add_milliseconds', lastModified, 1)")
                .select("lastModified")
                ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);
        addedDate.setTime((Date) actual.get(0));
        lastModified.setTime((Date) actual.get(1));

        assertEquals(lastModified.get(Calendar.YEAR), addedDate.get(Calendar.YEAR));
        assertEquals(lastModified.get(Calendar.MONTH), addedDate.get(Calendar.MONTH));
        assertEquals(lastModified.get(Calendar.DATE), addedDate.get(Calendar.DATE));
        assertEquals(lastModified.get(Calendar.HOUR), addedDate.get(Calendar.HOUR));
        assertEquals(lastModified.get(Calendar.MINUTE), addedDate.get(Calendar.MINUTE));
        assertEquals(lastModified.get(Calendar.SECOND), addedDate.get(Calendar.SECOND));
        assertNotEquals(lastModified.get(Calendar.MILLISECOND), addedDate.get(Calendar.MILLISECOND));
    }

    @Test
    public void testDateAddMinute() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("FUNCTION('add_minute', lastModified, 1)")
                .select("lastModified")
                ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);
        addedDate.setTime((Date) actual.get(0));
        lastModified.setTime((Date) actual.get(1));

        assertEquals(lastModified.get(Calendar.YEAR), addedDate.get(Calendar.YEAR));
        assertEquals(lastModified.get(Calendar.MONTH), addedDate.get(Calendar.MONTH));
        assertEquals(lastModified.get(Calendar.DATE), addedDate.get(Calendar.DATE));
        assertEquals(lastModified.get(Calendar.HOUR), addedDate.get(Calendar.HOUR));
        assertNotEquals(lastModified.get(Calendar.MINUTE), addedDate.get(Calendar.MINUTE));
        assertEquals(lastModified.get(Calendar.SECOND), addedDate.get(Calendar.SECOND));
        assertEquals(lastModified.get(Calendar.MILLISECOND), addedDate.get(Calendar.MILLISECOND));
    }

    @Test
    public void testDateAddMonth() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("FUNCTION('add_month', lastModified, 1)")
                .select("lastModified")
                ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);
        addedDate.setTime((Date) actual.get(0));
        lastModified.setTime((Date) actual.get(1));

        assertEquals(lastModified.get(Calendar.YEAR), addedDate.get(Calendar.YEAR));
        assertNotEquals(lastModified.get(Calendar.MONTH), addedDate.get(Calendar.MONTH));
        assertEquals(lastModified.get(Calendar.DATE), addedDate.get(Calendar.DATE));
        assertEquals(lastModified.get(Calendar.HOUR), addedDate.get(Calendar.HOUR));
        assertEquals(lastModified.get(Calendar.MINUTE), addedDate.get(Calendar.MINUTE));
        assertEquals(lastModified.get(Calendar.SECOND), addedDate.get(Calendar.SECOND));
        assertEquals(lastModified.get(Calendar.MILLISECOND), addedDate.get(Calendar.MILLISECOND));
    }

    @Test
    public void testDateAddQuarter() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("FUNCTION('add_quarter', lastModified, 1)")
                .select("lastModified")
                ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);
        addedDate.setTime((Date) actual.get(0));
        lastModified.setTime((Date) actual.get(1));

        assertEquals(lastModified.get(Calendar.YEAR), addedDate.get(Calendar.YEAR));
        assertNotEquals(lastModified.get(Calendar.MONTH), addedDate.get(Calendar.MONTH));
        assertEquals(lastModified.get(Calendar.DATE), addedDate.get(Calendar.DATE));
        assertEquals(lastModified.get(Calendar.HOUR), addedDate.get(Calendar.HOUR));
        assertEquals(lastModified.get(Calendar.MINUTE), addedDate.get(Calendar.MINUTE));
        assertEquals(lastModified.get(Calendar.SECOND), addedDate.get(Calendar.SECOND));
        assertEquals(lastModified.get(Calendar.MILLISECOND), addedDate.get(Calendar.MILLISECOND));
    }

    @Test
    public void testDateAddSecond() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("FUNCTION('add_second', lastModified, 1)")
                .select("lastModified")
                ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);
        addedDate.setTime((Date) actual.get(0));
        lastModified.setTime((Date) actual.get(1));

        assertEquals(lastModified.get(Calendar.YEAR), addedDate.get(Calendar.YEAR));
        assertEquals(lastModified.get(Calendar.MONTH), addedDate.get(Calendar.MONTH));
        assertEquals(lastModified.get(Calendar.DATE), addedDate.get(Calendar.DATE));
        assertEquals(lastModified.get(Calendar.HOUR), addedDate.get(Calendar.HOUR));
        assertEquals(lastModified.get(Calendar.MINUTE), addedDate.get(Calendar.MINUTE));
        assertNotEquals(lastModified.get(Calendar.SECOND), addedDate.get(Calendar.SECOND));
        assertEquals(lastModified.get(Calendar.MILLISECOND), addedDate.get(Calendar.MILLISECOND));
    }

    @Test
    public void testDateAddWeek() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("FUNCTION('add_week', lastModified, 1)")
                .select("lastModified")
                ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);
        addedDate.setTime((Date) actual.get(0));
        lastModified.setTime((Date) actual.get(1));

        assertEquals(lastModified.get(Calendar.YEAR), addedDate.get(Calendar.YEAR));
        assertEquals(lastModified.get(Calendar.MONTH), addedDate.get(Calendar.MONTH));
        assertNotEquals(lastModified.get(Calendar.DATE), addedDate.get(Calendar.DATE));
        assertEquals(lastModified.get(Calendar.HOUR), addedDate.get(Calendar.HOUR));
        assertEquals(lastModified.get(Calendar.MINUTE), addedDate.get(Calendar.MINUTE));
        assertEquals(lastModified.get(Calendar.SECOND), addedDate.get(Calendar.SECOND));
        assertEquals(lastModified.get(Calendar.MILLISECOND), addedDate.get(Calendar.MILLISECOND));
    }

    @Test
    public void testDateAddYear() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("FUNCTION('add_year', lastModified, 1)")
                .select("lastModified")
                ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);
        addedDate.setTime((Date) actual.get(0));
        lastModified.setTime((Date) actual.get(1));

        assertNotEquals(lastModified.get(Calendar.YEAR), addedDate.get(Calendar.YEAR));
        assertEquals(lastModified.get(Calendar.MONTH), addedDate.get(Calendar.MONTH));
        assertEquals(lastModified.get(Calendar.DATE), addedDate.get(Calendar.DATE));
        assertEquals(lastModified.get(Calendar.HOUR), addedDate.get(Calendar.HOUR));
        assertEquals(lastModified.get(Calendar.MINUTE), addedDate.get(Calendar.MINUTE));
        assertEquals(lastModified.get(Calendar.SECOND), addedDate.get(Calendar.SECOND));
        assertEquals(lastModified.get(Calendar.MILLISECOND), addedDate.get(Calendar.MILLISECOND));
    }


}
