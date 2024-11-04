/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.processor.serialization;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class FieldSerializationField extends SerializationField {

    private final VariableElement field;
    private final String signature;

    public FieldSerializationField(VariableElement field) {
        this.field = field;
        this.signature = getClassSignature(field.asType());
    }

    @Override
    public String getName() {
        return field.getSimpleName().toString();
    }

    @Override
    public Element getElement() {
        return field;
    }

    @Override
    public TypeKind getTypeKind() {
        return field.asType().getKind();
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
        return field.asType().getKind().isPrimitive();
    }
}
