/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.impl;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Type;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AttributeHolder {

    private final Attribute<?, ?> attribute;
    private final Type<?> attributeType;

    public AttributeHolder(Attribute<?, ?> attribute, Type<?> attributeType) {
        this.attribute = attribute;
        this.attributeType = attributeType;
    }

    public Attribute<?, ?> getAttribute() {
        return attribute;
    }

    public Type<?> getAttributeType() {
        return attributeType;
    }
}
