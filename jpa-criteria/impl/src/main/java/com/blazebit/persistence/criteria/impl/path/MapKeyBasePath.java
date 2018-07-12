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

import javax.persistence.criteria.Path;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.MapAttribute;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MapKeyBasePath<K, V> extends AbstractPath<Map<K, V>> implements Path<Map<K, V>> {

    private final MapAttribute<?, K, V> mapAttribute;
    private final MapAttributeJoin<?, K, V> mapJoin;

    public MapKeyBasePath(BlazeCriteriaBuilderImpl criteriaBuilder, Class<Map<K, V>> javaType, MapAttributeJoin<?, K, V> mapJoin, MapAttribute<?, K, V> attribute) {
        super(criteriaBuilder, javaType, null);
        this.mapJoin = mapJoin;
        this.mapAttribute = attribute;
    }

    @Override
    public void prepareAlias(RenderContext context) {
        mapJoin.prepareAlias(context);
    }

    @Override
    public MapAttribute<?, K, V> getAttribute() {
        return mapAttribute;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Bindable<Map<K, V>> getModel() {
        return (Bindable<Map<K, V>>) mapAttribute;
    }

    @Override
    public AbstractPath<?> getParentPath() {
        return mapJoin.getParentPath();
    }

    @Override
    public String getPathExpression() {
        return mapJoin.getPathExpression();
    }

    @Override
    public void renderPathExpression(RenderContext context) {
        if (mapJoin instanceof TreatedPath<?>) {
            ((TreatedPath) mapJoin).getTreatedPath().renderPathExpression(context);
        } else {
            mapJoin.renderPathExpression(context);
        }
    }

    @Override
    protected boolean isDereferencable() {
        return false;
    }

    @Override
    protected Attribute<?, ?> findAttribute(String attributeName) {
        throw new IllegalArgumentException("Map [" + mapJoin.getPathExpression() + "] cannot be dereferenced");
    }

    @Override
    public <T extends Map<K, V>> AbstractPath<T> treatAs(Class<T> treatAsType) {
        throw new UnsupportedOperationException();
    }

}
