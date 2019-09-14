/*
 * Copyright 2014 - 2019 Blazebit.
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TemporalType;
import javax.persistence.Tuple;
import javax.sql.DataSource;

import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.1.0
 */
public class DateDiffTest extends AbstractCoreTest {

    private static TimeZone timeZone;

    private Calendar c1;
    private Calendar c2;
    private Calendar l1;
    private Calendar l2;

    // Thanks to the MySQL driver not being able to handle timezones correctly, we must run this in the UTC timezone...

    @Override
    protected boolean recreateDataSource() {
        // Some drivers have timezone information bound to the connection
        // So we have to recreate the data source to get the newly configured time zones for connections
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }
        return true;
    }

    @Override
    protected DataSource createDataSource(Map<Object, Object> properties) {
        // Set the producer timezone
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        resetTimeZoneCaches();

        return super.createDataSource(properties);
    }

    @AfterClass
    public static void after() {
        TimeZone.setDefault(timeZone);
        resetTimeZoneCaches();
    }

    @Before
    public void setup() {
        c1 = Calendar.getInstance();
        c1.set(2000, 0, 1, 0, 0, 0);
        c1.set(Calendar.MILLISECOND, 0);

        c2 = Calendar.getInstance();
        c2.set(2001, 1, 2, 0, 0, 0);
        c2.set(Calendar.MILLISECOND, 0);

        l1 = Calendar.getInstance();
        l1.set(2002, 0, 1, 0, 0, 0);
        l1.set(Calendar.MILLISECOND, 543);

        l2 = Calendar.getInstance();
        l2.set(2003, 1, 2, 1, 1, 1);
        l2.set(Calendar.MILLISECOND, 40);
    }

    @Override
    public void setUpOnce() {
        cleanDatabase();
        setup();
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
                doc1.setCreationDate2(c2);

                doc1.setLastModified(l1.getTime());
                doc1.setLastModified2(l2.getTime());

                em.persist(doc1);
            }
        });
    }

    @Test
    public void testDateSelfDiff() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Document.class, "doc")
            .select("FUNCTION('YEAR_DIFF',          creationDate, creationDate)")
            .select("FUNCTION('MONTH_DIFF',         creationDate, creationDate)")
            .select("FUNCTION('DAY_DIFF',           creationDate, creationDate)")
            .select("FUNCTION('HOUR_DIFF',          creationDate, creationDate)")
            .select("FUNCTION('MINUTE_DIFF',        creationDate, creationDate)")
            .select("FUNCTION('SECOND_DIFF',        creationDate, creationDate)")
            .select("FUNCTION('MILLISECOND_DIFF',   creationDate, creationDate)")
            .select("FUNCTION('EPOCH_DIFF',         creationDate, creationDate)")
            .select("FUNCTION('MICROSECOND_DIFF',   creationDate, creationDate)")
            .select("FUNCTION('WEEK_DIFF',   creationDate, creationDate)")
            .select("FUNCTION('QUARTER_DIFF',   creationDate, creationDate)")
            ;
        
        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());
        
        Tuple actual = list.get(0);

        assertEquals(0, actual.get(0));
        assertEquals(0, actual.get(1));
        assertEquals(0, actual.get(2));
        assertEquals(0, actual.get(3));
        assertEquals(0, actual.get(4));
        assertEquals(0, actual.get(5));
        assertEquals(0L, actual.get(6));
        assertEquals(0, actual.get(7));
        assertEquals(0L, actual.get(8));
        assertEquals(0, actual.get(9));
    }
    
    @Test
    public void testDateWithDateDiff() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Document.class, "doc")
            .select("FUNCTION('YEAR_DIFF',          creationDate,  creationDate2)")
            .select("FUNCTION('MONTH_DIFF',         creationDate,  creationDate2)")
            .select("FUNCTION('DAY_DIFF',           creationDate,  creationDate2)")
            .select("FUNCTION('HOUR_DIFF',          creationDate,  creationDate2)")
            .select("FUNCTION('MINUTE_DIFF',        creationDate,  creationDate2)")
            .select("FUNCTION('SECOND_DIFF',        creationDate,  creationDate2)")
            .select("FUNCTION('MILLISECOND_DIFF',   creationDate,  creationDate2)")
            .select("FUNCTION('YEAR_DIFF',          creationDate2, creationDate)")
            .select("FUNCTION('MONTH_DIFF',         creationDate2, creationDate)")
            .select("FUNCTION('DAY_DIFF',           creationDate2, creationDate)")
            .select("FUNCTION('HOUR_DIFF',          creationDate2, creationDate)")
            .select("FUNCTION('MINUTE_DIFF',        creationDate2, creationDate)")
            .select("FUNCTION('SECOND_DIFF',        creationDate2, creationDate)")
            .select("FUNCTION('MILLISECOND_DIFF',   creationDate2, creationDate)")
            .select("FUNCTION('EPOCH_DIFF',         creationDate2, creationDate)")
            .select("FUNCTION('MICROSECOND_DIFF',   creationDate2, creationDate)")
            .select("FUNCTION('WEEK_DIFF',   creationDate2, creationDate)")
            .select("FUNCTION('QUARTER_DIFF',   creationDate2, creationDate)")
            ;
        
        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());
        
        Tuple actual = list.get(0);

        assertEquals(yearsBetween  (c1, c2), actual.get(0));
        assertEquals(monthsBetween (c1, c2), actual.get(1));
        assertEquals(daysBetween   (c1, c2), actual.get(2));
        assertEquals(hoursBetween  (c1, c2), actual.get(3));
        assertEquals(minutesBetween(c1, c2), actual.get(4));
        assertEquals(secondsBetween(c1, c2), actual.get(5));
        assertEquals(millisecondsBetween(c1, c2), actual.get(6));

        assertEquals(yearsBetween  (c2, c1), actual.get(7));
        assertEquals(monthsBetween (c2, c1), actual.get(8));
        assertEquals(daysBetween   (c2, c1), actual.get(9));
        assertEquals(hoursBetween  (c2, c1), actual.get(10));
        assertEquals(minutesBetween(c2, c1), actual.get(11));
        assertEquals(secondsBetween(c2, c1), actual.get(12));
        assertEquals(millisecondsBetween(c2, c1), actual.get(13));
        assertEquals(secondsBetween(c2, c1), actual.get(14));
        assertEquals(microsecondsBetween(c2, c1), actual.get(15));
        assertEquals(weeksBetween(c2, c1), actual.get(16));
        assertEquals(quartersBetween(c2, c1), actual.get(17));
    }
    
    @Test
    public void testTimestampWithTimestampDiff() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Document.class, "doc")
            .select("FUNCTION('YEAR_DIFF',   lastModified,  lastModified2)")
            .select("FUNCTION('MONTH_DIFF',  lastModified,  lastModified2)")
            .select("FUNCTION('DAY_DIFF',    lastModified,  lastModified2)")
            .select("FUNCTION('HOUR_DIFF',   lastModified,  lastModified2)")
            .select("FUNCTION('MINUTE_DIFF', lastModified,  lastModified2)")
            .select("FUNCTION('SECOND_DIFF', lastModified,  lastModified2)")
            .select("FUNCTION('MILLISECOND_DIFF', lastModified, lastModified2)")
            .select("FUNCTION('YEAR_DIFF',   lastModified2, lastModified)")
            .select("FUNCTION('MONTH_DIFF',  lastModified2, lastModified)")
            .select("FUNCTION('DAY_DIFF',    lastModified2, lastModified)")
            .select("FUNCTION('HOUR_DIFF',   lastModified2, lastModified)")
            .select("FUNCTION('MINUTE_DIFF', lastModified2, lastModified)")
            .select("FUNCTION('SECOND_DIFF', lastModified2, lastModified)")
            .select("FUNCTION('MILLISECOND_DIFF', lastModified2, lastModified)")
            .select("FUNCTION('EPOCH_DIFF', lastModified2, lastModified)")
            .select("FUNCTION('MICROSECOND_DIFF', lastModified2, lastModified)")
            .select("FUNCTION('WEEK_DIFF', lastModified2, lastModified)")
            .select("FUNCTION('QUARTER_DIFF', lastModified2, lastModified)")
            ;
        
        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());
        
        Tuple actual = list.get(0);

        assertEquals(yearsBetween  (l1, l2), actual.get(0));
        assertEquals(monthsBetween (l1, l2), actual.get(1));
        assertEquals(daysBetween   (l1, l2), actual.get(2));
        assertEquals(hoursBetween  (l1, l2), actual.get(3));
        assertEquals(minutesBetween(l1, l2), actual.get(4));
        assertEquals(secondsBetween(l1, l2), actual.get(5));
        assertEquals(millisecondsBetween(l1, l2), actual.get(6));

        assertEquals(yearsBetween  (l2, l1), actual.get(7));
        assertEquals(monthsBetween (l2, l1), actual.get(8));
        assertEquals(daysBetween   (l2, l1), actual.get(9));
        assertEquals(hoursBetween  (l2, l1), actual.get(10));
        assertEquals(minutesBetween(l2, l1), actual.get(11));
        assertEquals(secondsBetween(l2, l1), actual.get(12));
        assertEquals(millisecondsBetween(l2, l1), actual.get(13));
        assertEquals(secondsBetween(l2, l1), actual.get(14));
        assertEquals(microsecondsBetween(l2, l1), actual.get(15));
        assertEquals(weeksBetween(l2, l1), actual.get(16));
        assertEquals(quartersBetween(l2, l1), actual.get(17));
    }
    
    @Test
    public void testTimestampWithDateDiff() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Document.class, "doc")
            .select("FUNCTION('YEAR_DIFF',   lastModified, creationDate)")
            .select("FUNCTION('MONTH_DIFF',  lastModified, creationDate)")
            .select("FUNCTION('DAY_DIFF',    lastModified, creationDate)")
            .select("FUNCTION('HOUR_DIFF',   lastModified, creationDate)")
            .select("FUNCTION('MINUTE_DIFF', lastModified, creationDate)")
            .select("FUNCTION('SECOND_DIFF', lastModified, creationDate)")
            .select("FUNCTION('MILLISECOND_DIFF', lastModified, creationDate)")
            .select("FUNCTION('YEAR_DIFF',   creationDate, lastModified)")
            .select("FUNCTION('MONTH_DIFF',  creationDate, lastModified)")
            .select("FUNCTION('DAY_DIFF',    creationDate, lastModified)")
            .select("FUNCTION('HOUR_DIFF',   creationDate, lastModified)")
            .select("FUNCTION('MINUTE_DIFF', creationDate, lastModified)")
            .select("FUNCTION('SECOND_DIFF', creationDate, lastModified)")
            .select("FUNCTION('MILLISECOND_DIFF', creationDate, lastModified)")
            .select("FUNCTION('EPOCH_DIFF', creationDate, lastModified)")
            .select("FUNCTION('MICROSECOND_DIFF', creationDate, lastModified)")
            .select("FUNCTION('WEEK_DIFF', creationDate, lastModified)")
            .select("FUNCTION('QUARTER_DIFF', creationDate, lastModified)")
            ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());
        
        Tuple actual = list.get(0);

        assertEquals(yearsBetween  (l1, c1), actual.get(0));
        assertEquals(monthsBetween (l1, c1), actual.get(1));
        assertEquals(daysBetween   (l1, c1), actual.get(2));
        assertEquals(hoursBetween  (l1, c1), actual.get(3));
        assertEquals(minutesBetween(l1, c1), actual.get(4));
        assertEquals(secondsBetween(l1, c1), actual.get(5));
        assertEquals(millisecondsBetween(l1, c1), actual.get(6));

        assertEquals(yearsBetween  (c1, l1), actual.get(7));
        assertEquals(monthsBetween (c1, l1), actual.get(8));
        assertEquals(daysBetween   (c1, l1), actual.get(9));
        assertEquals(hoursBetween  (c1, l1), actual.get(10));
        assertEquals(minutesBetween(c1, l1), actual.get(11));
        assertEquals(secondsBetween(c1, l1), actual.get(12));
        assertEquals(millisecondsBetween(c1, l1), actual.get(13));
        assertEquals(secondsBetween(c1, l1), actual.get(14));
        assertEquals(microsecondsBetween(c1, l1), actual.get(15));
        assertEquals(weeksBetween(c1, l1), actual.get(16));
        assertEquals(quartersBetween(c1, l1), actual.get(17));
    }

    @Test
    public void testTimestampWithParameterDiff() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("FUNCTION('YEAR_DIFF',   lastModified, :lastModified2)")
                .select("FUNCTION('MONTH_DIFF',  lastModified, :lastModified2)")
                .select("FUNCTION('DAY_DIFF',    lastModified, :lastModified2)")
                .select("FUNCTION('HOUR_DIFF',   lastModified, :lastModified2)")
                .select("FUNCTION('MINUTE_DIFF', lastModified, :lastModified2)")
                .select("FUNCTION('SECOND_DIFF', lastModified, :lastModified2)")
                .select("FUNCTION('MILLISECOND_DIFF', lastModified, :lastModified2)")
                .select("FUNCTION('YEAR_DIFF',   :lastModified2, lastModified)")
                .select("FUNCTION('MONTH_DIFF',  :lastModified2, lastModified)")
                .select("FUNCTION('DAY_DIFF',    :lastModified2, lastModified)")
                .select("FUNCTION('HOUR_DIFF',   :lastModified2, lastModified)")
                .select("FUNCTION('MINUTE_DIFF', :lastModified2, lastModified)")
                .select("FUNCTION('SECOND_DIFF', :lastModified2, lastModified)")
                .select("FUNCTION('MILLISECOND_DIFF', :lastModified2, lastModified)")
                .select("FUNCTION('YEAR_DIFF',   :lastModified2, :lastModified)")
                .select("FUNCTION('MONTH_DIFF',  :lastModified2, :lastModified)")
                .select("FUNCTION('DAY_DIFF',    :lastModified2, :lastModified)")
                .select("FUNCTION('HOUR_DIFF',   :lastModified2, :lastModified)")
                .select("FUNCTION('MINUTE_DIFF', :lastModified2, :lastModified)")
                .select("FUNCTION('SECOND_DIFF', :lastModified2, :lastModified)")
                .select("FUNCTION('MILLISECOND_DIFF', :lastModified2, :lastModified)")
                .select("FUNCTION('EPOCH_DIFF', :lastModified2, :lastModified)")
                .select("FUNCTION('MICROSECOND_DIFF', :lastModified2, :lastModified)")
                .select("FUNCTION('WEEK_DIFF', :lastModified2, :lastModified)")
                .select("FUNCTION('QUARTER_DIFF', :lastModified2, :lastModified)")
                ;

        criteria.setParameter("lastModified", l1, TemporalType.TIMESTAMP);
        criteria.setParameter("lastModified2", l2, TemporalType.TIMESTAMP);
        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);

        assertEquals(yearsBetween  (l1, l2), actual.get(0));
        assertEquals(monthsBetween (l1, l2), actual.get(1));
        assertEquals(daysBetween   (l1, l2), actual.get(2));
        assertEquals(hoursBetween  (l1, l2), actual.get(3));
        assertEquals(minutesBetween(l1, l2), actual.get(4));
        assertEquals(secondsBetween(l1, l2), actual.get(5));
        assertEquals(millisecondsBetween(l1, l2), actual.get(6));

        assertEquals(yearsBetween  (l2, l1), actual.get(7));
        assertEquals(monthsBetween (l2, l1), actual.get(8));
        assertEquals(daysBetween   (l2, l1), actual.get(9));
        assertEquals(hoursBetween  (l2, l1), actual.get(10));
        assertEquals(minutesBetween(l2, l1), actual.get(11));
        assertEquals(secondsBetween(l2, l1), actual.get(12));
        assertEquals(millisecondsBetween(l2, l1), actual.get(13));

        assertEquals(yearsBetween  (l2, l1), actual.get(14));
        assertEquals(monthsBetween (l2, l1), actual.get(15));
        assertEquals(daysBetween   (l2, l1), actual.get(16));
        assertEquals(hoursBetween  (l2, l1), actual.get(17));
        assertEquals(minutesBetween(l2, l1), actual.get(18));
        assertEquals(secondsBetween(l2, l1), actual.get(19));
        assertEquals(millisecondsBetween(l2, l1), actual.get(20));
        assertEquals(secondsBetween(l2, l1), actual.get(21));
        assertEquals(microsecondsBetween(l2, l1), actual.get(22));
        assertEquals(weeksBetween(l2, l1), actual.get(23));
        assertEquals(quartersBetween(l2, l1), actual.get(24));
    }

    @Test
    public void testDateWithParameterDiff() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("FUNCTION('YEAR_DIFF',   creationDate, :creationDate2)")
                .select("FUNCTION('MONTH_DIFF',  creationDate, :creationDate2)")
                .select("FUNCTION('DAY_DIFF',    creationDate, :creationDate2)")
                .select("FUNCTION('HOUR_DIFF',   creationDate, :creationDate2)")
                .select("FUNCTION('MINUTE_DIFF', creationDate, :creationDate2)")
                .select("FUNCTION('SECOND_DIFF', creationDate, :creationDate2)")
                .select("FUNCTION('MILLISECOND_DIFF', creationDate, :creationDate2)")
                .select("FUNCTION('YEAR_DIFF',   :creationDate2, creationDate)")
                .select("FUNCTION('MONTH_DIFF',  :creationDate2, creationDate)")
                .select("FUNCTION('DAY_DIFF',    :creationDate2, creationDate)")
                .select("FUNCTION('HOUR_DIFF',   :creationDate2, creationDate)")
                .select("FUNCTION('MINUTE_DIFF', :creationDate2, creationDate)")
                .select("FUNCTION('SECOND_DIFF', :creationDate2, creationDate)")
                .select("FUNCTION('MILLISECOND_DIFF', :creationDate2, creationDate)")
                .select("FUNCTION('YEAR_DIFF',   :creationDate2, :creationDate)")
                .select("FUNCTION('MONTH_DIFF',  :creationDate2, :creationDate)")
                .select("FUNCTION('DAY_DIFF',    :creationDate2, :creationDate)")
                .select("FUNCTION('HOUR_DIFF',   :creationDate2, :creationDate)")
                .select("FUNCTION('MINUTE_DIFF', :creationDate2, :creationDate)")
                .select("FUNCTION('SECOND_DIFF', :creationDate2, :creationDate)")
                .select("FUNCTION('MILLISECOND_DIFF', :creationDate2, :creationDate)")
                .select("FUNCTION('EPOCH_DIFF', :creationDate2, :creationDate)")
                .select("FUNCTION('MICROSECOND_DIFF', :creationDate2, :creationDate)")
                .select("FUNCTION('WEEK_DIFF', :creationDate2, :creationDate)")
                .select("FUNCTION('QUARTER_DIFF', :creationDate2, :creationDate)")
                ;

        criteria.setParameter("creationDate", c1, TemporalType.DATE);
        criteria.setParameter("creationDate2", c2, TemporalType.DATE);
        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);

        assertEquals(yearsBetween  (c1, c2), actual.get(0));
        assertEquals(monthsBetween (c1, c2), actual.get(1));
        assertEquals(daysBetween   (c1, c2), actual.get(2));
        assertEquals(hoursBetween  (c1, c2), actual.get(3));
        assertEquals(minutesBetween(c1, c2), actual.get(4));
        assertEquals(secondsBetween(c1, c2), actual.get(5));
        assertEquals(millisecondsBetween(c1, c2), actual.get(6));

        assertEquals(yearsBetween  (c2, c1), actual.get(7));
        assertEquals(monthsBetween (c2, c1), actual.get(8));
        assertEquals(daysBetween   (c2, c1), actual.get(9));
        assertEquals(hoursBetween  (c2, c1), actual.get(10));
        assertEquals(minutesBetween(c2, c1), actual.get(11));
        assertEquals(secondsBetween(c2, c1), actual.get(12));
        assertEquals(millisecondsBetween(c2, c1), actual.get(13));

        assertEquals(yearsBetween  (c2, c1), actual.get(14));
        assertEquals(monthsBetween (c2, c1), actual.get(15));
        assertEquals(daysBetween   (c2, c1), actual.get(16));
        assertEquals(hoursBetween  (c2, c1), actual.get(17));
        assertEquals(minutesBetween(c2, c1), actual.get(18));
        assertEquals(secondsBetween(c2, c1), actual.get(19));
        assertEquals(millisecondsBetween(c2, c1), actual.get(20));
        assertEquals(secondsBetween(c2, c1), actual.get(21));
        assertEquals(microsecondsBetween(c2, c1), actual.get(22));
        assertEquals(weeksBetween(c2, c1), actual.get(23));
        assertEquals(quartersBetween(c2, c1), actual.get(24));
    }

    public static long millisecondsBetween(Calendar day1, Calendar day2) {
        return day2.getTimeInMillis() - day1.getTimeInMillis();
    }

    public static long microsecondsBetween(Calendar day1, Calendar day2) {
        return (day2.getTimeInMillis() - day1.getTimeInMillis()) * 1000;
    }
    
    public static int secondsBetween(Calendar day1, Calendar day2) {
        return daysBetween(day1, day2) * 24 * 60 * 60
                + (day2.get(Calendar.HOUR) - day1.get(Calendar.HOUR)) * 60 * 60
                + (day2.get(Calendar.MINUTE) - day1.get(Calendar.MINUTE)) * 60
                + (day2.get(Calendar.SECOND) - day1.get(Calendar.SECOND));
    }
    
    public static int minutesBetween(Calendar day1, Calendar day2) {
        return daysBetween(day1, day2) * 24 * 60 
            + (day2.get(Calendar.HOUR) - day1.get(Calendar.HOUR)) * 60
            + (day2.get(Calendar.MINUTE) - day1.get(Calendar.MINUTE));
    }
    
    public static int hoursBetween(Calendar day1, Calendar day2) {
        return daysBetween(day1, day2) * 24 
            + (day2.get(Calendar.HOUR) - day1.get(Calendar.HOUR));
    }
    
    public static int daysBetween(Calendar day1, Calendar day2) {
        if (day2.get(Calendar.YEAR) == day1.get(Calendar.YEAR)) {
            return (day2.get(Calendar.DAY_OF_YEAR) - day1.get(Calendar.DAY_OF_YEAR));
        } else {
            int offset = 0;
            int signum;
            int targetYear;
            Calendar calendarIterator;
            
            if (day2.get(Calendar.YEAR) < day1.get(Calendar.YEAR)) {
                calendarIterator = (Calendar) day1.clone();
                targetYear = day2.get(Calendar.YEAR);
                signum = -1;
            } else {
                calendarIterator = (Calendar) day2.clone();
                targetYear = day1.get(Calendar.YEAR);
                signum = 1;
            }

            while (calendarIterator.get(Calendar.YEAR) != targetYear) {
                calendarIterator.add(Calendar.YEAR, -1);
                // getActualMaximum() important for leap years
                offset += signum * calendarIterator.getActualMaximum(Calendar.DAY_OF_YEAR);
            }

            return offset + (day2.get(Calendar.DAY_OF_YEAR) - day1.get(Calendar.DAY_OF_YEAR));
        }
    }

    public static int weeksBetween(Calendar day1, Calendar day2) {
        return daysBetween(day1, day2) / 7;
    }

    public static int quartersBetween(Calendar day1, Calendar day2) {
        return monthsBetween(day1, day2) / 3;
    }

    public static int floorDiv(int x, int y) {
        int r = x / y;
        // if the signs are different and modulo not zero, round down
        if ((x ^ y) < 0 && (r * y != x)) {
            r--;
        }
        return r;
    }

    public static int monthsBetween(Calendar day1, Calendar day2) {
        int offset = (day2.get(Calendar.YEAR) - day1.get(Calendar.YEAR)) * 12;
        return offset + (day2.get(Calendar.MONTH) - day1.get(Calendar.MONTH));
    }
    
    public static int yearsBetween(Calendar day1, Calendar day2) {
        return day2.get(Calendar.YEAR) - day1.get(Calendar.YEAR);
    }
}
