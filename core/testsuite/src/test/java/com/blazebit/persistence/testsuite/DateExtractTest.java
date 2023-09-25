/*
 * Copyright 2014 - 2023 Blazebit.
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
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.1.0
 */
@RunWith(Parameterized.class)
public class DateExtractTest extends AbstractCoreTest {

    // The previous timezones so we can cache the datasources for one configuration run
    private static TimeZone previousProducerTimeZone;
    private static TimeZone previousClientTimeZone;

    private static final TimeZone dbmsTimeZone = TimeZone.getDefault();
    private Calendar c1;
    private Calendar c2;
    private Calendar lastModified;

    private TimeZone producerTimeZone;
    private TimeZone clientTimeZone;

    public DateExtractTest(TimeZone producerTimeZone, TimeZone clientTimeZone) {
        this.producerTimeZone = producerTimeZone;
        this.clientTimeZone = clientTimeZone;
    }

    @Parameterized.Parameters
    public static Collection<?> producerConsumerTimezones() {
        return Arrays.asList(
                new Object[]{ TimeZone.getTimeZone("GMT+00:00"), TimeZone.getTimeZone("GMT+00:00") },
                new Object[]{ TimeZone.getTimeZone("GMT+00:00"), TimeZone.getTimeZone("GMT+01:00") },
                new Object[]{ TimeZone.getTimeZone("GMT+01:00"), TimeZone.getTimeZone("GMT+01:00") },
                new Object[]{ TimeZone.getTimeZone("GMT+01:00"), TimeZone.getTimeZone("GMT+00:00") },

                new Object[]{ TimeZone.getTimeZone("GMT+12:00"), TimeZone.getTimeZone("GMT+06:00") },
                new Object[]{ TimeZone.getTimeZone("GMT-12:00"), TimeZone.getTimeZone("GMT-06:00") },
                new Object[]{ TimeZone.getTimeZone("GMT+06:00"), TimeZone.getTimeZone("GMT+12:00") },
                new Object[]{ TimeZone.getTimeZone("GMT-06:00"), TimeZone.getTimeZone("GMT-12:00") },

                new Object[]{ TimeZone.getTimeZone("GMT-12:00"), TimeZone.getTimeZone("GMT+06:00") },
                new Object[]{ TimeZone.getTimeZone("GMT+12:00"), TimeZone.getTimeZone("GMT-06:00") },
                new Object[]{ TimeZone.getTimeZone("GMT-06:00"), TimeZone.getTimeZone("GMT+12:00") },
                new Object[]{ TimeZone.getTimeZone("GMT+06:00"), TimeZone.getTimeZone("GMT-12:00") }
        );
    }

    @Override
    protected boolean recreateDataSource() {
        // Some drivers have timezone information bound to the connection
        // So we have to recreate the data source to get the newly configured time zones for connections
        boolean recreate = !producerTimeZone.equals(previousProducerTimeZone) || !clientTimeZone.equals(previousClientTimeZone);
        previousProducerTimeZone = producerTimeZone;
        previousClientTimeZone = clientTimeZone;
        return recreate;
    }

    @Override
    protected DataSource createDataSource(Map<Object, Object> properties, Consumer<Connection> connectionCustomizer) {
        // Set the producer timezone
        TimeZone.setDefault(producerTimeZone);
        resetTimeZoneCaches();

        return super.createDataSource(properties, connectionCustomizer);
    }

    @Before
    public void setup() {
        c1 = Calendar.getInstance();
        c1.set(2000, Calendar.JANUARY, 1, 0, 0, 0);
        c1.set(Calendar.MILLISECOND, 213);
        c1.setMinimalDaysInFirstWeek(4);
        c1.setFirstDayOfWeek(Calendar.MONDAY);

        c2 = Calendar.getInstance();
        c2.set(2000, Calendar.JANUARY, 1, 1, 1, 1);
        c2.set(Calendar.MILLISECOND, 412);
        c2.setMinimalDaysInFirstWeek(4);
        c2.setFirstDayOfWeek(Calendar.MONDAY);

        lastModified = Calendar.getInstance();
        lastModified.setMinimalDaysInFirstWeek(4);
        lastModified.setFirstDayOfWeek(Calendar.MONDAY);
    }

