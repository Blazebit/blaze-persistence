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
