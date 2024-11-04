/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.processor.serialization;

import com.blazebit.persistence.view.processor.MetaAttribute;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class MetaSerializationField extends SerializationField {

    private final MetaAttribute attribute;
    private final String signature;

    public MetaSerializationField(MetaAttribute attribute, TypeMirror typeMirror) {
        this.attribute = attribute;
        this.signature = getClassSignature(typeMirror);
    }

    @Override
    public String getName() {
        return attribute.getPropertyName();
    }

    @Override
    public Element getElement() {
        throw new UnsupportedOperationException(getClass().getName());
    }

    @Override
    public TypeKind getTypeKind() {
        return attribute.getTypeKind();
    }

    @Override
    public char getTypeCode() {
        return signature.charAt(0);
    }

    @Override
    public String getTypeString() {
        return isPrimitive() ? null : signature;
    }

    @Override
    public boolean isPrimitive() {
        return attribute.isPrimitive();
    }
}
