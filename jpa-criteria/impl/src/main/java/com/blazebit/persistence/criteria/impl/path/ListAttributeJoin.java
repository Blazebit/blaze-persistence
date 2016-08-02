package com.blazebit.persistence.criteria.impl.path;

import java.util.List;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.ListAttribute;

import com.blazebit.persistence.criteria.BlazeListJoin;
import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.expression.SubqueryExpression;
import com.blazebit.persistence.criteria.impl.expression.function.IndexFunction;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ListAttributeJoin<O, E> extends AbstractPluralAttributeJoin<O, List<E>, E> implements BlazeListJoin<O, E> {

    private static final long serialVersionUID = 1L;

    public ListAttributeJoin(BlazeCriteriaBuilderImpl criteriaBuilder, Class<E> javaType, AbstractPath<O> pathSource, ListAttribute<? super O, E> joinAttribute, JoinType joinType) {
        super(criteriaBuilder, javaType, pathSource, joinAttribute, joinType);
    }

    @Override
    public Expression<Integer> index() {
        return new IndexFunction(criteriaBuilder, this);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public ListAttribute<? super O, E> getAttribute() {
        return (ListAttribute<? super O, E>) super.getAttribute();
    }

    @Override
    public ListAttribute<? super O, E> getModel() {
        return getAttribute();
    }

    @Override
    public final ListAttributeJoin<O, E> correlateTo(SubqueryExpression<?> subquery) {
        return (ListAttributeJoin<O, E>) super.correlateTo(subquery);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected AbstractFrom<O, E> createCorrelationDelegate() {
        return new ListAttributeJoin<O, E>(criteriaBuilder, getJavaType(), (AbstractPath<O>) getParentPath(), getAttribute(), getJoinType());
    }

    /* JPA 2.1 support */
    
    @Override
    public Predicate getOn() {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public BlazeListJoin<O, E> on(Expression<Boolean> restriction) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public BlazeListJoin<O, E> on(Predicate... restrictions) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
