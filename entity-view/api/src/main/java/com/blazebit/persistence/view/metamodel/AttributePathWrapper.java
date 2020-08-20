/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.view.metamodel;

import java.util.Collection;
import java.util.List;

/**
 * A wrapper for an attribute path.
 *
 * @param <X> The type of the entity view that is the base of the path
 * @param <B> The result base type of attribute path to resolve against
 * @param <Y> The result type of attribute path
 * @author Christian Beikov
 * @since 1.5.0
 */
public abstract class AttributePathWrapper<X, B, Y> implements AttributePath<X, B, Y> {

    private final AttributePath<X, B, Y> wrapped;

    /**
     * Creates a new wrapper.
     *
     * @param wrapped The wrapped path.
     */
    public AttributePathWrapper(AttributePath<X, B, Y> wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * Returns the wrapped path.
     *
     * @return the wrapped path
     */
    public AttributePath<X, B, Y> getWrapped() {
        return wrapped;
    }

    @Override
    public String getPath() {
        return wrapped.getPath();
    }

    @Override
    public List<String> getAttributeNames() {
        return wrapped.getAttributeNames();
    }

    @Override
    public List<MethodAttribute<?, ?>> getAttributes() {
        return wrapped.getAttributes();
    }

    @Override
    public <E> AttributePath<X, E, E> get(String attributePath) {
        return wrapped.get(attributePath);
    }

    @Override
    public <E, C extends Collection<E>> AttributePath<X, E, C> getMulti(String attributePath) {
        return wrapped.getMulti(attributePath);
    }

    @Override
    public <E> AttributePath<X, E, E> get(MethodSingularAttribute<B, E> attribute) {
        return wrapped.get(attribute);
    }

    @Override
    public <E> AttributePath<X, E, E> get(MethodPluralAttribute<B, ?, E> attribute) {
        return wrapped.get(attribute);
    }

    @Override
    public <C extends Collection<E>, E> AttributePath<X, E, C> get(MethodMultiListAttribute<B, E, C> attribute) {
        return wrapped.get(attribute);
    }

    @Override
    public <C extends Collection<E>, E> AttributePath<X, E, C> get(MethodMultiMapAttribute<B, ?, E, C> attribute) {
        return wrapped.get(attribute);
    }

    @Override
    public <C, E> AttributePath<X, C, E> get(AttributePath<B, C, E> attributePath) {
        return wrapped.get(attributePath);
    }

    @Override
    public MethodAttribute<?, Y> resolve(ManagedViewType<X> baseType) {
        return wrapped.resolve(baseType);
    }

    @Override
    public List<MethodAttribute<?, ?>> resolveAll(ManagedViewType<X> baseType) {
        return wrapped.resolveAll(baseType);
    }
}
