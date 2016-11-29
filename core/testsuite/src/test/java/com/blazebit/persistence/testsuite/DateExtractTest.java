/*
 * Copyright 2014 - 2016 Blazebit.
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

import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Tuple;

import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.base.category.NoMySQL;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public class DateExtractTest extends AbstractCoreTest {
    
    private Calendar c1;
    private Calendar c2;
    
    private Document doc1;

    @Before
    public void setUp() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Person p = new Person("Pers1");
                p.setAge(20L);
                em.persist(p);

                Version v1 = new Version();
                em.persist(v1);

                doc1 = new Document("Doc1", p, v1);

                c1 = Calendar.getInstance();
                c1.set(2000, 0, 1, 0, 0, 0);
                c1.set(Calendar.MILLISECOND, 0);
                doc1.setCreationDate(c1);

                c2 = Calendar.getInstance();
                c2.set(2000, 0, 1, 1, 1, 1);
                c2.set(Calendar.MILLISECOND, 0);
                doc1.setLastModified(c2.getTime());

                em.persist(doc1);
            }
        });
    }

    // NOTE: MySQL is strange again https://bugs.mysql.com/bug.php?id=31990
    @Test
    @Category({ NoMySQL.class })
    public void testDateExtract() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Document.class, "doc")
            .select("FUNCTION('YEAR',   creationDate)")
            .select("FUNCTION('MONTH',  creationDate)")
            .select("FUNCTION('DAY',    creationDate)")
            .select("FUNCTION('HOUR',   creationDate)")
            .select("FUNCTION('MINUTE', creationDate)")
            .select("FUNCTION('SECOND', creationDate)")
            .select("FUNCTION('YEAR',   lastModified)")
            .select("FUNCTION('MONTH',  lastModified)")
            .select("FUNCTION('DAY',    lastModified)")
            .select("FUNCTION('HOUR',   lastModified)")
            .select("FUNCTION('MINUTE', lastModified)")
            .select("FUNCTION('SECOND', lastModified)")
            ;

        List<Tuple> list = criteria.getResultList();
        assertEquals(1, list.size());
        
        Tuple actual = list.get(0);

        assertEquals(c1.get(Calendar.YEAR), actual.get(0));
        assertEquals(c1.get(Calendar.MONTH) + 1, actual.get(1));
        assertEquals(c1.get(Calendar.DAY_OF_MONTH), actual.get(2));
        assertEquals(c1.get(Calendar.HOUR), actual.get(3));
        assertEquals(c1.get(Calendar.MINUTE), actual.get(4));
        assertEquals(c1.get(Calendar.SECOND), actual.get(5));

        assertEquals(c2.get(Calendar.YEAR), actual.get(6));
        assertEquals(c2.get(Calendar.MONTH) + 1, actual.get(7));
        assertEquals(c2.get(Calendar.DAY_OF_MONTH), actual.get(8));
        assertEquals(c2.get(Calendar.HOUR), actual.get(9));
        assertEquals(c2.get(Calendar.MINUTE), actual.get(10));
        assertEquals(c2.get(Calendar.SECOND), actual.get(11));
    }
}
