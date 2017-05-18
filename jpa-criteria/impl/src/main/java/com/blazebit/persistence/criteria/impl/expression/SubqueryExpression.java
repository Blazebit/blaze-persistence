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

package com.blazebit.persistence.criteria.impl.expression;

import com.blazebit.persistence.criteria.BlazeAbstractQuery;
import com.blazebit.persistence.criteria.BlazeCollectionJoin;
import com.blazebit.persistence.criteria.BlazeCommonAbstractCriteria;
import com.blazebit.persistence.criteria.BlazeJoin;
import com.blazebit.persistence.criteria.BlazeListJoin;
import com.blazebit.persistence.criteria.BlazeMapJoin;
import com.blazebit.persistence.criteria.BlazeOrder;
import com.blazebit.persistence.criteria.BlazeRoot;
import com.blazebit.persistence.criteria.BlazeSetJoin;
import com.blazebit.persistence.criteria.BlazeSubquery;
import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.InternalQuery;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.RenderContext;
import com.blazebit.persistence.criteria.impl.path.AbstractJoin;
import com.blazebit.persistence.criteria.impl.path.CollectionAttributeJoin;
import com.blazebit.persistence.criteria.impl.path.ListAttributeJoin;
import com.blazebit.persistence.criteria.impl.path.MapAttributeJoin;
import com.blazebit.persistence.criteria.impl.path.RootImpl;
import com.blazebit.persistence.criteria.impl.path.SetAttributeJoin;
import com.blazebit.persistence.criteria.impl.support.SubquerySupport;

