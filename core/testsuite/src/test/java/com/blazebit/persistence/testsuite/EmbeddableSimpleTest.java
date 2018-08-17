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

import javax.persistence.Tuple;

import com.blazebit.persistence.PaginatedCriteriaBuilder;
import org.junit.Ignore;
import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.entity.Order;
import com.blazebit.persistence.testsuite.entity.OrderPosition;
import com.blazebit.persistence.testsuite.entity.OrderPositionHead;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class EmbeddableSimpleTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            Order.class,
            OrderPosition.class,
            OrderPositionHead.class
        };
    }
    
    @Test
    public void testEmbeddedPropertySelect() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class);
        criteria.from(OrderPosition.class, "pos");
        criteria.select("pos.id.position");
        criteria.getResultList();
    }
    
    @Test
    public void testEmbeddedPropertyWhere() {
        CriteriaBuilder<OrderPosition> criteria = cbf.create(em, OrderPosition.class);
        criteria.from(OrderPosition.class, "pos");
        criteria.where("pos.id.position").eq(1);
        criteria.getResultList();
    }
    
    @Test
    public void testEmbeddedPropertyOrderBy() {
        CriteriaBuilder<OrderPosition> criteria = cbf.create(em, OrderPosition.class);
        criteria.from(OrderPosition.class, "pos");
        criteria.orderByAsc("pos.id.position");
        criteria.getResultList();
    }
    
    @Test
    public void testEmbeddedIdPagination() {
        PaginatedCriteriaBuilder<OrderPosition> criteria = cbf.create(em, OrderPosition.class, "pos").orderByAsc("pos.id.position").orderByAsc("pos.id").page(0, 1);
        String expectedCountQuery =
                "SELECT " + countPaginated("pos.id.orderId, pos.id.position", false) + " "
                + "FROM OrderPosition pos";
        assertEquals(expectedCountQuery, criteria.getPageCountQueryString());
    }

    // TODO: not yet implemented
    @Test
    @Ignore("This is still causing problems in the JoinManager because of the cyclic dependency")
    public void testCyclicDependencyInOnClauseImplicitJoin() {
        CriteriaBuilder<Order> criteria = cbf.create(em, Order.class, "o");
        criteria.leftJoinDefaultOn("o.orderPositions", "position")
                .on("position.head.number").in(1, 2, 3)
            .end();
        criteria.getResultList();
    }
}
