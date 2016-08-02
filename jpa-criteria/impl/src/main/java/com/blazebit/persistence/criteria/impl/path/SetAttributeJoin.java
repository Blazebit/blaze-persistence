package com.blazebit.persistence.criteria.impl.path;

import java.util.Set;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.SetAttribute;

import com.blazebit.persistence.criteria.BlazeSetJoin;
import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.expression.SubqueryExpression;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SetAttributeJoin<O, E> extends AbstractPluralAttributeJoin<O, Set<E>, E> implements BlazeSetJoin<O, E> {

    private static final long serialVersionUID = 1L;

    public SetAttributeJoin(BlazeCriteriaBuilderImpl criteriaBuilder, Class<E> javaType, AbstractPath<O> pathSource, SetAttribute<? super O, E> joinAttribute, JoinType joinType) {
        super(criteriaBuilder, javaType, pathSource, joinAttribute, joinType);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public SetAttribute<? super O, E> getAttribute() {
        return (SetAttribute<? super O, E>) super.getAttribute();
    }

    @Override
    public SetAttribute<? super O, E> getModel() {
        return getAttribute();
    }

    @Override
    public final SetAttributeJoin<O, E> correlateTo(SubqueryExpression<?> subquery) {
        return (SetAttributeJoin<O, E>) super.correlateTo(subquery);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected AbstractFrom<O, E> createCorrelationDelegate() {
        return new SetAttributeJoin<O, E>(criteriaBuilder, getJavaType(), (AbstractPath<O>) getParentPath(), getAttribute(), getJoinType());
    }

    /* JPA 2.1 support */
    
    @Override
    public Predicate getOn() {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public BlazeSetJoin<O, E> on(Expression<Boolean> restriction) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public BlazeSetJoin<O, E> on(Predicate... restrictions) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
