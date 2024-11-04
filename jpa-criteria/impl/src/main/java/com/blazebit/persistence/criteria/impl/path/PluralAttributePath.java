/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.criteria.impl.path;

import com.blazebit.persistence.criteria.impl.BlazeCriteriaBuilderImpl;

import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.PluralAttribute;
import java.io.Serializable;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class PluralAttributePath<X> extends AbstractPath<X> implements Serializable {

    private final PluralAttribute<?, X, ?> attribute;

    public PluralAttributePath(BlazeCriteriaBuilderImpl criteriaBuilder, AbstractPath<?> source, PluralAttribute<?, X, ?> attribute) {
        super(criteriaBuilder, attribute.getJavaType(), source);
        this.attribute = attribute;
    }

    private PluralAttributePath(PluralAttributePath<X> origin, String alias) {
        super(origin.criteriaBuilder, origin.getJavaType(), origin.getBasePath());
        this.attribute = origin.attribute;
        this.setAlias(alias);
    }

    @Override
    public Selection<X> alias(String alias) {
        return new PluralAttributePath<X>(this, alias);
    }

    public PluralAttribute<?, X, ?> getAttribute() {
        return attribute;
    }

    @Override
    protected boolean isDereferencable() {
        // only joined plural attributes can be dereferenced
        return false;
    }

    @Override
    public <T extends X> PluralAttributePath<T> treatAs(Class<T> treatAsType) {
        throw new UnsupportedOperationException(
                "Plural attribute path [" + getBasePath().getPathExpression() + '.'
                        + attribute.getName() + "] cannot be dereferenced"
        );
    }

    @Override
    protected Attribute<?, ?> findAttribute(String attributeName) {
        throw new IllegalArgumentException("Plural attribute paths cannot be dereferenced! Consider joining the path instead!");
    }

    @Override
    public Bindable<X> getModel() {
        throw new IllegalArgumentException("Plural attribute paths are not bindable! Consider joining the path instead to get a bindable path!");
    }
}