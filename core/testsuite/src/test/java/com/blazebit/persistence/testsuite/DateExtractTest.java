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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.sql.DataSource;

import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.1.0
 */
@RunWith(Parameterized.class)
public class DateExtractTest extends AbstractCoreTest {

    private final TimeZone dbmsTimeZone = TimeZone.getDefault();
    private Calendar c1;
    private Calendar c2;

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
        return true;
    }

    @Override
    protected DataSource createDataSource(Map<Object, Object> properties) {
        // Set the producer timezone
        TimeZone.setDefault(producerTimeZone);
        resetTimeZoneCaches();

        c1 = Calendar.getInstance();
        c1.set(2000, 0, 1, 0, 0, 0);
        c1.set(Calendar.MILLISECOND, 213);

        c2 = Calendar.getInstance();
        c2.set(2000, 0, 1, 1, 1, 1);
        c2.set(Calendar.MILLISECOND, 412);

        return super.createDataSource(properties);
    }

    // Doing this for every timezone
    @Before
    public void setUp() {
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
                doc1.setLastModified(c2.getTime());

                em.persist(doc1);
            }
        });
    }

    @After
    public void after() {
        TimeZone.setDefault(dbmsTimeZone);
        resetTimeZoneCaches();
    }

    private static void resetTimeZoneCaches() {
        // The H2 JDBC driver is not able to handle timezone changes because of an internal cache
        try {
            Class.forName("org.h2.util.DateTimeUtils").getMethod("resetCalendar").invoke(null);
        } catch (Exception e) {
            // Ignore any exceptions. If it is H2 it will succeed, otherwise will fail on class lookup already
        }

        // EclipseLink caches the timezone so we have to purge that cache
        try {
            Class<?> helperClass = Class.forName("org.eclipse.persistence.internal.helper.Helper");
            Field f = helperClass.getDeclaredField("defaultTimeZone");
            f.setAccessible(true);
            f.set(null, TimeZone.getDefault());

            f = helperClass.getDeclaredField("calendarCache");
            f.setAccessible(true);
            f.set(null, helperClass.getMethod("initCalendarCache").invoke(null));
        } catch (Exception e) {
            // Ignore any exceptions. If it is EclipseLink it will succeed, otherwise will fail on class lookup already
        }
    }

    // NOTE: MySQL is strange again https://bugs.mysql.com/bug.php?id=31990
    @Test
    @Category({ NoMySQL.class })
    public void testDateExtract() {
        // Set the client timezone
        TimeZone.setDefault(clientTimeZone);
        resetTimeZoneCaches();

        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Document.class, "doc")
            .select("FUNCTION('YEAR',   creationDate)")
            .select("FUNCTION('MONTH',  creationDate)")
            .select("FUNCTION('DAY',    creationDate)")
            .select("FUNCTION('HOUR',   creationDate)")
            .select("FUNCTION('MINUTE', creationDate)")
            .select("FUNCTION('SECOND', creationDate)")
            .select("FUNCTION('EPOCH',  creationDate)")
            .select("FUNCTION('YEAR',   lastModified)")
            .select("FUNCTION('MONTH',  lastModified)")
            .select("FUNCTION('DAY',    lastModified)")
            .select("FUNCTION('HOUR',   lastModified)")
            .select("FUNCTION('MINUTE', lastModified)")
            .select("FUNCTION('SECOND', lastModified)")
            .select("FUNCTION('EPOCH',  lastModified)")
            ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());
        
        Tuple actual = list.get(0);

        int offsetInMillis1 = producerTimeZone.getOffset(c1.getTimeInMillis());
        int offsetInMillis2 = producerTimeZone.getOffset(c2.getTimeInMillis());

        assertEquals(c1.get(Calendar.YEAR), actual.get(0));
        assertEquals(c1.get(Calendar.MONTH) + 1, actual.get(1));
        assertEquals(c1.get(Calendar.DAY_OF_MONTH), actual.get(2));
        assertEquals(c1.get(Calendar.HOUR_OF_DAY), (int) actual.get(3));
        assertEquals(c1.get(Calendar.MINUTE), (int) actual.get(4));
        assertEquals(c1.get(Calendar.SECOND), (int) actual.get(5));
        // But there is an offset in the time in millis as that is the epoch
        assertEquals((int) (c1.getTimeInMillis() / 1000L), (int) actual.get(6) - (offsetInMillis1 / 1000L));

        assertEquals(c2.get(Calendar.YEAR), actual.get(7));
        assertEquals(c2.get(Calendar.MONTH) + 1, actual.get(8));
        assertEquals(c2.get(Calendar.DAY_OF_MONTH), actual.get(9));
        assertEquals(c2.get(Calendar.HOUR_OF_DAY), (int) actual.get(10));
        assertEquals(c2.get(Calendar.MINUTE), (int) actual.get(11));
        assertEquals(c2.get(Calendar.SECOND), (int) actual.get(12));
        assertEquals((int) (c2.getTimeInMillis() / 1000L), (int) actual.get(13) - (offsetInMillis2 / 1000L));
    }
}
