/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence;

import com.blazebit.persistence.entity.Document;
import com.blazebit.persistence.entity.Order;
import com.blazebit.persistence.entity.OrderPosition;
import com.blazebit.persistence.entity.OrderPositionHead;
import com.blazebit.persistence.entity.OrderPositionId;
import com.blazebit.persistence.impl.expression.SyntaxErrorException;
import static com.googlecode.catchexception.CatchException.verifyException;
import javax.persistence.Tuple;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class EmbeddedTest extends AbstractCoreTest {

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
    public void testEmbeddedPropertyOn() {
        CriteriaBuilder<Order> criteria = cbf.create(em, Order.class, "order");
        criteria.where("order.number").in(1, 2, 3);
        criteria.leftJoinDefaultOn("order.orderPositions", "position")
//                .on("order.number").in(1, 2, 3)
                .on("position.head.number").in(1, 2, 3)
            .end();
        criteria.getResultList();
    }
}
