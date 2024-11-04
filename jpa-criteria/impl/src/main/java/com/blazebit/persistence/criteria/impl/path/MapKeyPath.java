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
import java.io.Serializable;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MapKeyPath<K> extends AbstractPath<K> implements Path<K>, Serializable {

    private final MapKeyAttribute<K> mapKeyAttribute;

    public MapKeyPath(BlazeCriteriaBuilderImpl criteriaBuilder, MapKeyBasePath<K, ?> source, MapKeyAttribute<K> mapKeyAttribute) {
        super(criteriaBuilder, mapKeyAttribute.getJavaType(), source);
        this.mapKeyAttribute = mapKeyAttribute;
    }

    @Override
    public MapKeyBasePath getBasePath() {
        return (MapKeyBasePath) super.getBasePath();
    }

    @Override
    public MapKeyAttribute<K> getAttribute() {
        return mapKeyAttribute;
    }

    private boolean isBasicTypeKey() {
        return Attribute.PersistentAttributeType.BASIC == mapKeyAttribute.getPersistentAttributeType();
    }

    @Override
    protected boolean isDereferencable() {
        return !isBasicTypeKey();
    }

    @Override
    public String getPathExpression() {
        AbstractPath<?> source = getBasePath();
        String name;
        if (source != null) {
            name = source.getPathExpression();
        } else {
            name = getAttribute().getName();
        }

        return "KEY(" + name + ')';
    }

    @Override
    public void renderPathExpression(RenderContext context) {
        render(context);
    }

    @Override
    public void render(RenderContext context) {
        final StringBuilder buffer = context.getBuffer();
        buffer.append("KEY(");

        AbstractPath<?> source = getBasePath();
        if (source != null) {
            source.renderPathExpression(context);
        } else {
            buffer.append(getAttribute().getName());
        }

        buffer.append(')');
    }

    @Override
    protected Attribute<?, ?> findAttribute(String attributeName) {
        if (!isDereferencable()) {
            throw new IllegalArgumentException("Map key [" + getBasePath().getPathExpression() + "] cannot be dereferenced");
        }
        // TODO: we have to add a join node too if we dereference the map key
        throw new UnsupportedOperationException("Not yet supported!");
    }

    @Override
    public Bindable<K> getModel() {
        return mapKeyAttribute;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends K> MapKeyPath<T> treatAs(Class<T> treatAsType) {
        // todo : if key is an entity, this is probably not enough
        return (MapKeyPath<T>) this;
    }
}
