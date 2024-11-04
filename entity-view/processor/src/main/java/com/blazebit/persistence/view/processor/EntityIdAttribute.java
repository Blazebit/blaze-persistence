/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

/**
 * @author Christian Beikov
 * @since 1.6.8
 */
public class EntityIdAttribute {

    private final String name;
    private final ElementKind kind;
    private final String idAttributeName;
    private final String idMethodName;

    public EntityIdAttribute(Element entityIdAttribute) {
        this.name = entityIdAttribute.getSimpleName().toString();
        this.kind = entityIdAttribute.getKind();
        if (entityIdAttribute.getKind() == ElementKind.METHOD) {
            String methodName = entityIdAttribute.getSimpleName().toString();
            if (methodName.startsWith("is")) {
                this.idAttributeName = Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
            } else {
                this.idAttributeName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
            }
        } else {
            this.idAttributeName = entityIdAttribute.getSimpleName().toString();
        }
        this.idMethodName = "$$_" + entityIdAttribute.getEnclosingElement().getSimpleName().toString() + "_" + idAttributeName;
    }

    public String getName() {
        return name;
    }

    public ElementKind getKind() {
        return kind;
    }

    public String getIdAttributeName() {
        return idAttributeName;
    }

    public String getIdMethodName() {
        return idMethodName;
    }
}
