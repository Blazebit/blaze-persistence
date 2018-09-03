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

package com.blazebit.persistence.criteria.impl;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.FromBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.GroupByBuilder;
import com.blazebit.persistence.HavingBuilder;
import com.blazebit.persistence.JoinOnBuilder;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.OrderByBuilder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.SelectObjectBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.WhereBuilder;
import com.blazebit.persistence.criteria.BlazeAbstractQuery;
import com.blazebit.persistence.criteria.BlazeJoin;
import com.blazebit.persistence.criteria.BlazeOrder;
import com.blazebit.persistence.criteria.BlazeRoot;
import com.blazebit.persistence.criteria.BlazeSubquery;
import com.blazebit.persistence.criteria.impl.RenderContext.ClauseType;
import com.blazebit.persistence.criteria.impl.expression.AbstractSelection;
import com.blazebit.persistence.criteria.impl.expression.SubqueryExpression;
import com.blazebit.persistence.criteria.impl.path.AbstractFrom;
import com.blazebit.persistence.criteria.impl.path.AbstractJoin;
import com.blazebit.persistence.criteria.impl.path.EntityJoin;
import com.blazebit.persistence.criteria.impl.path.RootImpl;
import com.blazebit.persistence.criteria.impl.path.TreatedPath;

import javax.persistence.Tuple;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class InternalQuery<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final BlazeAbstractQuery<T> owner;
    private final BlazeCriteriaBuilderImpl criteriaBuilder;
    private final boolean isSubQuery;

    private boolean distinct;
    private Selection<? extends T> selection;
    private final Set<RootImpl<?>> roots = new LinkedHashSet<>();
    private Set<AbstractFrom<?, ?>> correlationRoots;
    private Predicate restriction;
    private List<Expression<?>> groupList = Collections.emptyList();
    private Predicate having;
    private List<BlazeOrder> orderList = Collections.emptyList();
    private List<Subquery<?>> subqueries;

    public InternalQuery(BlazeAbstractQuery<T> owner, BlazeCriteriaBuilderImpl criteriaBuilder) {
        this.owner = owner;
        this.criteriaBuilder = criteriaBuilder;
        this.isSubQuery = owner instanceof Subquery<?>;
    }

    /* Select */

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    @SuppressWarnings("unchecked")
    public Selection<T> getSelection() {
        return (Selection<T>) selection;
    }

    public void setSelection(Selection<? extends T> selection) {
        // NOTE: checks for duplicate aliases and predicate selects are already done
        this.selection = selection;
    }

    /* From */

    @SuppressWarnings("unchecked")
    public Set<Root<?>> getRoots() {
        return (Set<Root<?>>) (Set<?>) roots;
    }

    @SuppressWarnings("unchecked")
    public Set<BlazeRoot<?>> getBlazeRoots() {
        return (Set<BlazeRoot<?>>) (Set<?>) roots;
    }

    public <X> BlazeRoot<X> from(Class<X> entityClass, String alias) {
        EntityType<X> entityType = criteriaBuilder.getEntityMetamodel().entity(entityClass);
        if (entityType == null) {
            throw new IllegalArgumentException(entityClass + " is not an entity");
        }
        return from(entityType, alias);
    }

    public <X> BlazeRoot<X> from(EntityType<X> entityType, String alias) {
        RootImpl<X> root = new RootImpl<X>(criteriaBuilder, entityType, alias, true);
        roots.add(root);
        return root;
    }

    /* Correlation */

    public void addCorrelationRoot(AbstractFrom<?, ?> fromImplementor) {
        if (!isSubQuery) {
            throw new IllegalStateException("Query is not identified as sub-query");
        }
        if (correlationRoots == null) {
            correlationRoots = new HashSet<AbstractFrom<?, ?>>();
        }
        correlationRoots.add(fromImplementor);
    }

    public Set<Join<?, ?>> collectCorrelatedJoins() {
        if (!isSubQuery) {
            throw new IllegalStateException("Query is not identified as sub-query");
        }
        final Set<Join<?, ?>> correlatedJoins;
        if (correlationRoots != null) {
            correlatedJoins = new HashSet<Join<?, ?>>();
            for (AbstractFrom<?, ?> correlationRoot : correlationRoots) {
                correlatedJoins.addAll(correlationRoot.getJoins());
            }
        } else {
            correlatedJoins = Collections.emptySet();
        }
        return correlatedJoins;
    }

    /* Where */

    public Predicate getRestriction() {
        return restriction;
    }

    public void setRestriction(Predicate restriction) {
        this.restriction = restriction;
    }

    /* Group by */

    public List<Expression<?>> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<Expression<?>> groupList) {
        this.groupList = groupList;
    }

    /* Having */

    public Predicate getGroupRestriction() {
        return having;
    }

    public void setHaving(Predicate having) {
        this.having = having;
    }

    /* Order by */

    public List<BlazeOrder> getBlazeOrderList() {
        return orderList;
    }

    public void setBlazeOrderList(List<BlazeOrder> orderList) {
        this.orderList = orderList;
    }

    @SuppressWarnings({"unchecked"})
    public List<Order> getOrderList() {
        return (List<Order>) (List<?>) orderList;
    }

    @SuppressWarnings({"unchecked"})
    public void setOrderList(List<Order> orderList) {
        this.orderList = (List<BlazeOrder>) (List<?>) orderList;
    }

    /* Parameters */

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

    /* Subquery */

    public List<Subquery<?>> internalGetSubqueries() {
        if (subqueries == null) {
            subqueries = new ArrayList<Subquery<?>>();
        }
        return subqueries;
    }

    public <U> BlazeSubquery<U> subquery(Class<U> subqueryType) {
        SubqueryExpression<U> subquery = new SubqueryExpression<U>(criteriaBuilder, subqueryType, owner);
        internalGetSubqueries().add(subquery);
        return subquery;
    }

    /* Rendering */

    public CriteriaBuilder<T> render(CriteriaBuilder<T> cb) {
        if (distinct) {
            cb.distinct();
        }

        RenderContextImpl context = new RenderContextImpl();
        renderFrom(cb, context);
        List<TreatedPath<?>> treatedSelections = renderSelect(cb, context);

        renderWhere(cb, context, treatedSelections);
        renderGroupBy(cb, context);
        renderHaving(cb, context);
        renderOrderBy(cb, context);

        for (ImplicitParameterBinding b : context.getImplicitParameterBindings()) {
            b.bind(cb);
        }

        for (Map.Entry<String, ParameterExpression<?>> entry : context.getExplicitParameterNameMapping().entrySet()) {
            cb.setParameterType(entry.getKey(), entry.getValue().getParameterType());
        }

        return cb;
    }

    public void renderSubquery(RenderContext context) {
        RenderContextImpl contextImpl = (RenderContextImpl) context;
        SubqueryInitiator<?> initiator = context.getSubqueryInitiator();
        SubqueryBuilder<?> cb = renderSubqueryFrom(initiator, contextImpl);

        if (distinct) {
            cb.distinct();
        }

        List<TreatedPath<?>> treatedSelections = renderSelect(cb, contextImpl);
        renderWhere(cb, contextImpl, treatedSelections);
        renderGroupBy(cb, contextImpl);
        renderHaving(cb, contextImpl);
        renderOrderBy(cb, contextImpl);

        cb.end();
    }

    private List<TreatedPath<?>> renderSelect(SelectBuilder<?> cb, final RenderContextImpl context) {
        if (selection == null) {
            return Collections.emptyList();
        }

        final List<TreatedPath<?>> treatedSelections = new ArrayList<>();
        context.setClauseType(ClauseType.SELECT);

        if (selection.isCompoundSelection()) {
            Class<?> selectionType = selection.getJavaType();

            if (selectionType.isArray()) {
                for (Selection<?> s : selection.getCompoundSelectionItems()) {
                    renderSelection(cb, context, s, treatedSelections);
                }
            } else if (Tuple.class.isAssignableFrom(selectionType)) {
                if (cb instanceof CriteriaBuilder) {
                    ((CriteriaBuilder) cb).selectNew(new JpaTupleObjectBuilder(selection.getCompoundSelectionItems()) {
                        @Override
                        protected void renderSelection(SelectBuilder<?> cb, Selection<?> s) {
                            InternalQuery.this.renderSelection(cb, context, s, treatedSelections);
                        }
                    });
                } else {
                    for (Selection<?> s : selection.getCompoundSelectionItems()) {
                        renderSelection(cb, context, s, treatedSelections);
                    }
                }
            } else {
                if (!(cb instanceof FullQueryBuilder<?, ?>)) {
                    throw new IllegalArgumentException("Invalid subquery found that uses select new!");
                }

                SelectObjectBuilder<?> b = ((FullQueryBuilder<?, ?>) cb).selectNew(selectionType);
                for (Selection<?> s : selection.getCompoundSelectionItems()) {
                    if (s instanceof Subquery<?>) {
                        if (s.getAlias() != null) {
                            context.pushSubqueryInitiator(b.withSubquery(s.getAlias()));
                        } else {
                            context.pushSubqueryInitiator(b.withSubquery());
                        }

                        ((SubqueryExpression<?>) s).renderSubquery(context);
                        context.popSubqueryInitiator();
                    } else {
                        ((AbstractSelection<?>) s).render(context);
                        String expr = context.takeBuffer();
                        Map<String, InternalQuery<?>> aliasToSubqueries = context.takeAliasToSubqueryMap();

                        if (aliasToSubqueries.isEmpty()) {
                            if (s.getAlias() != null && !(s instanceof AbstractFrom<?, ?>)) {
                                b.with(expr, s.getAlias());
                            } else {
                                b.with(expr);
                            }
                        } else {
                            MultipleSubqueryInitiator<?> initiator;

                            if (s.getAlias() != null) {
                                initiator = b.withSubqueries(expr, s.getAlias());
                            } else {
                                initiator = b.withSubqueries(expr);
                            }

                            for (Map.Entry<String, InternalQuery<?>> subqueryEntry : aliasToSubqueries.entrySet()) {
                                context.pushSubqueryInitiator(initiator.with(subqueryEntry.getKey()));
                                subqueryEntry.getValue().renderSubquery(context);
                                context.popSubqueryInitiator();
                            }

                            initiator.end();
                        }
                    }
                }
                b.end();
            }
        } else {
            renderSelection(cb, context, selection, treatedSelections);
        }

        return treatedSelections;
    }

    private void renderSelection(SelectBuilder<?> cb, RenderContextImpl context, Selection<?> s, List<TreatedPath<?>> treatedSelections) {
        if (s instanceof Subquery<?>) {
            if (s.getAlias() != null) {
                context.pushSubqueryInitiator(cb.selectSubquery(s.getAlias()));
            } else {
                context.pushSubqueryInitiator(cb.selectSubquery());
            }

            ((SubqueryExpression<?>) s).renderSubquery(context);
            context.popSubqueryInitiator();
        } else {
            if (s instanceof TreatedPath<?>) {
                TreatedPath<?> treatedPath = (TreatedPath<?>) s;
                treatedSelections.add(treatedPath);
                treatedPath.getTreatedPath().render(context);
            } else {
                ((AbstractSelection<?>) s).render(context);
            }
            String expr = context.takeBuffer();
            Map<String, InternalQuery<?>> aliasToSubqueries = context.takeAliasToSubqueryMap();

            if (aliasToSubqueries.isEmpty()) {
                if (s.getAlias() != null && !(s instanceof AbstractFrom<?, ?>)) {
                    cb.select(expr, s.getAlias());
                } else {
                    cb.select(expr);
                }
            } else {
                MultipleSubqueryInitiator<?> initiator;
                if (s.getAlias() != null) {
                    initiator = cb.selectSubqueries(expr, s.getAlias());
                } else {
                    initiator = cb.selectSubqueries(expr);
                }

                for (Map.Entry<String, InternalQuery<?>> subqueryEntry : aliasToSubqueries.entrySet()) {
                    context.pushSubqueryInitiator(initiator.with(subqueryEntry.getKey()));
                    subqueryEntry.getValue().renderSubquery(context);
                    context.popSubqueryInitiator();
                }

                initiator.end();
            }
        }
    }

    private void renderFrom(FromBuilder<?> cb, RenderContextImpl context) {
        context.setClauseType(ClauseType.FROM);

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

        renderJoins(cb, true, context, path, (Set<BlazeJoin<?, ?>>) (Set<?>) r.getBlazeJoins());
        Collection<TreatedPath<?>> treatedPaths = (Collection<TreatedPath<?>>) (Collection) r.getTreatedPaths();
        if (treatedPaths != null && treatedPaths.size() > 0) {
            for (TreatedPath<?> treatedPath : treatedPaths) {
                RootImpl<?> treatedRoot = (RootImpl<?>) treatedPath;
                String treatedParentPath = "TREAT(" + path + " AS " + treatedPath.getTreatType().getName() + ')';
                renderJoins(cb, fetching, context, treatedParentPath, (Set<BlazeJoin<?, ?>>) (Set<?>) treatedRoot.getBlazeJoins());
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private SubqueryBuilder<?> renderSubqueryFrom(SubqueryInitiator<?> initiator, RenderContextImpl context) {
        SubqueryBuilder<?> cb = null;
        context.setClauseType(ClauseType.FROM);

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

        for (RootImpl<?> r : roots) {
            renderJoins(cb, context, r, false);
        }

        if (correlationRoots != null) {
            for (AbstractFrom<?, ?> r : correlationRoots) {
                Set<BlazeJoin<?, ?>> joins = (Set<BlazeJoin<?, ?>>) (Set<?>) r.getBlazeJoins();

                for (BlazeJoin<?, ?> j : joins) {
                    renderJoins(cb, context, (AbstractFrom<?, ?>) j, false);
                }
            }
        }

        return cb;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void renderJoins(FromBuilder<?> cb, boolean fetching, RenderContextImpl context, String parentPath, Set<BlazeJoin<?, ?>> joins) {
        if (joins.isEmpty()) {
            return;
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
                    } else {
                        cb.join(path, alias, getJoinType(j.getJoinType()));
                    }
                }
            }

            if (onBuilder != null) {
                context.setClauseType(ClauseType.ON);
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

            renderJoins(cb, fetching, context, alias, (Set<BlazeJoin<?, ?>>) (Set<?>) j.getBlazeJoins());

            Collection<TreatedPath<?>> treatedPaths = (Collection<TreatedPath<?>>) (Collection) join.getTreatedPaths();
            if (treatedPaths != null && treatedPaths.size() > 0) {
                for (TreatedPath<?> treatedPath : treatedPaths) {
                    AbstractJoin<?, ?> treatedJoin = (AbstractJoin<?, ?>) treatedPath;
                    String treatedParentPath = "TREAT(" + alias + " AS " + treatedPath.getTreatType().getName() + ')';
                    renderJoins(cb, fetching, context, treatedParentPath, (Set<BlazeJoin<?, ?>>) (Set<?>) treatedJoin.getBlazeJoins());
                }
            }
        }
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

    private void renderWhere(WhereBuilder<?> wb, RenderContextImpl context, List<TreatedPath<?>> treatedSelections) {
        if (restriction == null) {
            if (!treatedSelections.isEmpty()) {
                renderTreatTypeRestrictions(context, treatedSelections);
                String expression = context.takeBuffer();
                wb.setWhereExpression(expression);
            }
            return;
        }

        context.setClauseType(ClauseType.WHERE);
        context.getBuffer().setLength(0);
        ((AbstractSelection<?>) restriction).render(context);
        renderTreatTypeRestrictions(context, treatedSelections);
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

        context.setClauseType(ClauseType.GROUP_BY);
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

        context.setClauseType(ClauseType.HAVING);
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

        context.setClauseType(ClauseType.ORDER_BY);
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
