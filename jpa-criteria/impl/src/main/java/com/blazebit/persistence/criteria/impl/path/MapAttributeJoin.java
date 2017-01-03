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

import java.util.Map;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.MapAttribute;

import com.blazebit.persistence.criteria.BlazeMapJoin;
import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.expression.SubqueryExpression;
import com.blazebit.persistence.criteria.impl.expression.function.EntryFunction;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MapAttributeJoin<O, K, V> extends AbstractPluralAttributeJoin<O, Map<K, V>, V> implements BlazeMapJoin<O, K, V> {

    private static final long serialVersionUID = 1L;

    public MapAttributeJoin(BlazeCriteriaBuilderImpl criteriaBuilder, Class<V> javaType, AbstractPath<O> pathSource, MapAttribute<? super O, K, V> joinAttribute, JoinType joinType) {
        super(criteriaBuilder, javaType, pathSource, joinAttribute, joinType);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public MapAttribute<? super O, K, V> getAttribute() {
        return (MapAttribute<? super O, K, V>) super.getAttribute();
    }

    @Override
    public MapAttribute<? super O, K, V> getModel() {
        return getAttribute();
    }

    @Override
    public final MapAttributeJoin<O, K, V> correlateTo(SubqueryExpression<?> subquery) {
        return (MapAttributeJoin<O, K, V>) super.correlateTo(subquery);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected AbstractFrom<O, V> createCorrelationDelegate() {
        return new MapAttributeJoin<O, K, V>(criteriaBuilder, getJavaType(), (AbstractPath<O>) getParentPath(), getAttribute(), getJoinType());
    }

    @Override
    public Path<V> value() {
        return this;
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public Expression<Map.Entry<K, V>> entry() {
        return new EntryFunction(criteriaBuilder, Map.Entry.class, this);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public Path<K> key() {
        final MapKeyBasePath<K, V> mapKeyBasePath = new MapKeyBasePath<K, V>(criteriaBuilder, getAttribute().getJavaType(), this, getAttribute());
        final MapKeyAttribute mapKeyAttribute = new MapKeyAttribute(criteriaBuilder, getAttribute());
        return new MapKeyPath(criteriaBuilder, mapKeyBasePath, mapKeyAttribute);
    }

    /* JPA 2.1 support */
    
    @Override
    public Predicate getOn() {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public BlazeMapJoin<O, K, V> on(Expression<Boolean> restriction) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public BlazeMapJoin<O, K, V> on(Predicate... restrictions) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
