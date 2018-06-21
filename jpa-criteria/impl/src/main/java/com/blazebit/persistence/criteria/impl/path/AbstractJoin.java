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

import com.blazebit.persistence.criteria.BlazeFetch;
import com.blazebit.persistence.criteria.BlazeFrom;
import com.blazebit.persistence.criteria.BlazeJoin;
import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.ParameterVisitor;
import com.blazebit.persistence.criteria.impl.expression.SubqueryExpression;
import com.blazebit.persistence.criteria.impl.support.JoinSupport;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractJoin<Z, X> extends AbstractFrom<Z, X> implements BlazeJoin<Z, X>, BlazeFetch<Z, X>, JoinSupport<Z, X> {

    private static final long serialVersionUID = 1L;

    protected EntityType<? extends X> treatJoinType;

    private final Attribute<? super Z, ?> joinAttribute;
    private final JoinType joinType;
    private boolean fetch;

    private Predicate suppliedJoinCondition;

    protected AbstractJoin(BlazeCriteriaBuilderImpl criteriaBuilder, AbstractJoin<Z, ? super X> original, EntityType<X> treatType) {
        super(criteriaBuilder, treatType.getJavaType(), original.getBasePath());
        this.joinAttribute = original.getAttribute();
        this.joinType = original.getJoinType();
    }

    public AbstractJoin(BlazeCriteriaBuilderImpl criteriaBuilder, Class<X> javaType, AbstractPath<Z> pathSource, Attribute<? super Z, ?> joinAttribute, JoinType joinType) {
        super(criteriaBuilder, javaType, pathSource);
        this.joinAttribute = joinAttribute;
        this.joinType = joinType;
    }

    @Override
    public void visit(ParameterVisitor visitor) {
        visitor.visit(suppliedJoinCondition);
        super.visit(visitor);
    }

    @Override
    @SuppressWarnings("unchecked")
    public AbstractPath<Z> getBasePath() {
        return (AbstractPath<Z>) super.getBasePath();
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
    @SuppressWarnings({"unchecked"})
    public BlazeFrom<?, Z> getParent() {
        return (BlazeFrom<?, Z>) getBasePath();
    }

    @Override
    @SuppressWarnings({"unchecked"})
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

    public abstract <T extends X> AbstractJoin<Z, T> treatJoin(Class<T> treatType);

    protected final void setTreatType(Class<? extends X> treatType) {
        if (treatType.isAssignableFrom(getJavaType())) {
            return;
        }
        if (treatJoinType != null) {
            throw new IllegalArgumentException("Invalid multiple invocations of treat on join: " + getPathExpression());
        }

        treatJoinType = criteriaBuilder.getEntityMetamodel().entity(treatType);
        setJavaType(treatJoinType.getJavaType());
    }

    @Override
    public abstract <T extends X> AbstractJoin<Z, T> treatAs(Class<T> treatAsType);

    public EntityType<? extends X> getTreatJoinType() {
        return treatJoinType;
    }

    protected final void onPredicates(Predicate... restrictions) {
        if (restrictions != null && restrictions.length > 0) {
            this.suppliedJoinCondition = criteriaBuilder.and(restrictions);
        } else {
            this.suppliedJoinCondition = null;
        }
    }

    protected final void onExpression(Expression<Boolean> restriction) {
        this.suppliedJoinCondition = criteriaBuilder.wrap(restriction);
    }

    @Override
    public Predicate getOn() {
        return suppliedJoinCondition;
    }

}
