/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.processor;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.6.8
 */
public class JavaType {

    protected final String name;
    private final List<JavaType> typeArguments;

    public JavaType(String name) {
        this.name = name;
        this.typeArguments = Collections.emptyList();
    }

    public JavaType(TypeMirror typeMirror) {
        if (typeMirror instanceof TypeVariable) {
            TypeVariable typeVariable = (TypeVariable) typeMirror;
            typeMirror = typeVariable.getUpperBound();
        }
        if (typeMirror instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) typeMirror;
            if (wildcardType.getExtendsBound() == null) {
                this.name = "? super " + wildcardType.getSuperBound().toString();
            } else {
                this.name = "? extends " + wildcardType.getExtendsBound().toString();
            }
            this.typeArguments = Collections.emptyList();
        } else {
            DeclaredType filterValueType = (DeclaredType) typeMirror;
            TypeElement filterValueTypeElement = (TypeElement) filterValueType.asElement();
            this.name = filterValueTypeElement.getQualifiedName().toString();
            List<? extends TypeMirror> typeArguments = filterValueType.getTypeArguments();
            List<JavaType> arguments = new ArrayList<>(typeArguments.size());
            for (TypeMirror typeArgument : typeArguments) {
                arguments.add(new JavaType(typeArgument));
            }
            this.typeArguments = arguments;
        }
    }

    public void append(ImportContext importContext, StringBuilder sb) {
        sb.append(importContext.importType(name));
        if (!typeArguments.isEmpty()) {
            sb.append("<");
            for (JavaType typeArgument : typeArguments) {
                typeArgument.append(importContext, sb);
                sb.append(", ");
            }
            sb.setLength(sb.length() - 2);
            sb.append(">");
        }
    }
}
