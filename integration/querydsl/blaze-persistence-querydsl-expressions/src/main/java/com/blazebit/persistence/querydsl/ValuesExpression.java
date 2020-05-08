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

package com.blazebit.persistence.querydsl;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Visitor;

import javax.annotation.Nullable;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;

/**
 * An expression type that represents a {@code VALUES} clause that can be used as {@code FROM} expression.
 *
 * @param <T> Value type
 * @author Jan-Willem Gmelig Meyling
 * @since 1.5.0
 */
public class ValuesExpression<T> implements Path<T> {

    private static final long serialVersionUID = 575334247890043563L;

    private final Path<T> entityPath;
    private final Path<T> alias;
    private final Collection<T> elements;
    private final boolean identifiable;

    /**
     * Construct a new {@code ValuesExpression}.
     *
     * @param entityPath Entity path or attribute path that determines the value type
     * @param elements The values used
     * @param identifiable Whether to only bind the id attribute for entity types, or all managed attributes
     */
    public ValuesExpression(Path<T> entityPath, Collection<T> elements, boolean identifiable) {
        this.entityPath = this.alias = entityPath;
        this.elements = elements;
        this.identifiable = identifiable;
    }

    /**
     * Construct a new {@code ValuesExpression}.
     *
     * @param entityPath Entity path or attribute path that determines the value type
     * @param alias The alias for the source
     * @param elements The values used
     * @param identifiable Whether to only bind the id attribute for entity types, or all managed attributes
     */
    public ValuesExpression(Path<T> entityPath, Path<T> alias, Collection<T> elements, boolean identifiable) {
        this.entityPath = entityPath;
        this.alias = alias;
        this.elements = elements;
        this.identifiable = identifiable;
    }

    @Override
    public PathMetadata getMetadata() {
        return entityPath.getMetadata();
    }

    @Override
    public Path<?> getRoot() {
        return entityPath.getRoot();
    }

    @Override
    public AnnotatedElement getAnnotatedElement() {
        return entityPath.getAnnotatedElement();
    }

    @Override
    @Nullable
    public <R, C> R accept(Visitor<R, C> v, @Nullable C context) {
        return entityPath.accept(v, context);
    }

    @Override
    public Class<? extends T> getType() {
        return entityPath.getType();
    }

    /**
     * Returns the elements in this {@code VALUES} clause.
     *
     * @return the elements in this {@code VALUES} clause
     */
    public Collection<T> getElements() {
        return elements;
    }

    /**
     * Returns whether this represents an identifiable {@code VALUES} clause.
     *
     * @return true iff this {@code VALUES} clause should bind only the id attribute
     */
    public boolean isIdentifiable() {
        return identifiable;
    }

    /**
     * Retrieve the alias under which this {@code ValuesExpression} is bound.
     *
     * @return the alias
     */
    public Path<T> getAlias() {
        return alias;
    }
}