import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.metamodel.EntityType;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SubqueryExpression<T> extends AbstractExpression<T> implements BlazeSubquery<T>, SubquerySupport<T>, Serializable {

    private static final long serialVersionUID = 1L;

    private final BlazeCommonAbstractCriteria parent;
    private final InternalQuery<T> query;

    public SubqueryExpression(BlazeCriteriaBuilderImpl criteriaBuilder, Class<T> javaType, BlazeCommonAbstractCriteria parent) {
        super(criteriaBuilder, javaType);
        this.parent = parent;
        this.query = new InternalQuery<T>(this, criteriaBuilder);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public BlazeAbstractQuery<?> getParent() {
        if (parent instanceof BlazeAbstractQuery<?>) {
            return (BlazeAbstractQuery<T>) parent;
        }

        throw new IllegalStateException("Update or delete criteria do not have a parent, but a containing query!");
    }

    @Override
    public Class<T> getResultType() {
        return getJavaType();
    }

    /* Select */

    @Override
    public boolean isDistinct() {
        return query.isDistinct();
    }

    @Override
    public BlazeSubquery<T> distinct(boolean distinct) {
        query.setDistinct(distinct);
        return this;
    }

    @Override
    public BlazeSubquery<T> select(Expression<T> selection) {
        query.setSelection(criteriaBuilder.wrapSelection(selection));
        return this;
    }

    @Override
    public Expression<T> getSelection() {
        return (Expression<T>) query.getSelection();
    }

    /* From */

    @Override
    public Set<Root<?>> getRoots() {
        return query.getRoots();
    }

    @Override
    public Set<BlazeRoot<?>> getBlazeRoots() {
        return query.getBlazeRoots();
    }

    @Override
    public <X> BlazeRoot<X> from(Class<X> entityClass) {
        return query.from(entityClass, null);
    }

    @Override
    public <X> BlazeRoot<X> from(EntityType<X> entityType) {
        return query.from(entityType, null);
    }

    @Override
    public <X> BlazeRoot<X> from(Class<X> entityClass, String alias) {
        return query.from(entityClass, alias);
    }

    @Override
    public <X> BlazeRoot<X> from(EntityType<X> entityType, String alias) {
        return query.from(entityType, alias);
    }

    /* Correlations */

    @Override
    public Set<Join<?, ?>> getCorrelatedJoins() {
        return query.collectCorrelatedJoins();
    }

    @Override
    public <Y> BlazeRoot<Y> correlate(Root<Y> source) {
        final RootImpl<Y> correlation = ((RootImpl<Y>) source).correlateTo(this);
        query.addCorrelationRoot(correlation);
        return correlation;
    }

    @Override
    public <X, Y> BlazeJoin<X, Y> correlate(Join<X, Y> source) {
        final AbstractJoin<X, Y> correlation = ((AbstractJoin<X, Y>) source).correlateTo(this);
        query.addCorrelationRoot(correlation);
        return correlation;
    }

    @Override
    public <X, Y> BlazeCollectionJoin<X, Y> correlate(CollectionJoin<X, Y> source) {
        final CollectionAttributeJoin<X, Y> correlation = ((CollectionAttributeJoin<X, Y>) source).correlateTo(this);
        query.addCorrelationRoot(correlation);
        return correlation;
    }

    @Override
    public <X, Y> BlazeSetJoin<X, Y> correlate(SetJoin<X, Y> source) {
        final SetAttributeJoin<X, Y> correlation = ((SetAttributeJoin<X, Y>) source).correlateTo(this);
        query.addCorrelationRoot(correlation);
        return correlation;
    }

    @Override
    public <X, Y> BlazeListJoin<X, Y> correlate(ListJoin<X, Y> source) {
        final ListAttributeJoin<X, Y> correlation = ((ListAttributeJoin<X, Y>) source).correlateTo(this);
        query.addCorrelationRoot(correlation);
        return correlation;
    }

    @Override
    public <X, K, V> BlazeMapJoin<X, K, V> correlate(MapJoin<X, K, V> source) {
        final MapAttributeJoin<X, K, V> correlation = ((MapAttributeJoin<X, K, V>) source).correlateTo(this);
        query.addCorrelationRoot(correlation);
        return correlation;
    }

    /* Where */

    @Override
    public Predicate getRestriction() {
        return query.getRestriction();
    }

    @Override
    public BlazeSubquery<T> where(Expression<Boolean> restriction) {
        query.setRestriction(restriction == null ? null : criteriaBuilder.wrap(restriction));
        return this;
    }

    @Override
    public BlazeSubquery<T> where(Predicate... restrictions) {
        if (restrictions == null || restrictions.length == 0) {
            query.setRestriction(null);
        } else {
            query.setRestriction(criteriaBuilder.and(restrictions));
        }
        return this;
    }

    /* Group by */

    @Override
    public List<Expression<?>> getGroupList() {
        return query.getGroupList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public BlazeSubquery<T> groupBy(Expression<?>... groupings) {
        if (groupings == null || groupings.length == 0) {
            query.setGroupList(Collections.EMPTY_LIST);
        } else {
            query.setGroupList(Arrays.asList(groupings));
        }

        return this;
    }

    @Override
    public BlazeSubquery<T> groupBy(List<Expression<?>> groupings) {
        query.setGroupList(groupings);
        return this;
    }

    /* Having */

    @Override
    public Predicate getGroupRestriction() {
        return query.getGroupRestriction();
    }

    @Override
    public BlazeSubquery<T> having(Expression<Boolean> restriction) {
        query.setHaving(criteriaBuilder.wrap(restriction));
        return this;
    }

    @Override
    public BlazeSubquery<T> having(Predicate... restrictions) {
        query.setHaving(criteriaBuilder.and(restrictions));
        return this;
    }

    /* Order by */

    @Override
    public List<BlazeOrder> getBlazeOrderList() {
        return query.getBlazeOrderList();
    }

    @Override
    public List<Order> getOrderList() {
        return query.getOrderList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public BlazeSubquery<T> orderBy(Order... orders) {
        if (orders == null || orders.length == 0) {
            query.setOrderList(Collections.EMPTY_LIST);
        } else {
            query.setOrderList(Arrays.asList(orders));
        }

        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public BlazeSubquery<T> orderBy(BlazeOrder... orders) {
        if (orders == null || orders.length == 0) {
            query.setOrderList(Collections.EMPTY_LIST);
        } else {
            query.setBlazeOrderList(Arrays.asList(orders));
        }

        return this;
    }

    @Override
    public BlazeSubquery<T> orderBy(List<BlazeOrder> orderList) {
        query.setBlazeOrderList(orderList);
        return this;
    }

    /* Parameters */

    @Override
    public Set<ParameterExpression<?>> getParameters() {
        return query.getParameters();
    }

    /* Subquery */

    @Override
    public <U> BlazeSubquery<U> subquery(Class<U> type) {
        return query.subquery(type);
    }

    @Override
    public void visitParameters(ParameterVisitor visitor) {
        for (ParameterExpression<?> param : query.getParameters()) {
            visitor.add(param);
        }
    }

    @Override
    public void render(RenderContext context) {
        StringBuilder buffer = context.getBuffer();
        buffer.append(context.generateSubqueryAlias(query));
    }

    public void renderSubquery(RenderContext context) {
        query.renderSubquery(context);
    }

    /* JPA 2.1 support */

    @Override
    public BlazeCommonAbstractCriteria getContainingQuery() {
        return parent;
    }

}
