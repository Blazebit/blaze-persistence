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

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.view.metamodel.ManagedViewType;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ConstrainedAttribute<T extends AbstractAttribute<?, ?>> {

    private final T attribute;
    private final List<Map.Entry<String, T>> selectionConstrainedAttributes;
    private final Map<ManagedViewType<?>, T> subAttributes;

    @SuppressWarnings("unchecked")
    public ConstrainedAttribute(String constraint, T attribute) {
        this.attribute = attribute;
        this.selectionConstrainedAttributes = new ArrayList<>();
        this.subAttributes = new HashMap<>();
        addSelectionConstraint(constraint, attribute);
    }

    public T getAttribute() {
        return attribute;
    }

    public boolean requiresCaseWhen() {
        return selectionConstrainedAttributes.size() > 1;
    }

    public Collection<Map.Entry<String, T>> getSelectionConstrainedAttributes() {
        return selectionConstrainedAttributes;
    }

    public T getSubAttribute(ManagedViewType<?> viewType) {
        T attribute = subAttributes.get(((ManagedViewTypeImplementor<?>) viewType).getRealType());
        if (attribute == null) {
            return this.attribute;
        }

        return attribute;
    }

    public void addSelectionConstraint(String constraint, T attribute) {
        selectionConstrainedAttributes.add(new AbstractMap.SimpleEntry<>(constraint, attribute));
    }

    public void addSubAttribute(ManagedViewType<?> viewType, T attribute) {
        subAttributes.put(((ManagedViewTypeImplementor<?>) viewType).getRealType(), attribute);
    }
}