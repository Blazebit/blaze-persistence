/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.querydsl;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.Queryable;
import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.JoinFlag;
import com.querydsl.core.NonUniqueResultException;
import com.querydsl.core.QueryFlag;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.QueryModifiers;
import com.querydsl.core.QueryResults;
import com.querydsl.core.support.ReplaceVisitor;
import com.querydsl.core.types.CollectionExpression;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.MapExpression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLSerializer;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.AbstractJPAQuery;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.blazebit.persistence.querydsl.HintsAccessor.getHints;

/**
 * Abstract base class for JPA API based implementations of the JPQLQuery interface
 *
 * @param <T> Query result type
 * @param <Q> Concrete query builder type
 * @author Jan-Willem Gmelig Meyling
 * @since 1.5.0
 */
@SuppressWarnings("unused")
public abstract class AbstractBlazeJPAQuery<T, Q extends AbstractBlazeJPAQuery<T, Q>> extends AbstractJPAQuery<T, Q> implements JPQLNextQuery<T>, ExtendedFetchable<T> {

    /**
     * Lateral join flag.
     * Can be added using {@link QueryMetadata#addJoinFlag(JoinFlag)}.
     * Used internally for implementing {@link #lateral()}.
     */
    public static final JoinFlag LATERAL = new JoinFlag("LATERAL", JoinFlag.Position.BEFORE_TARGET);

    /**
     * Default join flag.
     * Can be added using {@link QueryMetadata#addJoinFlag(JoinFlag)}.
     */
    public static final JoinFlag DEFAULT = new JoinFlag("DEFAULT");

    private static final Logger LOG = Logger.getLogger(AbstractBlazeJPAQuery.class.getName());

    private static final long serialVersionUID = 992291611132051622L;

    protected final CriteriaBuilderFactory criteriaBuilderFactory;

    protected boolean cacheable = false;

    protected final Binds<T> binds = new Binds<>();

    public AbstractBlazeJPAQuery(CriteriaBuilderFactory criteriaBuilderFactory) {
        this(null, JPQLNextTemplates.DEFAULT, new DefaultQueryMetadata(), criteriaBuilderFactory);
    }

    public AbstractBlazeJPAQuery(EntityManager em, CriteriaBuilderFactory criteriaBuilderFactory) {
        this(em, JPQLNextTemplates.DEFAULT, new DefaultQueryMetadata(), criteriaBuilderFactory);
    }

    public AbstractBlazeJPAQuery(EntityManager em, QueryMetadata metadata, CriteriaBuilderFactory criteriaBuilderFactory) {
        this(em, JPQLNextTemplates.DEFAULT, metadata, criteriaBuilderFactory);
    }

    public AbstractBlazeJPAQuery(EntityManager em, JPQLTemplates templates, CriteriaBuilderFactory criteriaBuilderFactory) {
        this(em, templates, new DefaultQueryMetadata(), criteriaBuilderFactory);
    }

    public AbstractBlazeJPAQuery(EntityManager em, JPQLTemplates templates, QueryMetadata metadata, CriteriaBuilderFactory criteriaBuilderFactory) {
        super(em, templates, metadata);
        this.criteriaBuilderFactory = criteriaBuilderFactory;
    }

    @Override
    protected JPQLNextSerializer createSerializer() {
        return new JPQLNextSerializer(getTemplates(), entityManager);
    }

    @Override
    public <X> Q fromValues(EntityPath<X> path, Collection<X> elements) {
        return this.queryMixin.from(new ValuesExpression<>(path, elements, false));
    }

    @Override
    public <X> Q fromIdentifiableValues(EntityPath<X> path, Collection<X> elements) {
        return this.queryMixin.from(new ValuesExpression<>(path, elements, true));
    }

    @Override
    public <X> Q fromValues(Path<X> path, Path<X> alias, Collection<X> elements) {
        return this.queryMixin.from(new ValuesExpression<>(path, alias, elements, false));
    }

    @Override
    public <X> Q fromIdentifiableValues(Path<X> path, Path<X> alias, Collection<X> elements) {
        return this.queryMixin.from(new ValuesExpression<>(path, alias, elements, true));
    }

    @Override
    public WithBuilder<Q> with(final EntityPath<?> alias, final Path<?>... columns) {
        final Expression<Object> columnsCombined = ExpressionUtils.list(Object.class, columns);
        final Expression<?> aliasCombined = Expressions.operation(alias.getType(), JPQLNextOps.WITH_COLUMNS, alias, columnsCombined);
        return new WithBuilderImpl(JPQLNextOps.WITH_ALIAS, alias, aliasCombined);
    }

