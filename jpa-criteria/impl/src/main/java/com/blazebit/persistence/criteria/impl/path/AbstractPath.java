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

import com.blazebit.persistence.criteria.BlazeExpression;
import com.blazebit.persistence.criteria.BlazePath;
import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;
import com.blazebit.persistence.criteria.impl.RenderContext;
import com.blazebit.persistence.criteria.impl.expression.AbstractExpression;
import com.blazebit.persistence.criteria.impl.expression.PathTypeExpression;

import javax.persistence.criteria.Path;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractPath<X> extends AbstractExpression<X> implements BlazePath<X> {

    private static final long serialVersionUID = 1L;

    private final AbstractPath<?> basePath;
    private final BlazeExpression<Class<? extends X>> typeExpression;
    private Map<String, Path<?>> attributePathCache;

    @SuppressWarnings({"unchecked"})
    public AbstractPath(BlazeCriteriaBuilderImpl criteriaBuilder, Class<X> javaType, AbstractPath<?> basePath) {
        super(criteriaBuilder, javaType);
        this.basePath = basePath;
        this.typeExpression = new PathTypeExpression(criteriaBuilder, getJavaType(), this);
    }

    public AbstractPath<?> getBasePath() {
        return basePath;
    }

    @Override
    public AbstractPath<?> getParentPath() {
        return getBasePath();
    }

    public abstract <T extends X> AbstractPath<T> treatAs(Class<T> treatAsType);

    protected final <T> EntityType<T> getTreatType(Class<T> type) {
        return criteriaBuilder.getEntityMetamodel().entity(type);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public BlazeExpression<Class<? extends X>> type() {
        return typeExpression;
    }

    public abstract Attribute<?, ?> getAttribute();

    protected abstract Attribute<?, ?> findAttribute(String attributeName);

    protected abstract boolean isDereferencable();

    public String getPathExpression() {
        return getBasePath().getPathExpression() + "." + getAttribute().getName();
    }

    private void checkGet(Attribute<?, ?> attribute) {
        checkDereferenceAllowed();

        if (attribute == null) {
            throw new IllegalArgumentException("Null attribute");
        }
    }

    protected final Path<?> getAttributePath(String attributeName) {
        return attributePathCache == null ? null : attributePathCache.get(attributeName);
    }

    protected final void putAttributePath(String attributeName, Path<?> path) {
        if (attributePathCache == null) {
            attributePathCache = new HashMap<String, Path<?>>();
        }
        attributePathCache.put(attributeName, path);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <Y> BlazePath<Y> get(SingularAttribute<? super X, Y> attribute) {
        checkGet(attribute);
        SingularAttributePath<Y> path = (SingularAttributePath<Y>) getAttributePath(attribute.getName());
        if (path == null) {
            path = new SingularAttributePath<Y>(criteriaBuilder, attribute.getJavaType(), this, attribute);
            putAttributePath(attribute.getName(), path);
        }
        return path;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <E, C extends Collection<E>> BlazeExpression<C> get(PluralAttribute<X, C, E> attribute) {
        checkGet(attribute);
        PluralAttributePath<C> path = (PluralAttributePath<C>) getAttributePath(attribute.getName());
        if (path == null) {
            path = new PluralAttributePath<C>(criteriaBuilder, this, attribute);
            putAttributePath(attribute.getName(), path);
        }
        return path;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <K, V, M extends Map<K, V>> BlazeExpression<M> get(MapAttribute<X, K, V> attribute) {
        checkGet(attribute);
        PluralAttributePath<M> path = (PluralAttributePath<M>) getAttributePath(attribute.getName());
        if (path == null) {
            path = new PluralAttributePath<M>(criteriaBuilder, this, (PluralAttribute<?, M, ?>) attribute);
            putAttributePath(attribute.getName(), path);
        }
        return path;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <Y> BlazePath<Y> get(String attributeName) {
        checkDereferenceAllowed();

        final Attribute<?, ?> attribute = getAttribute(attributeName);

        if (attribute.isCollection()) {
            final PluralAttribute<X, Y, ?> pluralAttribute = (PluralAttribute<X, Y, ?>) attribute;
            if (PluralAttribute.CollectionType.MAP.equals(pluralAttribute.getCollectionType())) {
                return (PluralAttributePath<Y>) this.<Object, Object, Map<Object, Object>>get((MapAttribute) pluralAttribute);
            } else {
                return (PluralAttributePath<Y>) this.get((PluralAttribute) pluralAttribute);
            }
        } else {
            return get((SingularAttribute<X, Y>) attribute);
        }
    }

    protected final Attribute<?, ?> getAttribute(String attributeName) {
        if (attributeName == null) {
            throw new IllegalArgumentException("Null attribute name");
        }

        final Attribute<?, ?> attribute = findAttribute(attributeName);
        // Some old hibernate versions don't throw an exception but return null
        if (attribute == null) {
            throw new IllegalArgumentException("Could not find attribute '" + attributeName + "' in '" + getBasePath().getPathExpression() + "'");
        }
        return attribute;
    }

    public void prepareAlias(RenderContext context) {
        AbstractPath<?> base = getBasePath();
        if (base != null) {
            base.prepareAlias(context);
        }
    }

    public void renderPathExpression(RenderContext context) {
        prepareAlias(context);
        getBasePath().renderPathExpression(context);
        context.getBuffer()
                .append('.')
                .append(getAttribute().getName());
    }

    @Override
    public void render(RenderContext context) {
        AbstractPath<?> base = getBasePath();
        if (base != null) {
            base.renderPathExpression(context);
            context.getBuffer()
                    .append('.')
                    .append(getAttribute().getName());
        } else {
            context.getBuffer().append(getAttribute().getName());
        }
    }

    private void checkDereferenceAllowed() {
        if (!isDereferencable()) {
            throw new IllegalArgumentException("Dereferencing attributes in '" + getBasePath().getPathExpression() + "' is not allowed!");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractPath<?>)) {
            return false;
        }

        AbstractPath<?> that = (AbstractPath<?>) o;

        if (getAttribute() != null ? !getAttribute().equals(that.getAttribute()) : that.getAttribute() != null) {
            return false;
        }
        return getAlias() != null ? getAlias().equals(that.getAlias()) : that.getAlias() == null;
    }

    @Override
    public int hashCode() {
        int result = getAttribute() != null ? getAttribute().hashCode() : 0;
        result = 31 * result + (getAlias() != null ? getAlias().hashCode() : 0);
        return result;
    }

}
