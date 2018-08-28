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

package com.blazebit.persistence.spi;

import javax.persistence.metamodel.Attribute;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AttributePath {

    private final List<Attribute<?, ?>> attributes;
    private final Class<?> attributeClass;

    /**
     * Construct a new {@code AttributePath}.
     * @param attributes List of attribute segments
     * @param attributeClass The attribute class
     */
    public AttributePath(List<Attribute<?, ?>> attributes, Class<?> attributeClass) {
        this.attributes = attributes;
        this.attributeClass = attributeClass;
    }

    public List<Attribute<?, ?>> getAttributes() {
        return attributes;
    }

    public Class<?> getAttributeClass() {
        return attributeClass;
    }
}