    @Override
    public <X> Q with(Path<X> alias, SubQueryExpression<?> o) {
        Expression<?> expr = ExpressionUtils.operation(alias.getType(), JPQLNextOps.WITH_ALIAS, alias, o);
        return queryMixin.addFlag(new QueryFlag(QueryFlag.Position.WITH, expr));
    }

    @Override
    public <X> Q withRecursive(Path<X> alias, SubQueryExpression<?> o) {
        Expression<?> expr = ExpressionUtils.operation(alias.getType(), JPQLNextOps.WITH_RECURSIVE_ALIAS, alias, o);
        return queryMixin.addFlag(new QueryFlag(QueryFlag.Position.WITH, expr));
    }

    @Override
    public WithBuilder<Q> withRecursive(final EntityPath<?> alias, Path<?>... columns) {
        final Expression<Object> columnsCombined = ExpressionUtils.list(Object.class, columns);
        final Expression<?> aliasCombined = Expressions.operation(alias.getType(), JPQLNextOps.WITH_RECURSIVE_COLUMNS, alias, columnsCombined);
        return new WithBuilderImpl(JPQLNextOps.WITH_RECURSIVE_ALIAS, alias, aliasCombined);
    }

    @Override
    public Query createQuery() {
        return createQuery(getMetadata().getModifiers(), false);
    }

    // TODO @Override
    protected Query createQuery(@Nullable QueryModifiers modifiers, boolean forCount) {
        if (forCount) {
            return  getFullQueryBuilder(modifiers).getCountQuery();
        }

        Queryable<T, ?> queryable = getQueryable(modifiers);
        TypedQuery<T> query = queryable.getQuery();

        if (lockMode != null) {
            query.setLockMode(lockMode);
        }
        if (flushMode != null) {
            query.setFlushMode(flushMode);
        }

        for (Map.Entry<String, Object> entry : getHints(this)) {
            try {
                query.setHint(entry.getKey(), entry.getValue());
            } catch (UnsupportedOperationException e) {
                LOG.warning("Failed to set query hint");
            }
        }

        return query;
    }

    @Override
    public PagedList<T> fetchPage(int firstResult, int maxResults) {
        return  getFullQueryBuilder(null)
                .page(firstResult, maxResults)
                .getResultList();
    }

    @Override
    public PagedList<T> fetchPage(KeysetPage keysetPage, int firstResult, int maxResults) {
        return getFullQueryBuilder(null)
                .page(keysetPage, firstResult, maxResults)
                .getResultList();
    }

    public String getQueryString() {
        return getQueryable(null).getQueryString();
    }

    protected FullQueryBuilder<T, ?> getFullQueryBuilder(@Nullable QueryModifiers modifiers) {
        Queryable<T, ?> queryable = getQueryable(modifiers);
        if (queryable instanceof FullQueryBuilder) {
            return (FullQueryBuilder<T, ?>) queryable;
        }
        throw new UnsupportedOperationException("This feature is not yet supported on " + queryable.getClass().getSimpleName());
    }

    protected Queryable<T, ?> getQueryable(@Nullable QueryModifiers modifiers) {
        BlazeCriteriaBuilderRenderer<T> blazeCriteriaBuilderRenderer = new BlazeCriteriaBuilderRenderer<T>(criteriaBuilderFactory, entityManager, getTemplates());
        Queryable<T, ?> queryable = blazeCriteriaBuilderRenderer.render(this);
        CriteriaBuilder<T> criteriaBuilder = blazeCriteriaBuilderRenderer.getCriteriaBuilder();

        if (modifiers != null) {
            if (modifiers.getLimitAsInteger() != null) {
                criteriaBuilder.setMaxResults(modifiers.getLimitAsInteger());
            }
            if (modifiers.getOffsetAsInteger() != null) {
                criteriaBuilder.setFirstResult(modifiers.getOffsetAsInteger());
            }
        }

        for (Map.Entry<String, Object> entry : getHints(this)) {
            criteriaBuilder.setProperty(entry.getKey(), entry.getValue().toString());
        }

        if (cacheable) {
            criteriaBuilder.setCacheable(true);
        }

        return queryable;
    }

    @Override
    @SuppressWarnings({ "unchecked", "unsafe" })
    protected void clone(Q query) {
        super.clone(query);
        this.cacheable = query.cacheable;
        this.binds.addBinds(((FactoryExpression) query.binds.accept(new ReplaceVisitor<Void>(), null)).getArgs());
    }

    // Work around private access to query(...)

    @Override
    public long fetchCount() {
        try {
            Query query = createQuery(getMetadata().getModifiers(), true);
            return (Long) query.getSingleResult();
        } finally {
            reset();
        }
    }

