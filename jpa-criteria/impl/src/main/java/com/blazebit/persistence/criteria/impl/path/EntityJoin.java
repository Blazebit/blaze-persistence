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
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;

/**
 * @author Christian Beikov
 * @since 1.3.0
 */
public class EntityJoin<Z, X> extends AbstractJoin<Z, X> {

    private static final long serialVersionUID = 1L;

    private final EntityType<X> model;

    private EntityJoin(BlazeCriteriaBuilderImpl criteriaBuilder, EntityJoin<Z, ? super X> original, EntityType<X> treatType) {
        super(criteriaBuilder, original, treatType);
        this.model = treatType;
    }

    @SuppressWarnings({"unchecked"})
    public EntityJoin(BlazeCriteriaBuilderImpl criteriaBuilder, AbstractPath<Z> pathSource, EntityType<X> entityType, JoinType joinType) {
        super(criteriaBuilder, entityType.getJavaType(), pathSource, null, joinType);
        this.model = entityType;
    }

    @Override
    public EntityJoin<Z, X> correlateTo(SubqueryExpression<?> subquery) {
        return (EntityJoin<Z, X>) super.correlateTo(subquery);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    protected AbstractFrom<Z, X> createCorrelationDelegate() {
        return new EntityJoin<Z, X>(criteriaBuilder, getBasePath(), getModel(), getJoinType());
    }

    @Override
    protected boolean isJoinAllowed() {
        return true;
    }

    @SuppressWarnings("unchecked")
    public EntityType<X> getModel() {
        if (treatJoinType != null) {
            return (EntityType<X>) treatJoinType;
        }
        return model;
    }

    /* JPA 2.1 support */

    @Override
    public EntityJoin<Z, X> on(Expression<Boolean> restriction) {
        super.onExpression(restriction);
        return this;
    }

    @Override
    public EntityJoin<Z, X> on(Predicate... restrictions) {
        super.onPredicates(restrictions);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends X> EntityJoin<Z, T> treatJoin(Class<T> treatType) {
        setTreatType(treatType);
        return (EntityJoin<Z, T>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends X> EntityJoin<Z, T> treatAs(Class<T> treatAsType) {
        // No need to treat if it is already of the proper subtype
        if (treatAsType.isAssignableFrom(getJavaType())) {
            return (EntityJoin<Z, T>) this;
        }
        return addTreatedPath(new TreatedEntityJoin<Z, T>(criteriaBuilder, this, getTreatType(treatAsType)));
    }

    /**
     * @author Christian Beikov
     * @since 1.3.0
     */
    public static class TreatedEntityJoin<Z, X> extends EntityJoin<Z, X> implements TreatedPath<X> {

        private static final long serialVersionUID = 1L;

        private final EntityJoin<?, ? super X> treatedJoin;
        private final EntityType<X> treatType;

        public TreatedEntityJoin(BlazeCriteriaBuilderImpl criteriaBuilder, EntityJoin<Z, ? super X> treatedJoin, EntityType<X> treatType) {
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
