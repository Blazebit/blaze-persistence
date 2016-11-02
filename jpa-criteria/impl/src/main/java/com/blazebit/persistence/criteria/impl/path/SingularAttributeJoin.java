/*
 * Copyright 2014 - 2016 Blazebit.
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

package com.blazebit.persistence.criteria.impl.path;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import com.blazebit.persistence.criteria.BlazeJoin;
import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.expression.SubqueryExpression;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SingularAttributeJoin<Z, X> extends AbstractJoin<Z, X> {

    private static final long serialVersionUID = 1L;
    
    private final Bindable<X> model;

    @SuppressWarnings({ "unchecked" })
    public SingularAttributeJoin(BlazeCriteriaBuilderImpl criteriaBuilder, Class<X> javaType, AbstractPath<Z> pathSource, SingularAttribute<? super Z, ?> joinAttribute, JoinType joinType) {
        super(criteriaBuilder, javaType, pathSource, joinAttribute, joinType);
        this.model = (Bindable<X>) (Attribute.PersistentAttributeType.EMBEDDED == joinAttribute
            .getPersistentAttributeType() ? joinAttribute : criteriaBuilder.getEntityManagerFactory().getMetamodel().managedType(javaType));
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public SingularAttribute<? super Z, ?> getAttribute() {
        return (SingularAttribute<? super Z, ?>) super.getAttribute();
    }

    @Override
    public SingularAttributeJoin<Z, X> correlateTo(SubqueryExpression<?> subquery) {
        return (SingularAttributeJoin<Z, X>) super.correlateTo(subquery);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected AbstractFrom<Z, X> createCorrelationDelegate() {
        return new SingularAttributeJoin<Z, X>(criteriaBuilder, getJavaType(), (AbstractPath<Z>) getBasePath(), getAttribute(), getJoinType());
    }

    @Override
    protected boolean isJoinAllowed() {
        return true;
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected ManagedType<? super X> getManagedType() {
        Bindable.BindableType t = model.getBindableType();

        switch (t) {
            case ENTITY_TYPE:
                return (ManagedType<? super X>) model;
            case SINGULAR_ATTRIBUTE:
                final Type<?> joinedAttributeType = ((SingularAttribute<?, ?>) getAttribute()).getType();
                if (!(joinedAttributeType instanceof ManagedType<?>)) {
                    throw new IllegalArgumentException("Joins on '" + getPathExpression() + "' are not allowed");
                }
                return (ManagedType<? super X>) joinedAttributeType;
            case PLURAL_ATTRIBUTE:
                final Type<?> elementType = ((PluralAttribute<?, ?, ?>) getAttribute()).getElementType();
                if (!(elementType instanceof ManagedType<?>)) {
                    throw new IllegalArgumentException("Joins on '" + getPathExpression() + "' are not allowed");
                }
                return (ManagedType<? super X>) elementType;
            default:
                break;
        }

        return super.getManagedType();
    }

    public Bindable<X> getModel() {
        return model;
    }

    /* JPA 2.1 support */
    
    @Override
    public Predicate getOn() {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public BlazeJoin<Z, X> on(Expression<Boolean> restriction) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public BlazeJoin<Z, X> on(Predicate... restrictions) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