    /**
     * Get the projection in {@link QueryResults} form
     *
     * @return results
     * @see #fetchPage(int, int)
     * @see #fetchPage(KeysetPage, int, int)
     * @deprecated Blaze-Persistence has better ways for paginated result sets, which are also optimized to use a single query.
     */
    @Override
    @Deprecated
    public QueryResults<T> fetchResults() {
        try {
            Query countQuery = createQuery(null, true);
            long total = (Long) countQuery.getSingleResult();
            if (total > 0) {
                QueryModifiers modifiers = getMetadata().getModifiers();
                Query query = createQuery(modifiers, false);
                @SuppressWarnings("unchecked")
                List<T> list = (List<T>) getResultList(query);
                return new QueryResults<>(list, modifiers, total);
            } else {
                return QueryResults.emptyResults();
            }
        } finally {
            reset();
        }

    }

    private List<?> getResultList(Query query) {
        // TODO : use lazy fetch here?
        if (projection != null) {
            List<?> results = query.getResultList();
            List<Object> rv = new ArrayList<>(results.size());
            for (Object o : results) {
                if (o != null) {
                    if (!o.getClass().isArray()) {
                        o = new Object[]{o};
                    }
                    rv.add(projection.newInstance((Object[]) o));
                } else {
                    rv.add(null);
                }
            }
            return rv;
        } else {
            return query.getResultList();
        }
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public T fetchOne() throws NonUniqueResultException {
        try {
            Query query = createQuery(getMetadata().getModifiers(), false);
            return (T) getSingleResult(query);
        } catch (javax.persistence.NoResultException e) {
            return null;
        } catch (javax.persistence.NonUniqueResultException e) {
            throw new NonUniqueResultException(e);
        } finally {
            reset();
        }
    }

    @Nullable
    private Object getSingleResult(Query query) {
        if (projection != null) {
            Object result = query.getSingleResult();
            if (result != null) {
                if (!result.getClass().isArray()) {
                    result = new Object[]{result};
                }
                return projection.newInstance((Object[]) result);
            } else {
                return null;
            }
        } else {
            return query.getSingleResult();
        }
    }

    // End workaround

    // Full joins
    @Override
    public <P> Q fullJoin(CollectionExpression<?,P> target) {
        return queryMixin.fullJoin(target);
    }

    @Override
    public <P> Q fullJoin(CollectionExpression<?,P>target, Path<P> alias) {
        return queryMixin.fullJoin(target, alias);
    }

    @Override
    public <P> Q fullJoin(EntityPath<P> target) {
        return queryMixin.fullJoin(target);
    }

    @Override
    public <P> Q fullJoin(EntityPath<P> target, Path<P> alias) {
        return queryMixin.fullJoin(target, alias);
    }

    @Override
    public <P> Q fullJoin(MapExpression<?,P> target) {
        return queryMixin.fullJoin(target);
    }

    @Override
    public <P> Q fullJoin(MapExpression<?,P> target, Path<P> alias) {
        return queryMixin.fullJoin(target, alias);
    }

    @Override
    @SuppressWarnings({"unchecked","rawtypes"})
    public <X> Q from(SubQueryExpression<X> subQueryExpression, Path<X> alias) {
        return (Q) queryMixin.from(ExpressionUtils.as((Expression) subQueryExpression, alias));
    }

    @Override
    @SuppressWarnings({"unchecked","rawtypes"})
    public <X> Q leftJoin(SubQueryExpression<X> o, Path<X> alias) {
        return (Q) queryMixin.leftJoin((Expression) o, alias);
    }

    @Override
    public Q lateral() {
        return queryMixin.addJoinFlag(LATERAL);
    }

    @Override
    public Q defaultJoin() {
        return queryMixin.addJoinFlag(DEFAULT);
    }

    @Override
    @SuppressWarnings({"unchecked","rawtypes"})
    public <X> Q rightJoin(SubQueryExpression<X> o, Path<X> alias) {
        return (Q) queryMixin.rightJoin((Expression) o, alias);
    }

    @Override
    @SuppressWarnings({"unchecked","rawtypes"})
    public <X> Q fullJoin(SubQueryExpression<X> o, Path<X> alias) {
        return (Q) queryMixin.fullJoin((Expression) o, alias);
    }

    @Override
    @SuppressWarnings({"unchecked","rawtypes"})
    public <X> Q innerJoin(SubQueryExpression<X> o, Path<X> alias) {
        return (Q) queryMixin.innerJoin((Expression) o, alias);
    }

    // End full joins

    public Q setCacheable(boolean cacheable) {
        this.cacheable = cacheable;
        return queryMixin.getSelf();
    }

    @Override
    protected JPQLSerializer serialize(boolean forCountRow) {
        return super.serialize(forCountRow);
    }

    // Union stuff

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <RT> SetExpression<RT> setOperation(JPQLNextOps operator, boolean wrapSets, List<SubQueryExpression<RT>> sq) {
        queryMixin.setProjection(sq.get(0).getMetadata().getProjection());
        if (!queryMixin.getMetadata().getJoins().isEmpty()) {
            throw new IllegalArgumentException("Don't mix union and from");
        }

        this.queryMixin.addFlag(new SetOperationFlag(SetUtils.setOperation(operator, wrapSets, sq.toArray(new Expression[0]))));
        return new SetExpressionImpl(this);
    }

    @Override
    public <RT> SetExpression<RT> union(List<SubQueryExpression<RT>> sq) {
        return setOperation(JPQLNextOps.SET_UNION, true, sq);
    }

    @Override
    public <RT> SetExpression<RT> unionAll(List<SubQueryExpression<RT>> sq) {
        return setOperation(JPQLNextOps.SET_UNION_ALL, true, sq);
    }

    @Override
    public <RT> SetExpression<RT> intersect(List<SubQueryExpression<RT>> sq) {
        return setOperation(JPQLNextOps.SET_INTERSECT, true, sq);
    }

    @Override
    public <RT> SetExpression<RT> intersectAll(List<SubQueryExpression<RT>> sq) {
        return setOperation(JPQLNextOps.SET_INTERSECT_ALL, true, sq);
    }

    @Override
    public <RT> SetExpression<RT> except(List<SubQueryExpression<RT>> sq) {
        return setOperation(JPQLNextOps.SET_EXCEPT, true, sq);
    }

    @Override
    public <RT> SetExpression<RT> exceptAll(List<SubQueryExpression<RT>> sq) {
        return setOperation(JPQLNextOps.SET_EXCEPT_ALL, true, sq);
    }

    @Override
    @SafeVarargs
    public final <RT> SetExpression<RT> union(SubQueryExpression<RT>... sq) {
        return union(Arrays.asList(sq));

    }

    @Override
    @SafeVarargs
    public final <RT> SetExpression<RT> unionAll(SubQueryExpression<RT>... sq) {
        return unionAll(Arrays.asList(sq));
    }

    @Override
    @SafeVarargs
    public final <RT> SetExpression<RT> intersect(SubQueryExpression<RT>... sq) {
        return intersect(Arrays.asList(sq));
    }

    @Override
    @SafeVarargs
    public final <RT> SetExpression<RT> intersectAll(SubQueryExpression<RT>... sq) {
        return intersectAll(Arrays.asList(sq));
    }

    @Override
    @SafeVarargs
    public final <RT> SetExpression<RT> except(SubQueryExpression<RT>... sq) {
        return except(Arrays.asList(sq));
    }

    @Override
    @SafeVarargs
    public final <RT> SetExpression<RT> exceptAll(SubQueryExpression<RT>... sq) {
        return exceptAll(Arrays.asList(sq));
    }

    /**
     * Bind a CTE attribute to a select expression.
     *
     * @param path Attribute path
     * @param expression Expression to bind the path to
     * @param <U> Attribute type
     * @return this query
     */
    public <U> Q bind(Path<? super U> path, Expression<? extends U> expression) {
        select(binds.bind(path, expression));
        return queryMixin.getSelf();
    }

    @Override
    public Q window(NamedWindow namedWindow) {
        queryMixin.addFlag(new QueryFlag(QueryFlag.Position.AFTER_HAVING, namedWindow.getWindowDefinition()));
        return queryMixin.getSelf();
    }

    /**
     * With builder implementation
     *
     * @author Jan-Willem Gmelig Meyling
     */
    private class WithBuilderImpl implements WithBuilder<Q> {

        private final EntityPath<?> alias;
        private final Expression<?> aliasCombined;
        private final JPQLNextOps withRecursiveAlias;

        public WithBuilderImpl(JPQLNextOps withRecursiveAlias, EntityPath<?> alias, Expression<?> aliasCombined) {
            this.alias = alias;
            this.aliasCombined = aliasCombined;
            this.withRecursiveAlias = withRecursiveAlias;
        }

        @Override
        public Q as(Expression expr) {
            Expression<?> flag = ExpressionUtils.operation(alias.getType(), withRecursiveAlias, aliasCombined, expr);
            return queryMixin.addFlag(new QueryFlag(QueryFlag.Position.WITH, flag));
        }
    }
}
