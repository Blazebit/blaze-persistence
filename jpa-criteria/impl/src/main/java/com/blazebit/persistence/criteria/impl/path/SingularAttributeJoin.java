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

package com.blazebit.persistence.criteria.impl.path;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.RenderContext;
import com.blazebit.persistence.criteria.impl.expression.SubqueryExpression;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SingularAttributeJoin<Z, X> extends AbstractJoin<Z, X> {

    private static final long serialVersionUID = 1L;

    private final Bindable<X> model;

    private SingularAttributeJoin(BlazeCriteriaBuilderImpl criteriaBuilder, SingularAttributeJoin<Z, ? super X> original, EntityType<X> treatType) {
        super(criteriaBuilder, original, treatType);
        this.model = treatType;
    }

    @SuppressWarnings({"unchecked"})
    public SingularAttributeJoin(BlazeCriteriaBuilderImpl criteriaBuilder, Class<X> javaType, AbstractPath<Z> pathSource, SingularAttribute<? super Z, ?> joinAttribute, JoinType joinType) {
        super(criteriaBuilder, javaType, pathSource, joinAttribute, joinType);
        this.model = (Bindable<X>) (Attribute.PersistentAttributeType.EMBEDDED == joinAttribute
                .getPersistentAttributeType() ? joinAttribute : criteriaBuilder.getEntityMetamodel().managedType(javaType));
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public SingularAttribute<? super Z, ?> getAttribute() {
        return (SingularAttribute<? super Z, ?>) super.getAttribute();
    }

    @Override
    public SingularAttributeJoin<Z, X> correlateTo(SubqueryExpression<?> subquery) {
        return (SingularAttributeJoin<Z, X>) super.correlateTo(subquery);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    protected AbstractFrom<Z, X> createCorrelationDelegate() {
        return new SingularAttributeJoin<Z, X>(criteriaBuilder, getJavaType(), (AbstractPath<Z>) getBasePath(), getAttribute(), getJoinType());
    }

    @Override
    protected boolean isJoinAllowed() {
        return true;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    protected ManagedType<? super X> getManagedType() {
        Bindable<X> m = getModel();
        Bindable.BindableType t = m.getBindableType();

        switch (t) {
            case ENTITY_TYPE:
                return (ManagedType<? super X>) m;
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

    @SuppressWarnings("unchecked")
    public Bindable<X> getModel() {
        if (treatJoinType != null) {
            return (Bindable<X>) treatJoinType;
        }
        return model;
    }

    /* JPA 2.1 support */

    @Override
    public SingularAttributeJoin<Z, X> on(Expression<Boolean> restriction) {
        super.onExpression(restriction);
        return this;
    }

    @Override
    public SingularAttributeJoin<Z, X> on(Predicate... restrictions) {
        super.onPredicates(restrictions);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends X> SingularAttributeJoin<Z, T> treatJoin(Class<T> treatType) {
        setTreatType(treatType);
        return (SingularAttributeJoin<Z, T>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends X> SingularAttributeJoin<Z, T> treatAs(Class<T> treatAsType) {
        // No need to treat if it is already of the proper subtype
        if (treatAsType.isAssignableFrom(getJavaType())) {
            return (SingularAttributeJoin<Z, T>) this;
        }
        return addTreatedPath(new TreatedSingularAttributeJoin<Z, T>(criteriaBuilder, this, getTreatType(treatAsType)));
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static class TreatedSingularAttributeJoin<Z, X> extends SingularAttributeJoin<Z, X> implements TreatedPath<X> {

        private static final long serialVersionUID = 1L;

        private final SingularAttributeJoin<?, ? super X> treatedJoin;
        private final EntityType<X> treatType;

        public TreatedSingularAttributeJoin(BlazeCriteriaBuilderImpl criteriaBuilder, SingularAttributeJoin<Z, ? super X> treatedJoin, EntityType<X> treatType) {
            super(criteriaBuilder, treatedJoin, treatType);
            this.treatedJoin = treatedJoin;
            this.treatType = treatType;
        }

        @Override
        protected ManagedType<X> getManagedType() {
            return treatType;
        }

        @Override
        public EntityType<X> getTreatType() {
            return treatType;
        }

        @Override
        public AbstractPath<? super X> getTreatedPath() {
            return treatedJoin;
        }

        @Override
        public String getAlias() {
            return treatedJoin.getAlias();
        }

        @Override
        public String getPathExpression() {
            return getAlias();
        }

        @Override
        public void renderPathExpression(RenderContext context) {
            render(context);
        }

        @Override
        public void render(RenderContext context) {
            prepareAlias(context);
            final StringBuilder buffer = context.getBuffer();
            buffer.append("TREAT(")
                    .append(getAlias())
                    .append(" AS ")
                    .append(getTreatType().getName())
                    .append(')');
        }
    }
}
