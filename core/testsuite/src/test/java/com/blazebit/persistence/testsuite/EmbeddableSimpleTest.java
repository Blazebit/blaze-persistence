/*
 * Copyright 2014 - 2020 Blazebit.
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
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus4;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.entity.Order;
import com.blazebit.persistence.testsuite.entity.OrderPosition;
import com.blazebit.persistence.testsuite.entity.OrderPositionElement;
import com.blazebit.persistence.testsuite.entity.OrderPositionHead;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.Tuple;

import static org.junit.Assert.assertEquals;

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
            OrderPositionHead.class,
            OrderPositionElement.class
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

    @Test
    public void testCyclicDependencyInOnClauseImplicitJoin() {
        CriteriaBuilder<Order> criteria = cbf.create(em, Order.class, "o");
        criteria.leftJoinDefaultOn("o.orderPositions", "p")
                .on("p.head.number").in(1, 2, 3)
            .end();
        criteria.getResultList();
    }

    @Test
    // Prior to Hibernate 5.1 it wasn't possible reference other from clause elements in the ON clause which is required to support implicit joins in ON clauses
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class })
    public void testCyclicDependencyInOnClauseImplicitJoin2() {
        CriteriaBuilder<OrderPosition> criteria = cbf.create(em, OrderPosition.class, "p");
        criteria.leftJoinDefaultOn("p.order", "o")
                    .on("o.orderPositions.head.number").gt(2)
                .end();
        criteria.getResultList();
    }

    @Test
    // Prior to Hibernate 5.1 it wasn't possible reference other from clause elements in the ON clause which is required to support implicit joins in ON clauses
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoEclipselink.class })
    public void testCyclicDependencyInOnClauseImplicitJoin3() {
        CriteriaBuilder<OrderPosition> criteria = cbf.create(em, OrderPosition.class, "p");
        criteria.leftJoinDefaultOn("p.order", "o")
                    .on("TREAT(o AS Order).orderPositions.head.number").gt(2)
                .end();
        criteria.getResultList();
    }

    @Test
    // Prior to Hibernate 5.1 it wasn't possible reference other from clause elements in the ON clause which is required to support implicit joins in ON clauses
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoEclipselink.class  })
    public void testCyclicDependencyInOnClauseImplicitJoin4() {
        CriteriaBuilder<OrderPosition> criteria = cbf.create(em, OrderPosition.class, "p");
        criteria.leftJoinDefaultOn("p.order", "o")
                    .on("TREAT(o.orderPositions AS OrderPosition).head.number").gt(2)
                .end();
        criteria.getResultList();
    }

    @Test
    // Prior to Hibernate 5.1 it wasn't possible reference other from clause elements in the ON clause which is required to support implicit joins in ON clauses
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class })
    public void testCyclicDependencyInOnClauseImplicitJoin5() {
        CriteriaBuilder<Order> criteria = cbf.create(em, Order.class, "o");
        criteria.leftJoinDefaultOn("o.orderPositions", "p")
                    .on("o.orderPositions.id.position").gt(2)
                .end();
        criteria.getResultList();
    }

    @Test
    // Prior to Hibernate 5.1 it wasn't possible reference other from clause elements in the ON clause which is required to support implicit joins in ON clauses
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class })
    public void testCyclicDependencyInOnClauseImplicitJoin6() {
        CriteriaBuilder<Order> criteria = cbf.create(em, Order.class, "o");
        criteria.leftJoinDefaultOn("o.orderPositions", "p")
                    .on("o.orderPositions.head.number").gt(2)
                .end();
        criteria.getResultList();
    }

    @Test
    // Prior to Hibernate 5.1 it wasn't possible reference other from clause elements in the ON clause which is required to support implicit joins in ON clauses
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus4.class })
    public void testCyclicDependencyInOnClauseImplicitJoin7() {
        CriteriaBuilder<Order> criteria = cbf.create(em, Order.class, "o");
        criteria.leftJoinDefault("o.orderPositions", "p")
                .leftJoinOn("p", Order.class, "o2")
                    .on("o2.id").eqExpression("o.orderPositions.id.orderId")
                .end();
        assertEquals(
                "SELECT o FROM Order o " +
                        "LEFT JOIN o.orderPositions p " +
                        "LEFT JOIN Order o2 ON (o2.id = p.id.orderId)",
                criteria.getQueryString()
        );
        criteria.getResultList();
    }

    @Test
    // Prior to Hibernate 5.1 it wasn't possible reference other from clause elements in the ON clause which is required to support implicit joins in ON clauses
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus4.class })
    public void testCyclicDependencyInOnClauseImplicitJoin8() {
        CriteriaBuilder<Order> criteria = cbf.create(em, Order.class, "o");
        criteria.leftJoinDefault("o.orderPositions", "p")
                .leftJoinOn("p", Order.class, "o2")
                    .on("o2.number").eqExpression("o.orderPositions.order.number")
                .end();
        assertEquals(
                "SELECT o FROM Order o " +
                        "LEFT JOIN o.orderPositions p " +
                        "LEFT JOIN Order o2 ON (EXISTS (" +
                        "SELECT 1 FROM p.order _synth_subquery_0 " +
                        "WHERE o2.number = _synth_subquery_0.number" +
                        "))",
                criteria.getQueryString()
        );
        criteria.getResultList();
    }

    @Test
    // Prior to Hibernate 5.1 it wasn't possible reference other from clause elements in the ON clause which is required to support implicit joins in ON clauses
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus4.class })
    public void testCyclicDependencyInOnClauseImplicitJoin9() {
        CriteriaBuilder<Order> criteria = cbf.create(em, Order.class, "o");
        criteria.leftJoinDefault("o.orderPositions", "p")
                .leftJoinDefault("p.order", "o1")
                .leftJoinOn("o1", Order.class, "o2")
                    .on("o2.number").eqExpression("o.orderPositions.order.number")
                .end();
        assertEquals(
                "SELECT o FROM Order o " +
                        "LEFT JOIN o.orderPositions p " +
                        "LEFT JOIN p.order o1 " +
                        "LEFT JOIN Order o2 ON (o2.number = o1.number)",
                criteria.getQueryString()
        );
        criteria.getResultList();
    }

    @Test
    // Prior to Hibernate 5.1 it wasn't possible reference other from clause elements in the ON clause which is required to support implicit joins in ON clauses
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus4.class })
    public void testCyclicDependencyInOnClauseImplicitJoin10() {
        CriteriaBuilder<Order> criteria = cbf.create(em, Order.class, "o");
        criteria.leftJoinDefaultOn("o.orderPositions", "p")
                    .on("p.id.position").gt(1)
                .end()
                .leftJoinOn("p", Order.class, "o2")
                    .on("o2.number").eqExpression("o.orderPositions.order.number")
                .end();
        String subquery;
        if (jpaProvider.needsCorrelationPredicateWhenCorrelatingWithWhereClause()) {
            subquery = "SELECT 1 FROM OrderPosition _synth_subquery_0 " +
                            "JOIN _synth_subquery_0.order order_1 " +
                            "WHERE _synth_subquery_0.order.id = o.id " +
                            "AND o2.number = order_1.number";
        } else {
            subquery = "SELECT 1 FROM o.orderPositions _synth_subquery_0 " +
                    "JOIN _synth_subquery_0.order order_1 " +
                    "WHERE o2.number = order_1.number";
        }
        assertEquals(
                "SELECT o FROM Order o " +
                        "LEFT JOIN o.orderPositions p ON (p.id.position > :param_0) " +
                        "LEFT JOIN Order o2 ON (EXISTS (" +
                        subquery +
                        "))",
                criteria.getQueryString()
        );
        criteria.getResultList();
    }

    @Test
    // Prior to Hibernate 5.1 it wasn't possible reference other from clause elements in the ON clause which is required to support implicit joins in ON clauses
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus4.class })
    public void testCyclicDependencyInOnClauseImplicitJoin11() {
        CriteriaBuilder<Order> criteria = cbf.create(em, Order.class, "o");
        criteria.leftJoinDefaultOn("o.orderPositions", "p")
                    .on("p.id.position").gt(1)
                .end()
                .leftJoinOn("p", Order.class, "o2")
                    .on("o2.number").eqExpression("o.orderPositions.order.number")
                    .on("o2.number").eqExpression("o.orderPositions.order.number2")
                .end();
        String subquery;
        if (jpaProvider.needsCorrelationPredicateWhenCorrelatingWithWhereClause()) {
            subquery = "SELECT 1 FROM OrderPosition _synth_subquery_0 " +
                    "JOIN _synth_subquery_0.order order_1 " +
                    "WHERE _synth_subquery_0.order.id = o.id " +
                    "AND o2.number = order_1.number AND o2.number = order_1.number2";
        } else {
            subquery = "SELECT 1 FROM o.orderPositions _synth_subquery_0 " +
                    "JOIN _synth_subquery_0.order order_1 " +
                    "WHERE o2.number = order_1.number AND o2.number = order_1.number2";
        }
        assertEquals(
                "SELECT o FROM Order o " +
                        "LEFT JOIN o.orderPositions p ON (p.id.position > :param_0) " +
                        "LEFT JOIN Order o2 ON (EXISTS (" +
                        subquery +
                        "))",
                criteria.getQueryString()
        );
        criteria.getResultList();
    }

    @Test
    // Prior to Hibernate 5.1 it wasn't possible reference other from clause elements in the ON clause which is required to support implicit joins in ON clauses
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus4.class })
    public void testCyclicDependencyInOnClauseImplicitJoinInDisjunction() {
        CriteriaBuilder<Order> criteria = cbf.create(em, Order.class, "o");
        criteria.leftJoinDefaultOn("o.orderPositions", "p")
                .on("p.id.position").gt(1)
                .end()
                .leftJoinOn("p", Order.class, "o2")
                    .onOr()
                        .on("o2.number").eqExpression("o.orderPositions.order.number")
                        .on("o2.number").isNull()
                    .endOr()
                .end();
        assertEquals(
                "SELECT o FROM Order o " +
                        "LEFT JOIN o.orderPositions p ON (p.id.position > :param_0) " +
                        "LEFT JOIN Order o2 ON (EXISTS (" +
                        "SELECT 1 FROM Order _synthetic_o LEFT JOIN _synthetic_o.orderPositions _synth_subquery_0 " +
                        "LEFT JOIN _synth_subquery_0.order order_1 " +
                        "WHERE (o2.number = order_1.number " +
                        "OR o2.number IS NULL) AND _synthetic_o = o" +
                        "))",
                criteria.getQueryString()
        );
        criteria.getResultList();
    }
}
