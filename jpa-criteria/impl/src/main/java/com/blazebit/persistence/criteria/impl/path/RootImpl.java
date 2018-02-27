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

import com.blazebit.persistence.criteria.BlazeRoot;
import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.RenderContext;
import com.blazebit.persistence.criteria.impl.expression.SubqueryExpression;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import java.io.Serializable;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class RootImpl<X> extends AbstractFrom<X, X> implements BlazeRoot<X>, Serializable {

    private static final long serialVersionUID = 1L;

    private final EntityType<X> entityType;
    private final boolean joinsAllowed;

    private RootImpl(BlazeCriteriaBuilderImpl criteriaBuilder, EntityType<X> entityType, boolean joinsAllowed) {
        super(criteriaBuilder, entityType.getJavaType());
        this.entityType = entityType;
        this.joinsAllowed = joinsAllowed;
    }

    public RootImpl(BlazeCriteriaBuilderImpl criteriaBuilder, EntityType<X> entityType, String alias, boolean joinsAllowed) {
        super(criteriaBuilder, entityType.getJavaType());
        this.entityType = entityType;
        this.setAlias(alias);
        this.joinsAllowed = joinsAllowed;
    }

    public EntityType<X> getEntityType() {
        return entityType;
    }

    public EntityType<X> getModel() {
        return getEntityType();
    }

    @Override
    protected AbstractFrom<X, X> createCorrelationDelegate() {
        return new RootImpl<X>(criteriaBuilder, getEntityType(), getAlias(), true);
    }

    @Override
    public RootImpl<X> correlateTo(SubqueryExpression<?> subquery) {
        return (RootImpl<X>) super.correlateTo(subquery);
    }

    @Override
    protected boolean isJoinAllowed() {
        return joinsAllowed;
    }

    @Override
    protected void checkJoinAllowed() {
        if (!joinsAllowed) {
            throw new IllegalArgumentException("Update and delete criteria queries cannot have joins");
        }
        super.checkJoinAllowed();
    }

    @Override
    protected void checkFetchAllowed() {
        if (!joinsAllowed) {
            throw new IllegalArgumentException("Update and delete criteria queries cannot have join fetches");
        }
        super.checkFetchAllowed();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends X> RootImpl<T> treatAs(Class<T> treatAsType) {
        // No need to treat if it is already of the proper subtype
        if (treatAsType.isAssignableFrom(getJavaType())) {
            return (RootImpl<T>) this;
        }
        return addTreatedPath(new TreatedRoot<T>(criteriaBuilder, this, getTreatType(treatAsType)));
    }

    @Override
    public String getPathExpression() {
        return getAlias();
    }

    @Override
    public void renderPathExpression(RenderContext context) {
        prepareAlias(context);
        context.getBuffer().append(getAlias());
    }

    @Override
    public void render(RenderContext context) {
        prepareAlias(context);
        context.getBuffer().append(getAlias());
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static class TreatedRoot<X> extends RootImpl<X> implements TreatedPath<X> {

        private static final long serialVersionUID = 1L;

        private final RootImpl<? super X> treatedRoot;
        private final EntityType<X> treatType;

        public TreatedRoot(BlazeCriteriaBuilderImpl criteriaBuilder, RootImpl<? super X> treatedRoot, EntityType<X> treatType) {
            super(criteriaBuilder, treatType, treatedRoot.isJoinAllowed());
            this.treatedRoot = treatedRoot;
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
            return treatedRoot;
        }

        @Override
        public String getAlias() {
            return treatedRoot.getAlias();
        }

        @Override
        protected AbstractFrom<X, X> createCorrelationDelegate() {
            return new TreatedRoot<X>(criteriaBuilder, treatedRoot, treatType);
        }

        @Override
        public TreatedRoot<X> correlateTo(SubqueryExpression<?> subquery) {
            return (TreatedRoot<X>) super.correlateTo(subquery);
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
