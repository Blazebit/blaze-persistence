/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.processor;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.6.8
 */
public class EntityViewLifecycleMethod {

    private final String name;
    private final String declaringTypeName;
    private final List<String> parameterTypes;
    private final boolean needsReflectionCall;

    public EntityViewLifecycleMethod(ExecutableElement postCreate) {
        this.name = postCreate.getSimpleName().toString();
        TypeElement declaringType = (TypeElement) postCreate.getEnclosingElement();
        this.declaringTypeName = declaringType.getQualifiedName().toString();
        List<? extends VariableElement> parameters = postCreate.getParameters();
        List<String> parameterTypes = new ArrayList<>(parameters.size());
        for (VariableElement parameter : parameters) {
            parameterTypes.add(parameter.asType().toString());
        }
        this.parameterTypes = parameterTypes;
        Set<Modifier> modifiers = postCreate.getModifiers();
        this.needsReflectionCall = !modifiers.contains(Modifier.PUBLIC) && !modifiers.contains(Modifier.PROTECTED);
    }

    public String getName() {
        return name;
    }

    public String getDeclaringTypeName() {
        return declaringTypeName;
    }

    public List<String> getParameterTypes() {
        return parameterTypes;
    }

    public boolean needsReflectionCall() {
        return needsReflectionCall;
    }
}
