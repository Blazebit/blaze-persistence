/*
 * Copyright 2014 - 2023 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
