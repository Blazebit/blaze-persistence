package com.blazebit.persistence.criteria.impl.path;

import java.util.Collection;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.CollectionAttribute;

import com.blazebit.persistence.criteria.BlazeCollectionJoin;
import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.expression.SubqueryExpression;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CollectionAttributeJoin<O, E> extends AbstractPluralAttributeJoin<O, Collection<E>, E> implements BlazeCollectionJoin<O, E> {

    private static final long serialVersionUID = 1L;

    public CollectionAttributeJoin(BlazeCriteriaBuilderImpl criteriaBuilder, Class<E> javaType, AbstractPath<O> pathSource, CollectionAttribute<? super O, E> joinAttribute, JoinType joinType) {
        super(criteriaBuilder, javaType, pathSource, joinAttribute, joinType);
    }

    @Override
    public final CollectionAttributeJoin<O, E> correlateTo(SubqueryExpression<?> subquery) {
        return (CollectionAttributeJoin<O, E>) super.correlateTo(subquery);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public CollectionAttribute<? super O, E> getAttribute() {
        return (CollectionAttribute<? super O, E>) super.getAttribute();
    }

    @Override
    public CollectionAttribute<? super O, E> getModel() {
        return getAttribute();
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected AbstractFrom<O, E> createCorrelationDelegate() {
        return new CollectionAttributeJoin<O, E>(criteriaBuilder, getJavaType(), (AbstractPath<O>) getParentPath(), getAttribute(), getJoinType());
    }

    /* JPA 2.1 support */
    
    @Override
    public Predicate getOn() {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public BlazeCollectionJoin<O, E> on(Expression<Boolean> restriction) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public BlazeCollectionJoin<O, E> on(Predicate... restrictions) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
