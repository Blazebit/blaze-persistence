/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spi;

import jakarta.persistence.metamodel.Attribute;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AttributePath {

    private final List<Attribute<?, ?>> attributes;
    private final Class<?> attributeClass;

    /**
     * Construct a new {@code AttributePath}.
     * @param attributes List of attribute segments
     * @param attributeClass The attribute class
     */
    public AttributePath(List<Attribute<?, ?>> attributes, Class<?> attributeClass) {
        this.attributes = attributes;
        this.attributeClass = attributeClass;
    }

    public List<Attribute<?, ?>> getAttributes() {
        return attributes;
    }

    public Class<?> getAttributeClass() {
        return attributeClass;
    }
}
