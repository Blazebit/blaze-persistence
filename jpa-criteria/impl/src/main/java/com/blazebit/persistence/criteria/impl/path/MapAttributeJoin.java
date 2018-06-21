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

import com.blazebit.persistence.criteria.BlazeMapJoin;
import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.RenderContext;
import com.blazebit.persistence.criteria.impl.expression.SubqueryExpression;
import com.blazebit.persistence.criteria.impl.expression.function.EntryFunction;
import com.blazebit.persistence.criteria.impl.support.MapJoinSupport;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MapAttributeJoin<O, K, V> extends AbstractPluralAttributeJoin<O, Map<K, V>, V> implements BlazeMapJoin<O, K, V>, MapJoinSupport<O, K, V> {

    private static final long serialVersionUID = 1L;

    private MapAttributeJoin(BlazeCriteriaBuilderImpl criteriaBuilder, MapAttributeJoin<O, K, ? super V> original, EntityType<V> treatType) {
        super(criteriaBuilder, original, treatType);
    }

    public MapAttributeJoin(BlazeCriteriaBuilderImpl criteriaBuilder, Class<V> javaType, AbstractPath<O> pathSource, MapAttribute<? super O, K, V> joinAttribute, JoinType joinType) {
        super(criteriaBuilder, javaType, pathSource, joinAttribute, joinType);
    }

    @Override
    @SuppressWarnings({"unchecked"})
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
    @SuppressWarnings({"unchecked"})
    protected AbstractFrom<O, V> createCorrelationDelegate() {
        return new MapAttributeJoin<O, K, V>(criteriaBuilder, getJavaType(), (AbstractPath<O>) getParentPath(), getAttribute(), getJoinType());
    }

    @Override
    public Path<V> value() {
        return this;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Expression<Map.Entry<K, V>> entry() {
        return new EntryFunction(criteriaBuilder, Map.Entry.class, this);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Path<K> key() {
        final MapKeyBasePath<K, V> mapKeyBasePath = new MapKeyBasePath<K, V>(criteriaBuilder, getAttribute().getJavaType(), this, getAttribute());
        final MapKeyAttribute mapKeyAttribute = new MapKeyAttribute(criteriaBuilder, getAttribute());
        return new MapKeyPath(criteriaBuilder, mapKeyBasePath, mapKeyAttribute);
    }

    /* JPA 2.1 support */

    @Override
    public MapAttributeJoin<O, K, V> on(Expression<Boolean> restriction) {
        super.onExpression(restriction);
        return this;
    }

    @Override
    public MapAttributeJoin<O, K, V> on(Predicate... restrictions) {
        super.onPredicates(restrictions);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends V> MapAttributeJoin<O, K, T> treatJoin(Class<T> treatType) {
        setTreatType(treatType);
        return (MapAttributeJoin<O, K, T>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends V> MapAttributeJoin<O, K, T> treatAs(Class<T> treatAsType) {
        // No need to treat if it is already of the proper subtype
        if (treatAsType.isAssignableFrom(getJavaType())) {
            return (MapAttributeJoin<O, K, T>) this;
        }
        return addTreatedPath(new TreatedMapAttributeJoin<O, K, T>(criteriaBuilder, this, getTreatType(treatAsType)));
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static class TreatedMapAttributeJoin<O, K, V> extends MapAttributeJoin<O, K, V> implements TreatedPath<V> {

        private static final long serialVersionUID = 1L;

        private final MapAttributeJoin<O, K, ? super V> treatedJoin;
        private final EntityType<V> treatType;

        public TreatedMapAttributeJoin(BlazeCriteriaBuilderImpl criteriaBuilder, MapAttributeJoin<O, K, ? super V> treatedJoin, EntityType<V> treatType) {
            super(criteriaBuilder, treatedJoin, treatType);
            this.treatedJoin = treatedJoin;
            this.treatType = treatType;
        }

        @Override
        protected ManagedType<V> getManagedType() {
            return treatType;
        }

        @Override
        public EntityType<V> getTreatType() {
            return treatType;
        }

        @Override
        public AbstractPath<? super V> getTreatedPath() {
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
