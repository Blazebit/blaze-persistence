package com.blazebit.persistence.criteria.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import com.blazebit.persistence.*;
import com.blazebit.persistence.criteria.*;
import com.blazebit.persistence.criteria.impl.RenderContext.ClauseType;
import com.blazebit.persistence.criteria.impl.expression.AbstractSelection;
import com.blazebit.persistence.criteria.impl.expression.SubqueryExpression;
import com.blazebit.persistence.criteria.impl.path.AbstractFrom;
import com.blazebit.persistence.criteria.impl.path.RootImpl;

/**
 *
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
    private final Set<BlazeRoot<?>> roots = new LinkedHashSet<BlazeRoot<?>>();
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

    public Set<BlazeRoot<?>> getBlazeRoots() {
        return roots;
    }

    public <X> BlazeRoot<X> from(Class<X> entityClass, String alias) {
        EntityType<X> entityType = criteriaBuilder.getEntityManagerFactory().getMetamodel().entity(entityClass);
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

    @SuppressWarnings({ "unchecked" })
    public List<Order> getOrderList() {
        return (List<Order>) (List<?>) orderList;
    }

    @SuppressWarnings({ "unchecked" })
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

        // TODO: on clauses?

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
        renderSelect(cb, context);

        renderWhere(cb, context);
        renderGroupBy(cb, context);
        renderHaving(cb, context);
        renderOrderBy(cb, context);
        
        for (ImplicitParameterBinding b : context.getImplicitParameterBindings()) {
            b.bind(cb);
        }

        for (Map.Entry<String, ParameterExpression<?>> entry: context.getExplicitParameterNameMapping().entrySet()) {
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

        renderSelect(cb, contextImpl);
        renderWhere(cb, contextImpl);
        renderGroupBy(cb, contextImpl);
        renderHaving(cb, contextImpl);
        renderOrderBy(cb, contextImpl);

        cb.end();
    }

    private void renderSelect(SelectBuilder<?> cb, final RenderContextImpl context) {
        if (selection == null) {
            return;
        }
        
        context.setClauseType(ClauseType.SELECT);

        if (selection.isCompoundSelection()) {
            Class<?> selectionType = selection.getJavaType();

            if (selectionType.isArray()) {
                for (Selection<?> s : selection.getCompoundSelectionItems()) {
                    renderSelection(cb, context, s);
                }
            } else if (Tuple.class.isAssignableFrom(selectionType)) {
                if (cb instanceof CriteriaBuilder) {
                    ((CriteriaBuilder) cb).selectNew(new JpaTupleObjectBuilder(selection.getCompoundSelectionItems()) {
                        @Override
                        protected void renderSelection(SelectBuilder<?> cb, Selection<?> s) {
                            InternalQuery.this.renderSelection(cb, context, s);
                        }
                    });
                } else {
                    for (Selection<?> s : selection.getCompoundSelectionItems()) {
                        renderSelection(cb, context, s);
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
            renderSelection(cb, context, selection);
        }
    }

    private void renderSelection(SelectBuilder<?> cb, RenderContextImpl context, Selection<?> s) {
        if (s instanceof Subquery<?>) {
            if (s.getAlias() != null) {
                context.pushSubqueryInitiator(cb.selectSubquery(s.getAlias()));
            } else {
                context.pushSubqueryInitiator(cb.selectSubquery());
            }

            ((SubqueryExpression<?>) s).renderSubquery(context);
            context.popSubqueryInitiator();
        } else {
            ((AbstractSelection<?>) s).render(context);
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void renderFrom(FromBuilder<?> cb, RenderContextImpl context) {
        context.setClauseType(ClauseType.FROM);

        for (BlazeRoot<?> r : roots) {
            ((AbstractFrom<?, ?>) r).prepareAlias(context);
            if (r.getAlias() != null) {
                cb.from(r.getJavaType(), r.getAlias());
            } else {
                cb.from(r.getJavaType());
            }
        }
        
        for (BlazeRoot<?> r : roots) {
            String path;
            if (r.getAlias() != null) {
                path = r.getAlias();
            } else {
                path = "";
            }
            
            renderJoins(cb, true, context, path, (Set<BlazeJoin<?, ?>>) (Set<?>) r.getBlazeJoins());
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private SubqueryBuilder<?> renderSubqueryFrom(SubqueryInitiator<?> initiator, RenderContextImpl context) {
        SubqueryBuilder<?> cb = null;
        context.setClauseType(ClauseType.FROM);

        for (BlazeRoot<?> r : roots) {
            ((AbstractFrom<?, ?>) r).prepareAlias(context);
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
                    String path = getPath(r.getAlias(), j);
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
        
        for (BlazeRoot<?> r : roots) {
            String path;
            if (r.getAlias() != null) {
                path = r.getAlias();
            } else {
                path = "";
            }
            
            renderJoins(cb, false, context, path, (Set<BlazeJoin<?, ?>>) (Set<?>) r.getJoins());
        }

        if (correlationRoots != null) {
            for (AbstractFrom<?, ?> r : correlationRoots) {
                Set<BlazeJoin<?, ?>> joins = (Set<BlazeJoin<?, ?>>) (Set<?>) r.getBlazeJoins();

                for (BlazeJoin<?, ?> j : joins) {
                    renderJoins(cb, false, context, j.getAlias(), (Set<BlazeJoin<?, ?>>) (Set<?>) j.getBlazeJoins());
                }
            }
        }
        
        return cb;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void renderJoins(FromBuilder<?> cb, boolean fetching, RenderContextImpl context, String parentPath, Set<BlazeJoin<?, ?>> joins) {
        if (joins.isEmpty()) {
            return;
        }

        for (BlazeJoin<?, ?> j : joins) {
            ((AbstractFrom<?, ?>) j).prepareAlias(context);
            // TODO: on clause? implicit joins?
            String path = getPath(parentPath, j);
            String alias = j.getAlias();

            // "Join" relations in embeddables
            if (j.getAttribute().getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {
                alias = path;
            } else {
                if (fetching && j.isFetch()) {
                    ((FullQueryBuilder<?, ?>) cb).join(path, alias, getJoinType(j.getJoinType()), true);
                } else {
                    cb.join(path, alias, getJoinType(j.getJoinType()));
                }
            }
            renderJoins(cb, fetching, context, alias, (Set<BlazeJoin<?, ?>>) (Set<?>) j.getBlazeJoins());
        }
    }

    private String getPath(String parentPath, BlazeJoin<?, ?> j) {
        String path = j.getAttribute().getName();
        if (parentPath == null || parentPath.isEmpty()) {
            return path;
        }

        return parentPath + "." + path;
    }

    private JoinType getJoinType(javax.persistence.criteria.JoinType joinType) {
        switch (joinType) {
            case INNER:
                return JoinType.INNER;
            case LEFT:
                return JoinType.LEFT;
            case RIGHT:
                return JoinType.RIGHT;
        }

        throw new IllegalArgumentException("Unsupported join type: " + joinType);
    }
    
    private void renderWhere(WhereBuilder<?> wb, RenderContextImpl context) {
        if (restriction == null) {
            return;
        }

        context.setClauseType(ClauseType.WHERE);
        context.getBuffer().setLength(0);
        ((AbstractSelection<?>) restriction).render(context);
        String expression = context.takeBuffer();
        Map<String, InternalQuery<?>> aliasToSubqueries = context.takeAliasToSubqueryMap();

        if (aliasToSubqueries.isEmpty()) {
            wb.whereExpression(expression);
        } else {
            MultipleSubqueryInitiator<?> initiator = wb.whereExpressionSubqueries(expression);
            
            for (Map.Entry<String, InternalQuery<?>> subqueryEntry : aliasToSubqueries.entrySet()) {
                context.pushSubqueryInitiator(initiator.with(subqueryEntry.getKey()));
                subqueryEntry.getValue().renderSubquery(context);
                context.popSubqueryInitiator();
            }
            
            initiator.end();
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
            hb.havingExpression(expression);
        } else {
            MultipleSubqueryInitiator<?> initiator = hb.havingExpressionSubqueries(expression);
            
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
