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

import com.blazebit.persistence.criteria.BlazeListJoin;
import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.RenderContext;
import com.blazebit.persistence.criteria.impl.expression.SubqueryExpression;
import com.blazebit.persistence.criteria.impl.expression.function.IndexFunction;
import com.blazebit.persistence.criteria.impl.support.ListJoinSupport;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.ManagedType;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ListAttributeJoin<O, E> extends AbstractPluralAttributeJoin<O, List<E>, E> implements BlazeListJoin<O, E>, ListJoinSupport<O, E> {

    private static final long serialVersionUID = 1L;

    private ListAttributeJoin(BlazeCriteriaBuilderImpl criteriaBuilder, ListAttributeJoin<O, ? super E> original, EntityType<E> treatType) {
        super(criteriaBuilder, original, treatType);
    }

    public ListAttributeJoin(BlazeCriteriaBuilderImpl criteriaBuilder, Class<E> javaType, AbstractPath<O> pathSource, ListAttribute<? super O, E> joinAttribute, JoinType joinType) {
        super(criteriaBuilder, javaType, pathSource, joinAttribute, joinType);
    }

    @Override
    public Expression<Integer> index() {
        return new IndexFunction(criteriaBuilder, this);
    }

    @Override
    @SuppressWarnings({"unchecked"})
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
    @SuppressWarnings({"unchecked"})
    protected AbstractFrom<O, E> createCorrelationDelegate() {
        return new ListAttributeJoin<O, E>(criteriaBuilder, getJavaType(), (AbstractPath<O>) getParentPath(), getAttribute(), getJoinType());
    }

    /* JPA 2.1 support */

    @Override
    public ListAttributeJoin<O, E> on(Expression<Boolean> restriction) {
        super.onExpression(restriction);
        return this;
    }

    @Override
    public ListAttributeJoin<O, E> on(Predicate... restrictions) {
        super.onPredicates(restrictions);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends E> ListAttributeJoin<O, T> treatJoin(Class<T> treatType) {
        setTreatType(treatType);
        return (ListAttributeJoin<O, T>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends E> ListAttributeJoin<O, T> treatAs(Class<T> treatAsType) {
        // No need to treat if it is already of the proper subtype
        if (treatAsType.isAssignableFrom(getJavaType())) {
            return (ListAttributeJoin<O, T>) this;
        }
        return addTreatedPath(new TreatedListAttributeJoin<>(criteriaBuilder, this, getTreatType(treatAsType)));
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static class TreatedListAttributeJoin<O, E> extends ListAttributeJoin<O, E> implements TreatedPath<E> {

        private static final long serialVersionUID = 1L;

        private final ListAttributeJoin<?, ? super E> treatedJoin;
        private final EntityType<E> treatType;

        public TreatedListAttributeJoin(BlazeCriteriaBuilderImpl criteriaBuilder, ListAttributeJoin<O, ? super E> treatedJoin, EntityType<E> treatType) {
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
