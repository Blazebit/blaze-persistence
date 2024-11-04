/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.processor;

import javax.lang.model.element.ExecutableElement;

/**
 * @author Christian Beikov
 * @since 1.6.8
 */
public class EntityViewSpecialMemberMethod {

    private final String name;
    private final String returnTypeName;

    public EntityViewSpecialMemberMethod(ExecutableElement postCreate) {
        this.name = postCreate.getSimpleName().toString();
        this.returnTypeName = postCreate.getReturnType().toString();
    }

    public String getName() {
        return name;
    }

    public String getReturnTypeName() {
        return returnTypeName;
    }
}
