/*
 * Copyright 2014 - 2021 Blazebit.
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
import com.blazebit.persistence.testsuite.base.jpa.category.NoMSSQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQLOld;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @author Sayra Ranjha
 * @since 1.4.0
 */
@Category({ NoMySQLOld.class })
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
    public void testCountFilter() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Person.class, "per")
                .select("per.age")
                .select("COUNT(*) FILTER (WHERE friend IS NOT NULL)")
                .orderByAsc("per.age")
                ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testCountWindowFilter() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Person.class, "per")
                .select("per.age")
                .select("COUNT(*) FILTER (WHERE friend IS NOT NULL) OVER (ORDER BY per.age)")
                .orderByAsc("per.age")
                ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testBasicAggregatesOverRows() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("SUM(per.age) OVER (ORDER BY per.age ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)")
            .select("MAX(per.age) OVER (ORDER BY per.age ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)")
            .select("MIN(per.age) OVER (ORDER BY per.age ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)")
            .select("AVG(per.age) OVER (ORDER BY per.age ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)")
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
                .select("COUNT(per.age) OVER (ORDER BY per.age ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)")
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
                .select("COUNT(*) OVER (ORDER BY per.age ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)")
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
            .select("SUM(per.age) OVER (ORDER BY per.age RANGE BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)")
            .select("MAX(per.age) OVER (ORDER BY per.age RANGE BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)")
            .select("MIN(per.age) OVER (ORDER BY per.age RANGE BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)")
            .select("AVG(per.age) OVER (ORDER BY per.age RANGE BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)")
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
            .select("SUM(per.age) OVER (ORDER BY per.age ROWS CURRENT ROW)")
            .select("SUM(per.age) OVER (ORDER BY per.age ROWS UNBOUNDED PRECEDING)")
            .select("SUM(per.age) OVER (ORDER BY per.age ROWS 1 PRECEDING)")
            .select("SUM(per.age) OVER (ORDER BY per.age ROWS BETWEEN CURRENT ROW AND CURRENT ROW)")
            .select("SUM(per.age) OVER (ORDER BY per.age ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)")
            .select("SUM(per.age) OVER (ORDER BY per.age ROWS BETWEEN 1 PRECEDING AND CURRENT ROW)")
            .select("SUM(per.age) OVER (ORDER BY per.age ROWS BETWEEN CURRENT ROW AND CURRENT ROW)")
            .select("SUM(per.age) OVER (ORDER BY per.age ROWS BETWEEN CURRENT ROW AND UNBOUNDED FOLLOWING)")
            .select("SUM(per.age) OVER (ORDER BY per.age ROWS BETWEEN CURRENT ROW AND 1 FOLLOWING)")
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
                .select("EVERY(true) OVER (ORDER BY per.id ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)")
                .select("OR_AGG(true) OVER (ORDER BY per.id ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)")
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
            .select("ROW_NUMBER()")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testSortVariantsOverOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Person.class, "per")
                .select("per.age")
                .select("ROW_NUMBER() OVER (ORDER BY per.age)")
                .select("ROW_NUMBER() OVER (ORDER BY per.age ASC)")
                .select("ROW_NUMBER() OVER (ORDER BY per.age DESC)")
                .select("ROW_NUMBER() OVER (ORDER BY per.age ASC NULLS FIRST)")
                .select("ROW_NUMBER() OVER (ORDER BY per.age ASC NULLS LAST)")
                .select("ROW_NUMBER() OVER (ORDER BY per.age DESC NULLS FIRST)")
                .select("ROW_NUMBER() OVER (ORDER BY per.age DESC NULLS LAST)")
                ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }


    @Test
    public void testRowNumberOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("ROW_NUMBER() OVER (ORDER BY per.age)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testRowNumberPartitionedAndOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("ROW_NUMBER() OVER (PARTITION BY per.name ORDER BY per.age)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testImplicitGroupByWithWindowFunction() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Person.class, "per")
                .select("per.age")
                .select("ROW_NUMBER() OVER (ORDER BY per.age)")
                .select("AVG(per.id) / LAG(AVG(per.id)) OVER (ORDER BY per.age)")
                .select("AVG(per.id) / (SUM(AVG(per.id)) OVER (ORDER BY per.age) / ROW_NUMBER() OVER (ORDER BY per.age))")
                .orderByAsc("per.age")
                ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testRankOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("RANK() OVER (ORDER BY per.age)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testRankPartitionedAndOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("RANK() OVER (PARTITION BY per.name ORDER BY per.age)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testDenseRankOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("DENSE_RANK() OVER (ORDER BY per.age)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testDenseRankPartitionedAndOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("DENSE_RANK() OVER (PARTITION BY per.name ORDER BY per.age)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testPercentRankOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("PERCENT_RANK() OVER (ORDER BY per.age)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testPercentRankPartitionedAndOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("PERCENT_RANK() OVER (PARTITION BY per.name ORDER BY per.age)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testPercentRankPartitionedAndMultiOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("PERCENT_RANK() OVER (PARTITION BY per.name ORDER BY per.age, per.name)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testCumeDistOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("CUME_DIST() OVER (ORDER BY per.age)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testCumeDistPartitionedAndOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("CUME_DIST() OVER (PARTITION BY per.name ORDER BY per.age)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testNtileOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("NTILE(10) OVER (ORDER BY per.age)")
            ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testNtilePartitionedAndOrderedWindow() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Person.class, "per")
            .select("per.age")
            .select("NTILE(10) OVER (PARTITION BY per.name ORDER BY per.age)")
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
            .select("LEAD(per.age) OVER (ORDER BY per.id)")
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
                .select("LEAD(per.age, 2) OVER (PARTITION BY per.name ORDER BY per.id)")
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
            .select("LAG(per.age) OVER (ORDER BY per.id)")
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
                .select("LAG(per.age, 2) OVER (PARTITION BY per.name ORDER BY per.id)")
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
            .select("FIRST_VALUE(per.age) OVER (ORDER BY per.id)")
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
                .select("FIRST_VALUE(per.age) OVER (PARTITION BY per.name ORDER BY per.id)")
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
            .select("LAST_VALUE(per.age) OVER (ORDER BY per.id)")
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
                .select("LAST_VALUE(per.age) OVER (PARTITION BY per.name ORDER BY per.id)")
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
            .select("NTH_VALUE(per.age, 1) OVER (ORDER BY per.id)")
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
                .select("NTH_VALUE(per.age, 1) OVER (PARTITION BY per.name ORDER BY per.id)")
                ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testParsedInlineWindowDefinition() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Person.class, "per")
                .select("per.age")
                .select("SUM(per.age) OVER (ORDER BY per.age ASC ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)")
                .orderByAsc("per.age")
                ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testWrappedParsedInlineWindowDefinition() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Person.class, "per")
                .select("per.age")
                .select("SUM(per.age) OVER (ORDER BY per.age ASC ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)")
                .orderByAsc("per.age")
                ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testParsedNamedWindowDefinition() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Person.class, "per")
                .window("x").orderByAsc("per.age").end()
                .select("per.age")
                .select("SUM(per.age) OVER x")
                .orderByAsc("per.age")
                ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testParsedCopiedNamedWindowDefinition() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Person.class, "per")
                .window("x").orderByAsc("per.age").end()
                .select("per.age")
                .select("SUM(per.age) OVER (x)")
                .orderByAsc("per.age")
                ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testWrappedParsedNamedWindowDefinition() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Person.class, "per")
                .window("x").orderByAsc("per.age").end()
                .select("per.age")
                .select("SUM(per.age) OVER x")
                .orderByAsc("per.age")
                ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    @Test
    public void testWrappedParsedCopiedNamedWindowDefinition() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Person.class, "per")
                .window("x").orderByAsc("per.age").end()
                .select("per.age")
                .select("SUM(per.age) OVER (x)")
                .orderByAsc("per.age")
                ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    /**
     * A window function over a named window with specified range
     */
    @Test
    public void testParsedNamedWindowDefinitionWithRange() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Person.class, "per")
                .window("x").orderByAsc("per.age").rows().betweenUnboundedPreceding().andCurrentRow().end()
                .select("per.age")
                .select("SUM(per.age) OVER x")
                .orderByAsc("per.age")
                ;

        String queryString = criteria.getQueryString();

        assertTrue(queryString.contains(function("window_sum", "per.age", "'ORDER BY'", "per.age",
                "'ASC NULLS LAST'", "'ROWS'", "'BETWEEN'", "'UNBOUNDED PRECEDING'", "'AND'", "'CURRENT ROW'")));

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    /**
     * Copy-and-modify syntax is only allowed for windows that do not specify a range.
     * Yet, we allow it and re-render it as `OVER x` instead.
     *
     * @see #testParsedNamedWindowDefinitionWithRange()
     */
    @Test
    public void testCopyAndModifyWindowDefinitionWithRange() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Person.class, "per")
                .window("x").orderByAsc("per.age").rows().betweenUnboundedPreceding().andCurrentRow().end()
                .select("per.age")
                .select("SUM(per.age) OVER (x)")
                .orderByAsc("per.age")
                ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    /**
     * Adding ordering through copy-and-modify syntax
     */
    @Test
    public void testParsedNamedWindowSpecifyOrdering() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Person.class, "per")
                .window("x").partitionBy("per.age").end()
                .select("per.age")
                .select("SUM(per.age) OVER (x ORDER BY per.id)")
                .orderByAsc("per.age")
                ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }


    /**
     * Specifying an ORDER in a WINDOW DEF is only allowed if the base window did not specify ordering
     *
     * @see #testParsedNamedWindowSpecifyOrdering()
     */
    @Test(expected = IllegalArgumentException.class)
    public void testParsedNamedWindowRespecifyOrdering() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Person.class, "per")
                .window("x").orderByAsc("per.age").end()
                .select("per.age")
                .select("SUM(per.age) OVER (x ORDER BY per.id)")
                .orderByAsc("per.age")
                ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }

    /**
     * WINDOW DEF that specifies previous window should not specify a partition
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCopyAndModifyWindowDefinitionWithPartitionFails() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Person.class, "per")
                .window("x").partitionBy("per.age").end()
                .select("per.age")
                .select("SUM(per.age) OVER (x PARTITION BY per.id)")
                .orderByAsc("per.age")
                ;

        List<Tuple> resultList = criteria.getResultList();
        assertNotNull(resultList);
    }
}
