package com.blazebit.persistence.impl;

import javax.persistence.metamodel.Attribute;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AttributePath {

    private final List<Attribute<?, ?>> attributes;
    private final Class<?> attributeClass;

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
