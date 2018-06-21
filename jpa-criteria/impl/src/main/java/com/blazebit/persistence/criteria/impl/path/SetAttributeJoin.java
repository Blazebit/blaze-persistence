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

import com.blazebit.persistence.criteria.BlazeSetJoin;
import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.RenderContext;
import com.blazebit.persistence.criteria.impl.expression.SubqueryExpression;
import com.blazebit.persistence.criteria.impl.support.SetJoinSupport;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SetAttribute;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SetAttributeJoin<O, E> extends AbstractPluralAttributeJoin<O, Set<E>, E> implements BlazeSetJoin<O, E>, SetJoinSupport<O, E> {

    private static final long serialVersionUID = 1L;

    private SetAttributeJoin(BlazeCriteriaBuilderImpl criteriaBuilder, SetAttributeJoin<O, ? super E> original, EntityType<E> treatType) {
        super(criteriaBuilder, original, treatType);
    }

    public SetAttributeJoin(BlazeCriteriaBuilderImpl criteriaBuilder, Class<E> javaType, AbstractPath<O> pathSource, SetAttribute<? super O, E> joinAttribute, JoinType joinType) {
        super(criteriaBuilder, javaType, pathSource, joinAttribute, joinType);
    }

    @Override
    @SuppressWarnings({"unchecked"})
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
    @SuppressWarnings({"unchecked"})
    protected AbstractFrom<O, E> createCorrelationDelegate() {
        return new SetAttributeJoin<O, E>(criteriaBuilder, getJavaType(), (AbstractPath<O>) getParentPath(), getAttribute(), getJoinType());
    }

    /* JPA 2.1 support */

    @Override
    public SetAttributeJoin<O, E> on(Expression<Boolean> restriction) {
        super.onExpression(restriction);
        return this;
    }

    @Override
    public SetAttributeJoin<O, E> on(Predicate... restrictions) {
        super.onPredicates(restrictions);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends E> SetAttributeJoin<O, T> treatJoin(Class<T> treatType) {
        setTreatType(treatType);
        return (SetAttributeJoin<O, T>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends E> SetAttributeJoin<O, T> treatAs(Class<T> treatAsType) {
        // No need to treat if it is already of the proper subtype
        if (treatAsType.isAssignableFrom(getJavaType())) {
            return (SetAttributeJoin<O, T>) this;
        }
        return addTreatedPath(new TreatedSetAttributeJoin<O, T>(criteriaBuilder, this, getTreatType(treatAsType)));
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static class TreatedSetAttributeJoin<O, E> extends SetAttributeJoin<O, E> implements TreatedPath<E> {

        private static final long serialVersionUID = 1L;

        private final SetAttributeJoin<?, ? super E> treatedJoin;
        private final EntityType<E> treatType;

        public TreatedSetAttributeJoin(BlazeCriteriaBuilderImpl criteriaBuilder, SetAttributeJoin<O, ? super E> treatedJoin, EntityType<E> treatType) {
            super(criteriaBuilder, treatedJoin, treatType);
            this.treatedJoin = treatedJoin;
            this.treatType = treatType;
        }

        @Override
        protected ManagedType<E> getManagedType() {
            return treatType;
        }

        @Override
        public EntityType<E> getTreatType() {
            return treatType;
        }

        @Override
        public AbstractPath<? super E> getTreatedPath() {
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
