/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.processor;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class AttributeFilter {

    private final String name;
    private final JavaType filterValueType;

    public AttributeFilter(String name, TypeMirror filterProvider, String attributeType, Context context) {
        this.name = name;
        TypeMirror typeMirror = TypeUtils.asMemberOf(context, (DeclaredType) filterProvider, context.getTypeElement(Constants.ATTRIBUTE_FILTER_PROVIDER).getTypeParameters().get(0));

        if (typeMirror.getKind() == TypeKind.TYPEVAR || "java.lang.Object".equals(typeMirror.toString())) {
            this.filterValueType = new JavaType(attributeType);
        } else {
            this.filterValueType = new JavaType(typeMirror);
        }
    }

    public String getName() {
        return name;
    }

    public JavaType getFilterValueType() {
        return filterValueType;
    }
}
