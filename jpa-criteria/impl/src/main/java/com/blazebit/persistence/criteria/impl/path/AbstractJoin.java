package com.blazebit.persistence.criteria.impl.path;

import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.Attribute;

import com.blazebit.persistence.criteria.BlazeFetch;
import com.blazebit.persistence.criteria.BlazeFrom;
import com.blazebit.persistence.criteria.BlazeJoin;
import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.RenderContext;
import com.blazebit.persistence.criteria.impl.expression.SubqueryExpression;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractJoin<Z, X> extends AbstractFrom<Z, X> implements BlazeJoin<Z, X>, BlazeFetch<Z,X> {

    private static final long serialVersionUID = 1L;
    
    private final Attribute<? super Z, ?> joinAttribute;
    private final JoinType joinType;
    private boolean fetch;

    public AbstractJoin(BlazeCriteriaBuilderImpl criteriaBuilder, Class<X> javaType, AbstractPath<Z> pathSource, Attribute<? super Z, ?> joinAttribute, JoinType joinType) {
        super(criteriaBuilder, javaType, pathSource);
        this.joinAttribute = joinAttribute;
        this.joinType = joinType;
    }

    @Override
    public Attribute<? super Z, ?> getAttribute() {
        return joinAttribute;
    }

    @Override
    public JoinType getJoinType() {
        return joinType;
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public BlazeFrom<?, Z> getParent() {
        return (BlazeFrom<?, Z>) getBasePath();
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public BlazeJoin<Z, X> fetch() {
        ((AbstractFrom<?, Z>) getBasePath()).getJoinScope().addFetch(this);
        return this;
    }

    @Override
    public boolean isFetch() {
        return fetch;
    }

    public void setFetch(boolean fetch) {
        this.fetch = fetch;
    }

    @Override
    public AbstractJoin<Z, X> correlateTo(SubqueryExpression<?> subquery) {
        return (AbstractJoin<Z, X>) super.correlateTo(subquery);
    }
}
