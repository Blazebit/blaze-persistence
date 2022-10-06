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

import com.blazebit.persistence.view.processor.MetaAttribute;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class MetaSerializationField extends SerializationField {

    private final MetaAttribute attribute;
    private final String signature;

    public MetaSerializationField(MetaAttribute attribute, TypeMirror typeMirror) {
        this.attribute = attribute;
        this.signature = getClassSignature(typeMirror);
    }

    @Override
    public String getName() {
        return attribute.getPropertyName();
    }

    @Override
    public Element getElement() {
        throw new UnsupportedOperationException(getClass().getName());
    }

    @Override
    public TypeKind getTypeKind() {
        return attribute.getTypeKind();
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
        return attribute.isPrimitive();
    }
}
