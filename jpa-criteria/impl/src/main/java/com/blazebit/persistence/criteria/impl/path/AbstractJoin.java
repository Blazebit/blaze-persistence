/*
 * Copyright 2014 - 2017 Blazebit.
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

import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.Attribute;

import com.blazebit.persistence.criteria.BlazeFetch;
import com.blazebit.persistence.criteria.BlazeFrom;
import com.blazebit.persistence.criteria.BlazeJoin;
import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.expression.SubqueryExpression;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractJoin<Z, X> extends AbstractFrom<Z, X> implements BlazeJoin<Z, X>, BlazeFetch<Z,X> {

    private static final long serialVersionUID = 1L;
    
    private final Attribute<? super Z, ?> joinAttribute;
    private final JoinType joinType;
    private boolean fetch;

    public AbstractJoin(BlazeCriteriaBuilderImpl criteriaBuilder, Class<X> javaType, AbstractPath<Z> pathSource, Attribute<? super Z, ?> joinAttribute, JoinType joinType) {
        super(criteriaBuilder, javaType, pathSource);
        this.joinAttribute = joinAttribute;
        this.joinType = joinType;
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
    @SuppressWarnings({ "unchecked" })
    public BlazeFrom<?, Z> getParent() {
        return (BlazeFrom<?, Z>) getBasePath();
    }

    @Override
    @SuppressWarnings({ "unchecked" })
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
}
