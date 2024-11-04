/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Type;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AttributeHolder {

    private final Attribute<?, ?> attribute;
    private final Type<?> attributeType;

    public AttributeHolder(Attribute<?, ?> attribute, Type<?> attributeType) {
        this.attribute = attribute;
        this.attributeType = attributeType;
    }

    public Attribute<?, ?> getAttribute() {
        return attribute;
    }

    public Type<?> getAttributeType() {
        return attributeType;
    }
}
