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

package com.blazebit.persistence.criteria.impl;

import com.blazebit.persistence.criteria.BlazeCTECriteria;
import com.blazebit.persistence.criteria.BlazeOrder;
import com.blazebit.persistence.criteria.BlazeRoot;
import com.blazebit.persistence.criteria.BlazeSubquery;
import com.blazebit.persistence.criteria.impl.expression.AbstractExpression;
import com.blazebit.persistence.criteria.impl.expression.SubqueryExpression;
import com.blazebit.persistence.criteria.impl.path.AbstractFrom;
import com.blazebit.persistence.criteria.impl.path.AbstractPath;
import com.blazebit.persistence.criteria.impl.path.RootImpl;
import com.blazebit.persistence.criteria.impl.path.SingularAttributePath;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BlazeCTECriteriaImpl<T> implements BlazeCTECriteria<T> {

    private final Class<T> returnType;
    private final BlazeCriteriaBuilderImpl criteriaBuilder;
    private final List<Assignment> assignments = new ArrayList<Assignment>();
    private final Root<T> path;

    private boolean distinct;
    private Selection<? extends T> selection;
    private final Set<RootImpl<?>> roots = new LinkedHashSet<>();
    private Set<AbstractFrom<?, ?>> correlationRoots;
    private Predicate restriction;
    private List<Expression<?>> groupList = Collections.emptyList();
    private Predicate having;
    private List<BlazeOrder> orderList = Collections.emptyList();
    private List<Subquery<?>> subqueries;

    public BlazeCTECriteriaImpl(BlazeCriteriaBuilderImpl criteriaBuilder, Class<T> returnType) {
        this.returnType = returnType;
        this.criteriaBuilder = criteriaBuilder;
        EntityType<T> entityType = criteriaBuilder.getEntityMetamodel().entity(returnType);
        this.path = new RootImpl<T>(criteriaBuilder, entityType, null, false);
    }

    @Override
    public <X> BlazeCTECriteria<X> with(Class<X> clasz) {
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<Root<?>> getRoots() {
        return (Set<Root<?>>) (Set<?>) roots;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<BlazeRoot<?>> getBlazeRoots() {
        return (Set<BlazeRoot<?>>) (Set<?>) roots;    }

    @Override
    public <X> BlazeRoot<X> from(Class<X> entityClass) {
        return from(entityClass, null);
    }

    @Override
    public <X> BlazeRoot<X> from(EntityType<X> entityType) {
        return from(entityType, null);
    }

    @Override
    public <X> BlazeRoot<X> from(Class<X> entityClass, String alias) {
        EntityType<X> entityType = criteriaBuilder.getEntityMetamodel().entity(entityClass);
        if (entityType == null) {
            throw new IllegalArgumentException(entityClass + " is not an entity");
        }
        return from(entityType, alias);
    }

    @Override
    public <X> BlazeRoot<X> from(EntityType<X> entityType, String alias) {
        RootImpl<X> root = new RootImpl<X>(criteriaBuilder, entityType, alias, true);
        roots.add(root);
        return root;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Selection<T> getSelection() {
        return (Selection<T>) selection;
    }

    @Override
    public Class<T> getResultType() {
        return returnType;
    }

    @Override
    public BlazeCTECriteria<T> set(String attributeName, Object value) {
        final Path<?> attributePath = path.get(attributeName);
        return internalSet(attributePath, valueExpression(attributePath, value));
    }

    @Override
    public <Y, X extends Y> BlazeCTECriteria<T> set(SingularAttribute<? super T, Y> attribute, X value) {
        Path<?> attributePath = path.get(attribute);
        return internalSet(attributePath, valueExpression(attributePath, value));
    }

    @Override
    public <Y> BlazeCTECriteria<T> set(SingularAttribute<? super T, Y> attribute, Expression<? extends Y> value) {
        Path<?> attributePath = path.get(attribute);
        return internalSet(attributePath, value);
    }

    @Override
    public <Y, X extends Y> BlazeCTECriteria<T> set(Path<Y> attribute, X value) {
        return internalSet(attribute, valueExpression(attribute, value));
    }

    @Override
    public <Y> BlazeCTECriteria<T> set(Path<Y> attribute, Expression<? extends Y> value) {
        return internalSet(attribute, value);
    }

    private BlazeCTECriteria<T> internalSet(Path<?> attribute, Expression<?> value) {
        if (!(attribute instanceof AbstractPath<?>)) {
            throw new IllegalArgumentException("Illegal custom attribute path: " + attribute.getClass().getName());
        }
        if (!(attribute instanceof SingularAttributePath<?>)) {
            throw new IllegalArgumentException("Only singular attributes can be updated");
        }
        if (value == null) {
            throw new IllegalArgumentException("Illegal null expression passed. Check your set-call, you probably wanted to pass a literal null");
        }
        if (!(value instanceof AbstractExpression<?>)) {
            throw new IllegalArgumentException("Illegal custom value expression: " + value.getClass().getName());
        }
        assignments.add(new Assignment((SingularAttributePath<?>) attribute, (AbstractExpression<?>) value));
        return this;
    }

    private AbstractExpression<?> valueExpression(Path<?> attributePath, Object value) {
        if (value == null) {
            return criteriaBuilder.nullLiteral(attributePath.getJavaType());
        } else if (value instanceof AbstractExpression<?>) {
            return (AbstractExpression<?>) value;
        } else {
            return criteriaBuilder.literal(value);
        }
    }

    /* Where */

    @Override
    public Predicate getRestriction() {
        return restriction;
    }

    @Override
    public BlazeCTECriteriaImpl<T> where(Expression<Boolean> restriction) {
        this.restriction = restriction == null ? null : criteriaBuilder.wrap(restriction);
        return this;
    }

    @Override
    public BlazeCTECriteria<T> where(Predicate... restrictions) {
        if (restrictions == null || restrictions.length == 0) {
            this.restriction = null;
        } else {
            this.restriction = criteriaBuilder.and(restrictions);
        }
        return this;
    }

    /* Group by */

    @Override
    public List<Expression<?>> getGroupList() {
        return groupList;
    }

    @Override
    @SuppressWarnings("unchecked")
    public BlazeCTECriteria<T> groupBy(Expression<?>... groupings) {
        if (groupings == null || groupings.length == 0) {
            groupList = Collections.EMPTY_LIST;
        } else {
            groupList = Arrays.asList(groupings);
        }

        return this;
    }

    @Override
    public BlazeCTECriteria<T> groupBy(List<Expression<?>> groupings) {
        groupList = groupings;
        return this;
    }

    /* Having */

    @Override
    public Predicate getGroupRestriction() {
        return having;
    }

    @Override
    public BlazeCTECriteria<T> having(Expression<Boolean> restriction) {
        if (restriction == null) {
            having = null;
        } else {
            having = criteriaBuilder.wrap(restriction);
        }
        return this;
    }

    @Override
    public BlazeCTECriteria<T> having(Predicate... restrictions) {
        if (restrictions == null || restrictions.length == 0) {
            having = null;
        } else {
            having = criteriaBuilder.and(restrictions);
        }
        return this;
    }

    @Override
    public boolean isDistinct() {
        return distinct;
    }

    @Override
    public BlazeCTECriteria<T> distinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    public List<Subquery<?>> internalGetSubqueries() {
        if (subqueries == null) {
            subqueries = new ArrayList<Subquery<?>>();
        }
        return subqueries;
    }

    @Override
    public <U> BlazeSubquery<U> subquery(Class<U> type) {
        SubqueryExpression<U> subquery = new SubqueryExpression<U>(criteriaBuilder, type, this);
        internalGetSubqueries().add(subquery);
        return subquery;
    }

    @Override
    public List<BlazeOrder> getBlazeOrderList() {
        return orderList;
    }

    @Override
    public BlazeCTECriteria<T> orderBy(Order... orders) {
        if (orders == null || orders.length == 0) {
            orderList = Collections.EMPTY_LIST;
        } else {
            orderList = (List<BlazeOrder>) (List<?>) Arrays.asList(orders);
        }

        return this;
    }

    @Override
    public BlazeCTECriteria<T> orderBy(List<Order> orderList) {
        this.orderList = (List<BlazeOrder>) (List<?>) orderList;
        return null;
    }

    @Override
    public Set<ParameterExpression<?>> getParameters() {
        // NOTE: we have to always visit them because it's not possible to cache that easily
        ParameterVisitor visitor = new ParameterVisitor();

        visitor.visit(selection);
        visitor.visit(restriction);
        if (subqueries != null) {
            for (Subquery<?> subquery : subqueries) {
                visitor.visit(subquery);
            }
        }

        for (RootImpl<?> r : roots) {
            r.visit(visitor);
        }
        for (AbstractFrom<?, ?> r : correlationRoots) {
            r.visit(visitor);
        }

        visitor.visit(having);
        if (groupList != null) {
            for (Expression<?> grouping : groupList) {
                visitor.visit(grouping);
            }
        }
        if (orderList != null) {
            for (Order ordering : orderList) {
                visitor.visit(ordering.getExpression());
            }
        }

        return visitor.getParameters();
    }

    @Override
    public Bindable<T> getModel() {
        return path.getModel();
    }

    @Override
    public <Y> Path<Y> get(SingularAttribute<? super T, Y> attribute) {
        return path.get(attribute);
    }

    @Override
    public <Y> Path<Y> get(String attributeName) {
        return path.get(attributeName);
    }

    /**
     * @since 1.4.0
     */
    private static final class Assignment {
        private final SingularAttributePath<?> attributePath;
        private final AbstractExpression<?> valueExpression;

        public Assignment(SingularAttributePath<?> attributePath, AbstractExpression<?> valueExpression) {
            this.attributePath = attributePath;
            this.valueExpression = valueExpression;
        }
    }
}
