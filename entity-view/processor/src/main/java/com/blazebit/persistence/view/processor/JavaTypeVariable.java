/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.processor;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVariable;

/**
 * @author Christian Beikov
 * @since 1.6.8
 */
public class JavaTypeVariable {

    private final String name;
    private final boolean isExtends;
    private final JavaType bound;

    public JavaTypeVariable(TypeVariable typeVariable) {
        this.name = typeVariable.toString();
        this.isExtends = typeVariable.getLowerBound().getKind() == TypeKind.NULL;
        if (isExtends) {
            this.bound = new JavaType(typeVariable.getUpperBound());
        } else {
            this.bound = new JavaType(typeVariable.getLowerBound());
        }
    }

    public String getName() {
        return name;
    }

    public void append(ImportContext importContext, StringBuilder sb) {
        sb.append(name);
        if (isExtends) {
            sb.append(" extends ");
        } else {
            sb.append(" super ");
        }
        bound.append(importContext, sb);
    }
}
