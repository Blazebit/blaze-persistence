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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDB2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMSSQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @author Sayra Ranjha
 * @since 1.4.0
 */
public class WindowFunctionTest extends AbstractCoreTest {

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Person p = new Person("Pers1");
                p.setAge(1L);
                em.persist(p);
                Person p2 = new Person("Pers2");
                p2.setAge(2L);
                em.persist(p2);
                Person p3 = new Person("Pers3");
                p3.setAge(3L);
                em.persist(p3);
                Person p4 = new Person("Pers4");
                p4.setAge(4L);
                em.persist(p4);
            }
        });
    }

    @Test
    public void testBasicAggregatesOverRows() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("FUNCTION('WINDOW_SUM', per.age, 'ORDER BY', per.age, 'ROWS', 'BETWEEN', 'UNBOUNDED PRECEDING', 'AND', 'CURRENT ROW')")
            .select("FUNCTION('WINDOW_MAX', per.age, 'ORDER BY', per.age, 'ROWS', 'BETWEEN', 'UNBOUNDED PRECEDING', 'AND', 'CURRENT ROW')")
            .select("FUNCTION('WINDOW_MIN', per.age, 'ORDER BY', per.age, 'ROWS', 'BETWEEN', 'UNBOUNDED PRECEDING', 'AND', 'CURRENT ROW')")
            .select("FUNCTION('WINDOW_AVG', per.age, 'ORDER BY', per.age, 'ROWS', 'BETWEEN', 'UNBOUNDED PRECEDING', 'AND', 'CURRENT ROW')")
            .orderByAsc("per.age")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testCountAggregate() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Person.class, "per")
                .select("per.age")
                .select("FUNCTION('WINDOW_COUNT', per.age, 'ORDER BY', per.age, 'ROWS', 'BETWEEN', 'UNBOUNDED PRECEDING', 'AND', 'CURRENT ROW')")
                .orderByAsc("per.age")
                ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testCountStarAggregate() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Person.class, "per")
                .select("per.age")
                .select("FUNCTION('WINDOW_COUNT', 'ORDER BY', per.age, 'ROWS', 'BETWEEN', 'UNBOUNDED PRECEDING', 'AND', 'CURRENT ROW')")
                .orderByAsc("per.age")
                ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testBasicAggregatesOverRange() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("FUNCTION('WINDOW_SUM', per.age, 'ORDER BY', per.age, 'RANGE', 'BETWEEN', 'UNBOUNDED PRECEDING', 'AND', 'CURRENT ROW')")
            .select("FUNCTION('WINDOW_MAX', per.age, 'ORDER BY', per.age, 'RANGE', 'BETWEEN', 'UNBOUNDED PRECEDING', 'AND', 'CURRENT ROW')")
            .select("FUNCTION('WINDOW_MIN', per.age, 'ORDER BY', per.age, 'RANGE', 'BETWEEN', 'UNBOUNDED PRECEDING', 'AND', 'CURRENT ROW')")
            .select("FUNCTION('WINDOW_AVG', per.age, 'ORDER BY', per.age, 'RANGE', 'BETWEEN', 'UNBOUNDED PRECEDING', 'AND', 'CURRENT ROW')")
            .orderByAsc("per.age")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testBasicAggregatesWithVariousRanges() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("FUNCTION('WINDOW_SUM', per.age, 'ORDER BY', per.age, 'ROWS', 'CURRENT ROW')")
            .select("FUNCTION('WINDOW_SUM', per.age, 'ORDER BY', per.age, 'ROWS', 'UNBOUNDED PRECEDING')")
            .select("FUNCTION('WINDOW_SUM', per.age, 'ORDER BY', per.age, 'ROWS', 1, 'PRECEDING')")
            .select("FUNCTION('WINDOW_SUM', per.age, 'ORDER BY', per.age, 'ROWS', 'BETWEEN', 'CURRENT ROW', 'AND', 'CURRENT ROW')")
            .select("FUNCTION('WINDOW_SUM', per.age, 'ORDER BY', per.age, 'ROWS', 'BETWEEN', 'UNBOUNDED PRECEDING', 'AND', 'CURRENT ROW')")
            .select("FUNCTION('WINDOW_SUM', per.age, 'ORDER BY', per.age, 'ROWS', 'BETWEEN', 1, 'PRECEDING', 'AND', 'CURRENT ROW')")
            .select("FUNCTION('WINDOW_SUM', per.age, 'ORDER BY', per.age, 'ROWS', 'BETWEEN', 'CURRENT ROW', 'AND', 'CURRENT ROW')")
            .select("FUNCTION('WINDOW_SUM', per.age, 'ORDER BY', per.age, 'ROWS', 'BETWEEN', 'CURRENT ROW', 'AND', 'UNBOUNDED FOLLOWING')")
            .select("FUNCTION('WINDOW_SUM', per.age, 'ORDER BY', per.age, 'ROWS', 'BETWEEN', 'CURRENT ROW', 'AND', 1, 'FOLLOWING')")
            .orderByAsc("per.age")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testWindowBooleanAggregateOverRows() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Person.class, "per")
                .select("per.age")
                .select("FUNCTION('WINDOW_EVERY', true, 'ORDER BY', per.id, 'ROWS', 'BETWEEN', 'UNBOUNDED PRECEDING', 'AND', 'CURRENT ROW')")
                .select("FUNCTION('WINDOW_OR_AGG', true, 'ORDER BY', per.id, 'ROWS', 'BETWEEN', 'UNBOUNDED PRECEDING', 'AND', 'CURRENT ROW')")
                .orderByAsc("per.age")
                ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }


    @Test
    @Category({NoMSSQL.class, NoOracle.class}) // MSSQL demands a specific ordering for row_number
    public void testRowNumberBaseWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("FUNCTION('ROW_NUMBER')")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testSortVariantsOverOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Person.class, "per")
                .select("per.age")
                .select("FUNCTION('ROW_NUMBER', 'ORDER BY', per.age)")
                .select("FUNCTION('ROW_NUMBER', 'ORDER BY', per.age, 'ASC')")
                .select("FUNCTION('ROW_NUMBER', 'ORDER BY', per.age, 'DESC')")
                .select("FUNCTION('ROW_NUMBER', 'ORDER BY', per.age, 'ASC NULLS FIRST')")
                .select("FUNCTION('ROW_NUMBER', 'ORDER BY', per.age, 'ASC NULLS LAST')")
                .select("FUNCTION('ROW_NUMBER', 'ORDER BY', per.age, 'DESC NULLS FIRST')")
                .select("FUNCTION('ROW_NUMBER', 'ORDER BY', per.age, 'DESC NULLS LAST')")
                ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }


    @Test
    public void testRowNumberOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("FUNCTION('ROW_NUMBER', 'ORDER BY', per.age)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testRowNumberPartitionedAndOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("FUNCTION('ROW_NUMBER', 'PARTITION BY', per.name, 'ORDER BY', per.age)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testRankOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("FUNCTION('RANK', 'ORDER BY', per.age)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testRankPartitionedAndOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("FUNCTION('RANK', 'PARTITION BY', per.name, 'ORDER BY', per.age)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testDenseRankOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("FUNCTION('DENSE_RANK', 'ORDER BY', per.age)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testDenseRankPartitionedAndOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("FUNCTION('DENSE_RANK', 'PARTITION BY', per.name, 'ORDER BY', per.age)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testPercentRankOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("FUNCTION('PERCENT_RANK', 'ORDER BY', per.age)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testPercentRankPartitionedAndOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("FUNCTION('PERCENT_RANK', 'PARTITION BY', per.name, 'ORDER BY', per.age)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testPercentRankPartitionedAndMultiOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("FUNCTION('PERCENT_RANK', 'PARTITION BY', per.name, 'ORDER BY', per.age, per.name)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testCumeDistOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("FUNCTION('CUME_DIST', 'ORDER BY', per.age)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testCumeDistPartitionedAndOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("FUNCTION('CUME_DIST', 'PARTITION BY', per.name, 'ORDER BY', per.age)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testNtileOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("FUNCTION('NTILE', 10, 'ORDER BY', per.age)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testNtilePartitionedAndOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("FUNCTION('NTILE', 10, 'PARTITION BY', per.name, 'ORDER BY', per.age)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testLeadOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.id")
            .select("per.age")
            .select("FUNCTION('LEAD', per.age, 'ORDER BY', per.id)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testLeadPartitionedAndOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Person.class, "per")
                .select("per.id")
                .select("per.age")
                .select("FUNCTION('LEAD', per.age, 2, 'PARTITION BY', per.name, 'ORDER BY', per.id)")
                ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testLagOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.id")
            .select("per.age")
            .select("FUNCTION('LAG', per.age, 'ORDER BY', per.id)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testLagPartitionedAndOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Person.class, "per")
                .select("per.id")
                .select("per.age")
                .select("FUNCTION('LAG', per.age, 2, 'PARTITION BY', per.name, 'ORDER BY', per.id)")
                ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testFirstValueOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.id")
            .select("per.age")
            .select("FUNCTION('FIRST_VALUE', per.age, 'ORDER BY', per.id)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testFirstValuePartitionedAndOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Person.class, "per")
                .select("per.id")
                .select("per.age")
                .select("FUNCTION('FIRST_VALUE', per.age,'PARTITION BY', per.name, 'ORDER BY', per.id)")
                ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testLastValueOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.id")
            .select("per.age")
            .select("FUNCTION('LAST_VALUE', per.age, 'ORDER BY', per.id)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testLastValuePartitionedAndOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Person.class, "per")
                .select("per.id")
                .select("per.age")
                .select("FUNCTION('LAST_VALUE', per.age,'PARTITION BY', per.name, 'ORDER BY', per.id)")
                ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    @Category(NoMSSQL.class) // MSSQL does not have NTH_VALUE
    public void testNthValueOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.id")
            .select("per.age")
            .select("FUNCTION('NTH_VALUE', per.age, 1, 'ORDER BY', per.id)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    @Category(NoMSSQL.class) // MSSQL does not have NTH_VALUE
    public void testNthValuePartitionedAndOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Person.class, "per")
                .select("per.id")
                .select("per.age")
                .select("FUNCTION('NTH_VALUE', per.age, 1, 'PARTITION BY', per.name, 'ORDER BY', per.id)")
                ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

}
