/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.path;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.RenderContext;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.Bindable;
import jakarta.persistence.metamodel.MapAttribute;
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
    public String resolveAlias(RenderContext context) {
        return mapJoin.resolveAlias(context);
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
            ((TreatedPath<?>) mapJoin).getTreatedPath().renderPathExpression(context);
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
