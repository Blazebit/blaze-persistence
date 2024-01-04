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

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.view.metamodel.ManagedViewType;

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
    private final int index;
    private final List<Entry<T>> selectionConstrainedAttributes;
    private final Map<ManagedViewType<?>, T> subAttributes;

    @SuppressWarnings("unchecked")
    public ConstrainedAttribute(String constraint, int[] subtypeIndexes, T attribute, int index) {
        this.attribute = attribute;
        this.index = index;
        this.selectionConstrainedAttributes = new ArrayList<>();
        this.subAttributes = new HashMap<>();
        addSelectionConstraint(constraint, subtypeIndexes, attribute);
    }

    public T getAttribute() {
        return attribute;
    }

    public int getIndex() {
        return index;
    }

    public boolean requiresCaseWhen() {
        return selectionConstrainedAttributes.size() > 1;
    }

    public Collection<Entry<T>> getSelectionConstrainedAttributes() {
        return selectionConstrainedAttributes;
    }

    public T getSubAttribute(ManagedViewType<?> viewType) {
        T attribute = subAttributes.get(((ManagedViewTypeImplementor<?>) viewType).getRealType());
        if (attribute == null) {
            return this.attribute;
        }

        return attribute;
    }

    public void addSelectionConstraint(String constraint, int[] subtypeIndexes, T attribute) {
        selectionConstrainedAttributes.add(new Entry<>(constraint, subtypeIndexes, attribute));
    }

    public void addSubAttribute(ManagedViewType<?> viewType, T attribute) {
        subAttributes.put(((ManagedViewTypeImplementor<?>) viewType).getRealType(), attribute);
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.3.0
     */
    public static class Entry<T> {
        private final String constraint;
        private final int[] subtypeIndexes;
        private final T attribute;

        public Entry(String constraint, int[] subtypeIndexes, T attribute) {
            this.constraint = constraint;
            this.subtypeIndexes = subtypeIndexes;
            this.attribute = attribute;
        }

        public String getConstraint() {
            return constraint;
        }

        public int getSubtypeIndex() {
            return subtypeIndexes[0];
        }

        public int[] getSubtypeIndexes() {
            return subtypeIndexes;
        }

        public T getAttribute() {
            return attribute;
        }
    }
}