    @After
    public void tearDown() {
        // Back to the produce time zone like after recreation of the data source
        TimeZone.setDefault(producerTimeZone);
        resetTimeZoneCaches();
    }

    @Override
    // Doing this for every timezone
    protected void setUpOnce() {
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
                doc1.setLastModified(c2.getTime());

                em.persist(doc1);
            }
        });
    }

    @AfterClass
    public static void after() {
        TimeZone.setDefault(dbmsTimeZone);
        resetTimeZoneCaches();
    }

    @Test
    public void testDateExtractYear() {
        // Set the client timezone
        TimeZone.setDefault(clientTimeZone);
        resetTimeZoneCaches();

        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Document.class, "doc")
            .select("YEAR(creationDate)")
            .select("YEAR(lastModified)")
            ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());
        
        Tuple actual = list.get(0);

        assertEquals(c1.get(Calendar.YEAR), actual.get(0));
        assertEquals(c2.get(Calendar.YEAR), actual.get(1));
    }

    @Test
    public void testDateExtractYearOfWeek() {
        // Set the client timezone
        TimeZone.setDefault(clientTimeZone);
        resetTimeZoneCaches();

        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Document.class, "doc")
            .select("YEAR_OF_WEEK(creationDate)")
            .select("YEAR_OF_WEEK(lastModified)")
            ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);

        assertEquals(1999, actual.get(0));
        assertEquals(1999, actual.get(1));
    }

    @Test
    public void testDateExtractYearWeek() {
        // Set the client timezone
        TimeZone.setDefault(clientTimeZone);
        resetTimeZoneCaches();

        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Document.class, "doc")
            .select("YEAR_WEEK(creationDate)")
            .select("YEAR_WEEK(lastModified)")
            ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);

        assertEquals("1999-52", actual.get(0));
        assertEquals("1999-52", actual.get(1));
    }

    @Test
    public void testDateExtractMonth() {
        // Set the client timezone
        TimeZone.setDefault(clientTimeZone);
        resetTimeZoneCaches();

        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("MONTH(creationDate)")
                .select("MONTH(lastModified)")
                ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);

        assertEquals(c1.get(Calendar.MONTH) + 1, actual.get(0));
        assertEquals(c2.get(Calendar.MONTH) + 1, actual.get(1));
    }

    @Test
    public void testDateExtractDayOfMonth() {
        // Set the client timezone
        TimeZone.setDefault(clientTimeZone);
        resetTimeZoneCaches();

        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("DAY(creationDate)")
                .select("DAY(lastModified)")
                ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);

        assertEquals(c1.get(Calendar.DAY_OF_MONTH), actual.get(0));
        assertEquals(c2.get(Calendar.DAY_OF_MONTH), actual.get(1));
    }


    @Test
    public void testDateExtractIsoWeekOfYear() {
        // Set the client timezone
        TimeZone.setDefault(clientTimeZone);
        resetTimeZoneCaches();

        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("WEEK(creationDate)")
                .select("WEEK(lastModified)")
                ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);

        assertEquals(c1.get(Calendar.WEEK_OF_YEAR), (int) actual.get(0));
        assertEquals(c2.get(Calendar.WEEK_OF_YEAR), (int) actual.get(1));
    }

    @Test
    public void testDateExtractWeekInYear() {
        // Set the client timezone
        TimeZone.setDefault(clientTimeZone);
        resetTimeZoneCaches();

        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("WEEK_IN_YEAR(creationDate)")
                .select("WEEK_IN_YEAR(lastModified)")
                ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);

        assertEquals(1, (int) actual.get(0));
        assertEquals(1, (int) actual.get(1));
    }

    @Test
    public void testDateExtractDayOfYear() {
        // Set the client timezone
        TimeZone.setDefault(clientTimeZone);
        resetTimeZoneCaches();

        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("DAYOFYEAR(creationDate)")
                .select("DAYOFYEAR(lastModified)")
                ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);

        assertEquals(c1.get(Calendar.DAY_OF_YEAR), (int) actual.get(0));
        assertEquals(c2.get(Calendar.DAY_OF_YEAR), (int) actual.get(1));
    }

    @Test
    public void testDateExtractDayOfWeek() {
        // Set the client timezone
        TimeZone.setDefault(clientTimeZone);
        resetTimeZoneCaches();

        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("DAYOFWEEK(creationDate)")
                .select("DAYOFWEEK(lastModified)")
                ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);

        assertEquals(c1.get(Calendar.DAY_OF_WEEK), (int) actual.get(0));
        assertEquals(c2.get(Calendar.DAY_OF_WEEK), (int) actual.get(1));
    }

    @Test
    public void testDateExtractIsoDayOfWeek() {
        // Set the client timezone
        TimeZone.setDefault(clientTimeZone);
        resetTimeZoneCaches();

        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("ISODAYOFWEEK(creationDate)")
                .select("ISODAYOFWEEK(lastModified)")
                ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);

        assertEquals((c1.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1, (int) actual.get(0));
        assertEquals((c2.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1, (int) actual.get(1));
    }

    @Test
    public void testDateExtractQuarter() {
        // Set the client timezone
        TimeZone.setDefault(clientTimeZone);
        resetTimeZoneCaches();

        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("QUARTER(creationDate)")
                .select("QUARTER(lastModified)")
                ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);

        assertEquals((c1.get(Calendar.MONTH) + 3) / 3, (int) actual.get(0));
        assertEquals((c2.get(Calendar.MONTH) + 3) / 3, (int) actual.get(1));
    }

    @Test
    public void testDateExtractHourOfDay() {
        // Set the client timezone
        TimeZone.setDefault(clientTimeZone);
        resetTimeZoneCaches();

        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("HOUR(lastModified)")
                ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);

        assertEquals(c2.get(Calendar.HOUR_OF_DAY), (int) actual.get(0));
    }

    @Test
    public void testDateExtractMinute() {
        // Set the client timezone
        TimeZone.setDefault(clientTimeZone);
        resetTimeZoneCaches();

        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("MINUTE(lastModified)")
                ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);

        assertEquals(c2.get(Calendar.MINUTE), (int) actual.get(0));
    }

    @Test
    public void testDateExtractSecond() {
        // Set the client timezone
        TimeZone.setDefault(clientTimeZone);
        resetTimeZoneCaches();

        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("SECOND(lastModified)")
                ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);

        assertEquals(c2.get(Calendar.SECOND), actual.get(0, Number.class).intValue());
    }

    @Test
    public void testDateExtractMillisecond() {
        // Set the client timezone
        TimeZone.setDefault(clientTimeZone);
        resetTimeZoneCaches();

        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("MILLISECOND(lastModified)")
                .select("lastModified")
                ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);

        lastModified.setTime((Date) actual.get(1));
        assumeTrue("Milliseconds were truncated or rounded in " + dbmsDialect, lastModified.get(Calendar.MILLISECOND) == c2.get(Calendar.MILLISECOND));

        assertEquals(c2.get(Calendar.MILLISECOND), (int) actual.get(0), 1); // Milliseconds are rounded in MS SQL
    }

    @Test
    public void testDateExtractMicrosecond() {
        // Set the client timezone
        TimeZone.setDefault(clientTimeZone);
        resetTimeZoneCaches();

        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("MICROSECOND(lastModified)")
                .select("lastModified")
                ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);

        lastModified.setTime((Date) actual.get(1));
        assumeTrue("Milliseconds were truncated or rounded in " + dbmsDialect, lastModified.get(Calendar.MILLISECOND) == c2.get(Calendar.MILLISECOND));

        assertEquals(c2.get(Calendar.MILLISECOND) * 1000, (int) actual.get(0), 2000); // Microseconds are rounded in MS SQL
    }

    @Test
    public void testDateExtractEpoch() {
        // Set the client timezone
        TimeZone.setDefault(clientTimeZone);
        resetTimeZoneCaches();

        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("EPOCH(creationDate)")
                .select("EPOCH(lastModified)")
                ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);

        int offsetInMillis1 = producerTimeZone.getOffset(c1.getTimeInMillis());
        int offsetInMillis2 = producerTimeZone.getOffset(c2.getTimeInMillis());
        assertEquals(c1.getTimeInMillis() / 1000L, actual.get(0, Long.class) - (offsetInMillis1 / 1000L));
        assertEquals(c2.getTimeInMillis() / 1000L, actual.get(1, Long.class) - (offsetInMillis2 / 1000L));
    }

    @Test
    public void testDateExtractEpochSeconds() {
        // Set the client timezone
        TimeZone.setDefault(clientTimeZone);
        resetTimeZoneCaches();

        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("EPOCH_SECONDS(creationDate)")
                .select("EPOCH_SECONDS(lastModified)")
                ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);

        int offsetInMillis1 = producerTimeZone.getOffset(c1.getTimeInMillis());
        int offsetInMillis2 = producerTimeZone.getOffset(c2.getTimeInMillis());
        assertEquals(c1.getTimeInMillis() / 1000L, actual.get(0, Long.class) - (offsetInMillis1 / 1000L));
        assertEquals(c2.getTimeInMillis() / 1000L, actual.get(1, Long.class) - (offsetInMillis2 / 1000L));
    }

    @Test
    public void testDateExtractEpochDays() {
        // Set the client timezone
        TimeZone.setDefault(clientTimeZone);
        resetTimeZoneCaches();

        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("EPOCH_DAYS(creationDate)")
                .select("EPOCH_DAYS(lastModified)")
                ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);

        assertEquals((int) (((c1.getTimeInMillis() + producerTimeZone.getOffset(c1.getTimeInMillis())) / (1000L * 60L * 60L * 24L))), (int) actual.get(0, Integer.class));
        assertEquals((int) (((c2.getTimeInMillis() + producerTimeZone.getOffset(c2.getTimeInMillis())) / (1000L * 60L * 60L * 24L))), (int) actual.get(1, Integer.class));
    }

    @Test
    public void testDateExtractEpochMilliseconds() {
        // Set the client timezone
        TimeZone.setDefault(clientTimeZone);
        resetTimeZoneCaches();

        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("EPOCH_MILLISECONDS(creationDate)")
                .select("EPOCH_MILLISECONDS(lastModified)")
                ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);

        int offsetInMillis1 = producerTimeZone.getOffset(c1.getTimeInMillis());
        int offsetInMillis2 = producerTimeZone.getOffset(c2.getTimeInMillis());
        // A date has no fractional part
        assertEquals(((long) (c1.getTimeInMillis() / 1000L)) * 1000L, actual.get(0, Long.class) - offsetInMillis1);
        assertEquals(c2.getTimeInMillis(), actual.get(1, Long.class) - offsetInMillis2);
    }

    @Test
    public void testDateExtractEpochMicroseconds() {
        // Set the client timezone
        TimeZone.setDefault(clientTimeZone);
        resetTimeZoneCaches();

        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("EPOCH_MICROSECONDS(ADD_MICROSECONDS(creationDate, 1))")
                .select("EPOCH_MICROSECONDS(ADD_MICROSECONDS(lastModified, 1))")
                ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());

        Tuple actual = list.get(0);

        int offsetInMillis1 = producerTimeZone.getOffset(c1.getTimeInMillis());
        int offsetInMillis2 = producerTimeZone.getOffset(c2.getTimeInMillis());
        // Hours, minutes, seconds, milliseconds were lost in calendar to date conversion, only check whether the epoch ends with a 1.
        assertEquals(actual.get(0, Long.class) & 1L, 1L);
        assertEquals(c2.getTimeInMillis() * 1000L + 1L, actual.get(1, Long.class) - offsetInMillis2 * 1000L);
    }

}
