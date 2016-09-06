package com.blazebit.persistence.impl;

import javax.persistence.metamodel.Attribute;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
class AttributeJoinResult {

    private final Attribute<?, ?> attribute;
    private final Class<?> containingClass;

    public AttributeJoinResult(Attribute<?, ?> attribute, Class<?> containingClass) {
        this.attribute = attribute;
        this.containingClass = containingClass;
    }

    public Attribute<?, ?> getAttribute() {
        return attribute;
    }

    public Class<?> getContainingClass() {
        return containingClass;
    }
}
