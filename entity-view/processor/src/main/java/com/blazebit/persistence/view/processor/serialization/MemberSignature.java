/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.processor.serialization;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class MemberSignature {

    private final Set<Modifier> modifiers;
    private final String name;
    private final String signature;

    public MemberSignature(VariableElement field) {
        modifiers = field.getModifiers();
        name = field.getSimpleName().toString();
        signature = SerializationField.getClassSignature(field.asType());
    }

    public MemberSignature(ExecutableElement meth) {
        modifiers = meth.getModifiers();
        name = meth.getSimpleName().toString();
        signature = SerializationField.getMethodSignature(meth);
    }

    public Set<Modifier> getModifiers() {
        return modifiers;
    }

    public String getName() {
        return name;
    }

    public String getSignature() {
        return signature;
    }
}
