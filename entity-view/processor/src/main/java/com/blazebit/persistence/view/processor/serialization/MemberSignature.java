/*
 * Copyright 2014 - 2022 Blazebit.
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
