/*
 * Copyright 2014 - 2024 Blazebit.
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
