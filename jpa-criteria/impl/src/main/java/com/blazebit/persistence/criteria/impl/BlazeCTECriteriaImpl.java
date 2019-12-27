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

import com.blazebit.persistence.BaseSubqueryBuilder;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.FromBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.FullSelectCTECriteriaBuilder;
import com.blazebit.persistence.GroupByBuilder;
import com.blazebit.persistence.HavingBuilder;
import com.blazebit.persistence.JoinOnBuilder;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.OrderByBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.WhereBuilder;
import com.blazebit.persistence.criteria.BlazeCTECriteria;
import com.blazebit.persistence.criteria.BlazeJoin;
import com.blazebit.persistence.criteria.BlazeOrder;
import com.blazebit.persistence.criteria.BlazeRoot;
import com.blazebit.persistence.criteria.BlazeSubquery;
import com.blazebit.persistence.criteria.impl.expression.AbstractExpression;
import com.blazebit.persistence.criteria.impl.expression.AbstractSelection;
import com.blazebit.persistence.criteria.impl.expression.LiteralExpression;
import com.blazebit.persistence.criteria.impl.expression.SubqueryExpression;
import com.blazebit.persistence.criteria.impl.path.AbstractFrom;
import com.blazebit.persistence.criteria.impl.path.AbstractJoin;
import com.blazebit.persistence.criteria.impl.path.AbstractPath;
import com.blazebit.persistence.criteria.impl.path.EntityJoin;
import com.blazebit.persistence.criteria.impl.path.RootImpl;
import com.blazebit.persistence.criteria.impl.path.SingularAttributePath;
import com.blazebit.persistence.criteria.impl.path.TreatedPath;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
    public BlazeCTECriteria<T> bind(String attributeName, Object value) {
        final Path<?> attributePath = path.get(attributeName);
        return internalSet(attributePath, valueExpression(attributePath, value));
    }

    @Override
    public <Y, X extends Y> BlazeCTECriteria<T> bind(SingularAttribute<? super T, Y> attribute, X value) {
        Path<?> attributePath = path.get(attribute);
        return internalSet(attributePath, valueExpression(attributePath, value));
    }

    @Override
    public <Y> BlazeCTECriteria<T> bind(SingularAttribute<? super T, Y> attribute, Expression<? extends Y> value) {
        Path<?> attributePath = path.get(attribute);
        return internalSet(attributePath, value);
    }

    @Override
    public <Y, X extends Y> BlazeCTECriteria<T> bind(Path<Y> attribute, X value) {
        return internalSet(attribute, valueExpression(attribute, value));
    }

    @Override
    public <Y> BlazeCTECriteria<T> bind(Path<Y> attribute, Expression<? extends Y> value) {
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

    public <X> CriteriaBuilder<X> render(CriteriaBuilder<X> cbs) {

        FullSelectCTECriteriaBuilder<CriteriaBuilder<X>> fullSelectCTECriteriaBuilder = cbs.with(returnType);
        RenderContextImpl context = new RenderContextImpl();

        renderFrom(fullSelectCTECriteriaBuilder, context);
        renderWhere(fullSelectCTECriteriaBuilder, context);
        renderGroupBy(fullSelectCTECriteriaBuilder, context);
        renderHaving(fullSelectCTECriteriaBuilder, context);
        renderOrderBy(fullSelectCTECriteriaBuilder, context);

        context.setClauseType(RenderContext.ClauseType.SELECT);

        for (Assignment a : assignments) {
            // TODO apply this for AbstractModificationCriteriaQuery as well
            String attribute = a.attributePath.getPathExpression().substring(String.valueOf(path.getAlias()).length() + 1);

            context.getBuffer().setLength(0);
            a.valueExpression.render(context);
            String valueExpression = context.takeBuffer();

            if (a.valueExpression instanceof LiteralExpression<?>) {
                fullSelectCTECriteriaBuilder.bind(attribute).select(valueExpression);
            } else {
                Map<String, InternalQuery<?>> aliasToSubqueries = context.takeAliasToSubqueryMap();

                if (aliasToSubqueries.isEmpty()) {
                    fullSelectCTECriteriaBuilder.bind(attribute).select(valueExpression);
                } else {
                    MultipleSubqueryInitiator<FullSelectCTECriteriaBuilder<CriteriaBuilder<X>>> initiator = fullSelectCTECriteriaBuilder.bind(attribute).selectSubqueries(valueExpression);

                    for (Map.Entry<String, InternalQuery<?>> subqueryEntry : aliasToSubqueries.entrySet()) {
                        context.pushSubqueryInitiator(initiator.with(subqueryEntry.getKey()));
                        subqueryEntry.getValue().renderSubquery(context);
                        context.popSubqueryInitiator();
                    }

                    initiator.end();
                }
            }
        }

        for (ImplicitParameterBinding b : context.getImplicitParameterBindings()) {
            b.bind(fullSelectCTECriteriaBuilder);
        }

        for (Map.Entry<String, ParameterExpression<?>> entry : context.getExplicitParameterNameMapping().entrySet()) {
            fullSelectCTECriteriaBuilder.setParameterType(entry.getKey(), entry.getValue().getParameterType());
        }

        return fullSelectCTECriteriaBuilder.end();
    }


    private void renderFrom(FromBuilder<?> cb, RenderContextImpl context) {
        context.setClauseType(RenderContext.ClauseType.FROM);

        for (BlazeRoot<?> r : roots) {
            ((AbstractFrom<?, ?>) r).prepareAlias(context);
            if (r.getAlias() != null) {
                cb.from(r.getModel(), r.getAlias());
            } else {
                cb.from(r.getModel());
            }
        }

        for (RootImpl<?> r : roots) {
            renderJoins(cb, context, r, true);
        }
    }

    @SuppressWarnings("unchecked")
    private void renderJoins(FromBuilder<?> cb, RenderContextImpl context, AbstractFrom<?, ?> r, boolean fetching) {
        String path;
        if (r.getAlias() != null) {
            path = r.getAlias();
        } else {
            path = "";
        }

        renderJoins(cb, null, true, context, path, (Set<BlazeJoin<?, ?>>) (Set<?>) r.getBlazeJoins());
        Collection<TreatedPath<?>> treatedPaths = (Collection<TreatedPath<?>>) (Collection) r.getTreatedPaths();
        if (treatedPaths != null && treatedPaths.size() > 0) {
            for (TreatedPath<?> treatedPath : treatedPaths) {
                RootImpl<?> treatedRoot = (RootImpl<?>) treatedPath;
                String treatedParentPath = "TREAT(" + path + " AS " + treatedPath.getTreatType().getName() + ')';
                renderJoins(cb, null, fetching, context, treatedParentPath, (Set<BlazeJoin<?, ?>>) (Set<?>) treatedRoot.getBlazeJoins());
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private SubqueryBuilder<?> renderSubqueryFrom(SubqueryInitiator<?> initiator, RenderContextImpl context) {
        SubqueryBuilder<?> cb = null;
        context.setClauseType(RenderContext.ClauseType.FROM);

        for (RootImpl<?> r : roots) {
            r.prepareAlias(context);
            if (cb == null) {
                if (r.getAlias() != null) {
                    cb = initiator.from(r.getJavaType(), r.getAlias());
                } else {
                    cb = initiator.from(r.getJavaType());
                }
            } else {
                if (r.getAlias() != null) {
                    cb.from(r.getJavaType(), r.getAlias());
                } else {
                    cb.from(r.getJavaType());
                }
            }
        }

        if (correlationRoots != null) {
            for (AbstractFrom<?, ?> r : correlationRoots) {
                r.prepareAlias(context);
                Set<BlazeJoin<?, ?>> joins = (Set<BlazeJoin<?, ?>>) (Set<?>) r.getBlazeJoins();

                for (BlazeJoin<?, ?> j : joins) {
                    AbstractJoin<?, ?> join = (AbstractJoin<?, ?>) j;
                    join.prepareAlias(context);
                    EntityType<?> treatJoinType = join.getTreatJoinType();
                    String path = getPath(r.getAlias(), j, treatJoinType);
                    if (j.getAttribute() != null && j.getAttribute().getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {
                        cb = (SubqueryBuilder<?>) renderJoins(cb, initiator, false, context, path, (Set<BlazeJoin<?, ?>>) (Set<?>) j.getBlazeJoins());
                    } else {
                        if (cb == null) {
                            if (j.getAlias() != null) {
                                cb = initiator.from(path, j.getAlias());
                            } else {
                                cb = initiator.from(path);
                            }
                        } else {
                            if (j.getAlias() != null) {
                                cb.from(path, j.getAlias());
                            } else {
                                cb.from(path);
                            }
                        }
                    }
                }
            }
        }

        for (RootImpl<?> r : roots) {
            renderJoins(cb, context, r, false);
        }

        if (correlationRoots != null) {
            for (AbstractFrom<?, ?> r : correlationRoots) {
                Set<BlazeJoin<?, ?>> joins = (Set<BlazeJoin<?, ?>>) (Set<?>) r.getBlazeJoins();

                for (BlazeJoin<?, ?> j : joins) {
                    // We already rendered correlation joins for embedded paths
                    if (j.getAttribute() != null && j.getAttribute().getPersistentAttributeType() != Attribute.PersistentAttributeType.EMBEDDED) {
                        renderJoins(cb, context, (AbstractFrom<?, ?>) j, false);
                    }
                }
            }
        }

        return cb;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private FromBuilder<?> renderJoins(FromBuilder<?> cb, SubqueryInitiator<?> subqueryInitiator, boolean fetching, RenderContextImpl context, String parentPath, Set<BlazeJoin<?, ?>> joins) {
        if (joins.isEmpty()) {
            return cb;
        }

        for (BlazeJoin<?, ?> j : joins) {
            AbstractJoin<?, ?> join = (AbstractJoin<?, ?>) j;
            EntityType<?> treatJoinType = join.getTreatJoinType();
            join.prepareAlias(context);
            // TODO: implicit joins?
            String path = getPath(parentPath, j, treatJoinType);
            String alias = j.getAlias();
            JoinOnBuilder<?> onBuilder = null;

            // "Join" relations in embeddables
            if (j.getAttribute() != null && j.getAttribute().getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {
                alias = path;
            } else {
                if (j.getOn() != null) {
                    if (fetching && j.isFetch()) {
                        throw new IllegalArgumentException("Fetch joining with on-condition is not allowed!" + j);
                    } else if (j instanceof EntityJoin<?, ?>) {
                        onBuilder = cb.joinOn(path, (EntityType<?>) j.getModel(), alias, getJoinType(j.getJoinType()));
                    } else {
                        onBuilder = cb.joinOn(path, alias, getJoinType(j.getJoinType()));
                    }
                } else {
                    if (fetching && j.isFetch()) {
                        ((FullQueryBuilder<?, ?>) cb).join(path, alias, getJoinType(j.getJoinType()), true);
                    } else if (j instanceof EntityJoin<?, ?>) {
                        throw new IllegalArgumentException("Entity join without on-condition is not allowed! " + j);
                    } else if (cb == null) {
                        cb = subqueryInitiator.from(path, alias);
                    } else if (cb instanceof BaseSubqueryBuilder<?> && j.isCorrelated()) {
                        ((SubqueryBuilder<?>) cb).from(path, alias);
                    } else {
                        cb.join(path, alias, getJoinType(j.getJoinType()));
                    }
                }
            }

            if (onBuilder != null) {
                context.setClauseType(RenderContext.ClauseType.ON);
                context.getBuffer().setLength(0);
                ((AbstractSelection<?>) j.getOn()).render(context);
                String expression = context.takeBuffer();
                Map<String, InternalQuery<?>> aliasToSubqueries = context.takeAliasToSubqueryMap();

                if (aliasToSubqueries.isEmpty()) {
                    onBuilder.setOnExpression(expression);
                } else {
                    MultipleSubqueryInitiator<?> initiator = onBuilder.setOnExpressionSubqueries(expression);

                    for (Map.Entry<String, InternalQuery<?>> subqueryEntry : aliasToSubqueries.entrySet()) {
                        context.pushSubqueryInitiator(initiator.with(subqueryEntry.getKey()));
                        subqueryEntry.getValue().renderSubquery(context);
                        context.popSubqueryInitiator();
                    }

                    initiator.end();
                }
            }

            renderJoins(cb, null, fetching, context, alias, (Set<BlazeJoin<?, ?>>) (Set<?>) j.getBlazeJoins());

            Collection<TreatedPath<?>> treatedPaths = (Collection<TreatedPath<?>>) (Collection) join.getTreatedPaths();
            if (treatedPaths != null && treatedPaths.size() > 0) {
                for (TreatedPath<?> treatedPath : treatedPaths) {
                    AbstractJoin<?, ?> treatedJoin = (AbstractJoin<?, ?>) treatedPath;
                    String treatedParentPath = "TREAT(" + alias + " AS " + treatedPath.getTreatType().getName() + ')';
                    renderJoins(cb, null, fetching, context, treatedParentPath, (Set<BlazeJoin<?, ?>>) (Set<?>) treatedJoin.getBlazeJoins());
                }
            }
        }

        return cb;
    }

    private String getPath(String parentPath, BlazeJoin<?, ?> j, EntityType<?> treatJoinType) {
        if (j.getAttribute() == null) {
            return parentPath;
        }
        String path = j.getAttribute().getName();
        if (parentPath == null || parentPath.isEmpty()) {
            if (treatJoinType != null) {
                return "TREAT(" + path + " AS " + treatJoinType.getName() + ')';
            } else {
                return path;
            }
        }

        if (treatJoinType != null) {
            return "TREAT(" + parentPath + "." + path + " AS " + treatJoinType.getName() + ')';
        } else {
            return parentPath + "." + path;
        }
    }

    private JoinType getJoinType(javax.persistence.criteria.JoinType joinType) {
        switch (joinType) {
            case INNER:
                return JoinType.INNER;
            case LEFT:
                return JoinType.LEFT;
            case RIGHT:
                return JoinType.RIGHT;
            default:
                throw new IllegalArgumentException("Unsupported join type: " + joinType);
        }
    }


    protected void renderWhere(WhereBuilder<?> wb, RenderContextImpl context) {
        if (restriction == null) {
            return;
        }

        context.setClauseType(RenderContext.ClauseType.WHERE);
        context.getBuffer().setLength(0);
        ((AbstractSelection<?>) restriction).render(context);
        String expression = context.takeBuffer();
        Map<String, InternalQuery<?>> aliasToSubqueries = context.takeAliasToSubqueryMap();

        if (aliasToSubqueries.isEmpty()) {
            wb.setWhereExpression(expression);
        } else {
            MultipleSubqueryInitiator<?> initiator = wb.setWhereExpressionSubqueries(expression);

            for (Map.Entry<String, InternalQuery<?>> subqueryEntry : aliasToSubqueries.entrySet()) {
                context.pushSubqueryInitiator(initiator.with(subqueryEntry.getKey()));
                subqueryEntry.getValue().renderSubquery(context);
                context.popSubqueryInitiator();
            }

            initiator.end();
        }
    }


    private void renderTreatTypeRestrictions(RenderContextImpl context, List<TreatedPath<?>> treatedSelections) {
        final StringBuilder buffer = context.getBuffer();
        boolean first = buffer.length() == 0;

        for (TreatedPath<?> p : treatedSelections) {
            if (first) {
                first = false;
            } else {
                buffer.append(" AND ");
            }

            buffer.append("TYPE(")
                    .append(p.getAlias())
                    .append(") = ")
                    .append(p.getTreatType().getName());
        }
    }

    private void renderGroupBy(GroupByBuilder<?> gb, RenderContextImpl context) {
        if (groupList == null) {
            return;
        }

        context.setClauseType(RenderContext.ClauseType.GROUP_BY);
        for (Expression<?> expr : groupList) {
            context.getBuffer().setLength(0);
            ((AbstractSelection<?>) expr).render(context);
            String expression = context.takeBuffer();
            Map<String, InternalQuery<?>> aliasToSubqueries = context.takeAliasToSubqueryMap();

            if (aliasToSubqueries.isEmpty()) {
                gb.groupBy(expression);
            } else {
                throw new IllegalArgumentException("Subqueries are not supported in the group by clause!");
                //            MultipleSubqueryInitiator<?> initiator = gb.groupBySubqueries(expression);
                //
                //            for (Map.Entry<String, InternalQuery<?>> subqueryEntry : aliasToSubqueries.entrySet()) {
                //                context.pushSubqueryInitiator(initiator.with(subqueryEntry.getKey()));
                //                subqueryEntry.getValue().renderSubquery(context);
                //                context.popSubqueryInitiator();
                //            }
                //
                //            initiator.end();
            }
        }
    }

    private void renderHaving(HavingBuilder<?> hb, RenderContextImpl context) {
        if (having == null) {
            return;
        }

        context.setClauseType(RenderContext.ClauseType.HAVING);
        context.getBuffer().setLength(0);
        ((AbstractSelection<?>) having).render(context);
        String expression = context.takeBuffer();
        Map<String, InternalQuery<?>> aliasToSubqueries = context.takeAliasToSubqueryMap();

        if (aliasToSubqueries.isEmpty()) {
            hb.setHavingExpression(expression);
        } else {
            MultipleSubqueryInitiator<?> initiator = hb.setHavingExpressionSubqueries(expression);

            for (Map.Entry<String, InternalQuery<?>> subqueryEntry : aliasToSubqueries.entrySet()) {
                context.pushSubqueryInitiator(initiator.with(subqueryEntry.getKey()));
                subqueryEntry.getValue().renderSubquery(context);
                context.popSubqueryInitiator();
            }

            initiator.end();
        }
    }

    private void renderOrderBy(OrderByBuilder<?> ob, RenderContextImpl context) {
        if (orderList == null) {
            return;
        }

        context.setClauseType(RenderContext.ClauseType.ORDER_BY);
        for (Order order : orderList) {
            context.getBuffer().setLength(0);
            ((AbstractSelection<?>) order.getExpression()).render(context);
            String expression = context.takeBuffer();
            Map<String, InternalQuery<?>> aliasToSubqueries = context.takeAliasToSubqueryMap();

            if (aliasToSubqueries.isEmpty()) {
                boolean nullsFirst = false;

                if (order instanceof BlazeOrder) {
                    nullsFirst = ((BlazeOrder) order).isNullsFirst();
                }

                ob.orderBy(expression, order.isAscending(), nullsFirst);
            } else {
                throw new IllegalArgumentException("Subqueries are not supported in the order by clause!");
                //            MultipleSubqueryInitiator<?> initiator = ob.groupBySubqueries(expression);
                //
                //            for (Map.Entry<String, InternalQuery<?>> subqueryEntry : aliasToSubqueries.entrySet()) {
                //                context.pushSubqueryInitiator(initiator.with(subqueryEntry.getKey()));
                //                subqueryEntry.getValue().renderSubquery(context);
                //                context.popSubqueryInitiator();
                //            }
                //
                //            initiator.end();
            }
        }
    }

}
