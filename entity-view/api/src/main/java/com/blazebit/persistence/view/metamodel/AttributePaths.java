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

package com.blazebit.persistence.view.metamodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A util class for creating attribute paths.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public final class AttributePaths {

    private AttributePaths() {
    }

    /**
     * Creates an attribute path for the given path string.
     *
     * @param attributePath The attribute path string
     * @param <X> The source type
     * @param <E> The element type
     * @return The attribute path
     */
    public static <X, E> AttributePath<X, E, E> of(String attributePath) {
        return new StringAttributePath<>(attributePath);
    }

    /**
     * Creates an attribute path for the given attribute.
     *
     * @param attribute The attribute
     * @param <X> The source type
     * @param <E> The element type
     * @return The attribute path
     */
    public static <X, E> AttributePath<X, E, E> of(MethodSingularAttribute<X, E> attribute) {
        return new TypedAttributePath<>(attribute);
    }

    /**
     * Creates an attribute path for the given attribute.
     *
     * @param attribute The attribute
     * @param <X> The source type
     * @param <E> The element type
     * @return The attribute path
     */
    public static <X, E> AttributePath<X, E, E> of(MethodPluralAttribute<X, ?, E> attribute) {
        return new TypedAttributePath<>(attribute);
    }

    /**
     * Creates an attribute path for the given multi-list attribute.
     *
     * @param attribute The attribute
     * @param <X> The source type
     * @param <C> The element type to resolve against
     * @param <E> The element type
     * @return The attribute path
     */
    public static <X, C extends Collection<E>, E> AttributePath<X, C, E> of(MethodMultiListAttribute<X, E, C> attribute) {
        return new TypedAttributePath<>(attribute);
    }

    /**
     * Creates an attribute path for the given multi-map attribute.
     *
     * @param attribute The attribute
     * @param <X> The source type
     * @param <C> The element type to resolve against
     * @param <E> The element type
     * @return The attribute path
     */
    public static <X, C extends Collection<E>, E> AttributePath<X, C, E> of(MethodMultiMapAttribute<X, ?, E, C> attribute) {
        return new TypedAttributePath<>(attribute);
    }

    private static String getAttributePathForNames(List<String> attributes) {
        StringBuilder sb = new StringBuilder();
        sb.append(attributes.get(0));
        for (int i = 1; i < attributes.size(); i++) {
            sb.append('.').append(attributes.get(i));
        }

        return sb.toString();
    }

    private static String getAttributePathForAttributes(List<? extends MethodAttribute<?, ?>> attributes) {
        StringBuilder sb = new StringBuilder();
        sb.append(attributes.get(0).getName());
        for (int i = 1; i < attributes.size(); i++) {
            sb.append('.').append(attributes.get(i).getName());
        }

        return sb.toString();
    }

    /**
     * An typed attribute path.
     *
     * @param <X> The source type
     * @param <B> The result base type of attribute path to resolve against
     * @param <Y> The result type
     * @author Christian Beikov
     * @since 1.5.0
     */
    private static final class StringAttributePath<X, B, Y> implements AttributePath<X, B, Y> {

        private final List<String> attributes;

        private StringAttributePath(String attributePath) {
            List<String> attributes = Arrays.asList(attributePath.split("\\."));
            this.attributes = Collections.unmodifiableList(attributes);
        }

        private StringAttributePath(StringAttributePath<X, ?, ?> base, String attributePath) {
            String[] parts = attributePath.split("\\.");
            List<String> attributes = new ArrayList<>(base.attributes.size() + parts.length);
            attributes.addAll(base.attributes);
            for (String part : parts) {
                attributes.add(part);
            }
            this.attributes = Collections.unmodifiableList(attributes);
        }

        private StringAttributePath(StringAttributePath<X, ?, ?> base, AttributePath<?, B, Y> sub) {
            List<String> attributeNames = sub.getAttributeNames();
            List<String> attributes = new ArrayList<>(base.attributes.size() + attributeNames.size());
            attributes.addAll(base.attributes);
            attributes.addAll(attributeNames);
            this.attributes = Collections.unmodifiableList(attributes);
        }

        @Override
        public MethodAttribute<?, Y> resolve(ManagedViewType<X> baseType) {
            MethodAttribute<?, ?> lastAttribute = null;
            Type<?> type = baseType;
            for (String part : attributes) {
                if (type instanceof ManagedViewType<?>) {
                    lastAttribute = ((ManagedViewType<?>) type).getAttribute(part);
                    if (lastAttribute == null) {
                        throw new IllegalArgumentException("Error de-referencing attribute '" + part + "' because it does not exist on the type '" + type.getJavaType().getName() + "' while resolving attribute path: " + getAttributePathForNames(attributes));
                    }
                    if (lastAttribute instanceof MethodSingularAttribute<?, ?>) {
                        type = ((MethodSingularAttribute<?, ?>) lastAttribute).getType();
                    } else {
                        type = ((MethodPluralAttribute<?, ?, ?>) lastAttribute).getElementType();
                    }
                } else {
                    throw new IllegalArgumentException("Can not de-reference attribute '" + part + "' on non-managed type '" + type.getJavaType().getName() + "' while resolving attribute path: " + getAttributePathForNames(attributes));
                }
            }
            return (MethodAttribute<?, Y>) lastAttribute;
        }

        @Override
        public List<MethodAttribute<?, ?>> resolveAll(ManagedViewType<X> baseType) {
            List<MethodAttribute<?, ?>> list = new ArrayList<>(attributes.size());
            Type<?> type = baseType;
            for (String part : attributes) {
                if (type instanceof ManagedViewType<?>) {
                    MethodAttribute<?, ?> attribute = ((ManagedViewType<?>) type).getAttribute(part);
                    if (attribute == null) {
                        throw new IllegalArgumentException("Error de-referencing attribute '" + part + "' because it does not exist on the type '" + type.getJavaType().getName() + "' while resolving attribute path: " + getAttributePathForNames(attributes));
                    }
                    list.add(attribute);
                    if (attribute instanceof MethodSingularAttribute<?, ?>) {
                        type = ((MethodSingularAttribute<?, ?>) attribute).getType();
                    } else {
                        type = ((MethodPluralAttribute<?, ?, ?>) attribute).getElementType();
                    }
                } else {
                    throw new IllegalArgumentException("Can not de-reference attribute '" + part + "' on non-managed type '" + type.getJavaType().getName() + "' while resolving attribute path: " + getAttributePathForNames(attributes));
                }
            }
            return list;
        }

        @Override
        public List<String> getAttributeNames() {
            return attributes;
        }

        @Override
        public List<MethodAttribute<?, ?>> getAttributes() {
            throw new UnsupportedOperationException("String based attribute path can't provide attributes! Use resolveAll instead!");
        }

        @Override
        public <E> AttributePath<X, E, E> get(String attributePath) {
            return new StringAttributePath<>(this, attributePath);
        }

        @Override
        public <E, C extends Collection<E>> AttributePath<X, E, C> getMulti(String attributePath) {
            return new StringAttributePath<>(this, attributePath);
        }

        @Override
        public <E> AttributePath<X, E, E> get(MethodSingularAttribute<B, E> attribute) {
            return new StringAttributePath<>(this, attribute.getName());
        }

        @Override
        public <E> AttributePath<X, E, E> get(MethodPluralAttribute<B, ?, E> attribute) {
            return new StringAttributePath<>(this, attribute.getName());
        }

        @Override
        public <C extends Collection<E>, E> AttributePath<X, E, C> get(MethodMultiListAttribute<B, E, C> attribute) {
            return new StringAttributePath<>(this, attribute.getName());
        }

        @Override
        public <C extends Collection<E>, E> AttributePath<X, E, C> get(MethodMultiMapAttribute<B, ?, E, C> attribute) {
            return new StringAttributePath<>(this, attribute.getName());
        }

        @Override
        public <C,E> AttributePath<X, C, E> get(AttributePath<B, C, E> attributePath) {
            return new StringAttributePath<>(this, attributePath);
        }

        @Override
        public String getPath() {
            return getAttributePathForNames(attributes);
        }

        @Override
        public String toString() {
            return getAttributePathForNames(attributes);
        }
    }

    /**
     * A typed attribute path.
     *
     * @param <X> The source type
     * @param <B> The result base type of attribute path to resolve against
     * @param <Y> The result type
     * @author Christian Beikov
     * @since 1.5.0
     */
    private static final class TypedAttributePath<X, B, Y> implements AttributePath<X, B, Y> {

        private final List<MethodAttribute<?, ?>> attributes;

        private TypedAttributePath(MethodAttribute<X, ?> attribute) {
            this.attributes = (List<MethodAttribute<?, ?>>) (List<?>) Collections.singletonList(attribute);
        }

        private TypedAttributePath(TypedAttributePath<X, ?, ?> base, String attributePath) {
            this(base, Arrays.asList(attributePath.split("\\.")));
        }

        private TypedAttributePath(TypedAttributePath<X, ?, ?> base, List<String> attributeNames) {
            int size = base.attributes.size();
            List<MethodAttribute<?, ?>> attributes = new ArrayList<>(size + attributeNames.size());
            attributes.addAll(base.attributes);
            MethodAttribute<?, ?> lastAttribute = base.attributes.get(size - 1);
            for (String part : attributeNames) {
                Type<?> type;
                if (lastAttribute instanceof MethodSingularAttribute<?, ?>) {
                    type = ((MethodSingularAttribute<?, ?>) lastAttribute).getType();
                } else {
                    type = ((MethodPluralAttribute<?, ?, ?>) lastAttribute).getElementType();
                }
                if (type instanceof ManagedViewType<?>) {
                    MethodAttribute<?, ?> attribute = ((ManagedViewType<?>) type).getAttribute(part);
                    if (attribute == null) {
                        throw new IllegalArgumentException("Error de-referencing attribute '" + part + "' because it does not exist on the type '" + type.getJavaType().getName() + "' while creating attribute path for: " + getAttributePathForNames(attributeNames));
                    }
                    attributes.add(lastAttribute = attribute);
                } else {
                    throw new IllegalArgumentException("Can not de-reference attribute '" + part + "' on non-managed type '" + type.getJavaType().getName() + "' for attribute path: " + getAttributePathForNames(attributeNames));
                }
            }
            this.attributes = Collections.unmodifiableList(attributes);
        }

        private TypedAttributePath(TypedAttributePath<X, ?, ?> base, List<? extends MethodAttribute<?, ?>> subAttributes, boolean noop) {
            int size = base.attributes.size();
            MethodAttribute<?, ?> lastAttribute = base.attributes.get(size - 1);
            List<MethodAttribute<?, ?>> attributes = new ArrayList<>(size + 1);
            attributes.addAll(base.attributes);
            for (int i = 0; i < subAttributes.size(); i++) {
                MethodAttribute<?, ?> subAttribute = subAttributes.get(i);
                String part = subAttribute.getName();
                Type<?> type;
                if (lastAttribute instanceof MethodSingularAttribute<?, ?>) {
                    type = ((MethodSingularAttribute<?, ?>) lastAttribute).getType();
                } else {
                    type = ((MethodPluralAttribute<?, ?, ?>) lastAttribute).getElementType();
                }
                if (type instanceof ManagedViewType<?>) {
                    MethodAttribute<?, ?> attribute = ((ManagedViewType<?>) type).getAttribute(part);
                    if (attribute == null) {
                        throw new IllegalArgumentException("Error de-referencing attribute '" + part + "' because it does not exist on the type '" + type.getJavaType().getName() + "' while creating attribute path for: " + getAttributePathForAttributes(subAttributes));
                    }
                    if (subAttribute == attribute) {
                        attributes.addAll(subAttributes.subList(i, subAttributes.size()));
                        break;
                    } else {
                        attributes.add(lastAttribute = attribute);
                    }
                } else {
                    throw new IllegalArgumentException("Can not de-reference attribute '" + part + "' on non-managed type '" + type.getJavaType().getName() + "' for attribute path: " + getAttributePathForAttributes(subAttributes));
                }
            }
            this.attributes = Collections.unmodifiableList(attributes);
        }

        @Override
        public MethodAttribute<?, Y> resolve(ManagedViewType<X> baseType) {
            if (baseType == attributes.get(0).getDeclaringType()) {
                return (MethodAttribute<?, Y>) attributes.get(attributes.size() - 1);
            }
            MethodAttribute<?, ?> lastAttribute = null;
            Type<?> type = baseType;
            for (MethodAttribute<?, ?> attribute : attributes) {
                String part = attribute.getName();
                if (type instanceof ManagedViewType<?>) {
                    lastAttribute = ((ManagedViewType<?>) type).getAttribute(part);
                    if (lastAttribute == attribute) {
                        return (MethodAttribute<?, Y>) attributes.get(attributes.size() - 1);
                    }
                    if (lastAttribute == null) {
                        throw new IllegalArgumentException("Error de-referencing attribute '" + part + "' because it does not exist on the type '" + type.getJavaType().getName() + "' while resolving attribute path: " + getAttributePathForAttributes(attributes));
                    }
                    if (lastAttribute instanceof MethodSingularAttribute<?, ?>) {
                        type = ((MethodSingularAttribute<?, ?>) lastAttribute).getType();
                    } else {
                        type = ((MethodPluralAttribute<?, ?, ?>) lastAttribute).getElementType();
                    }
                } else {
                    throw new IllegalArgumentException("Can not de-reference attribute '" + part + "' on non-managed type '" + type.getJavaType().getName() + "' while resolving attribute path: " + getAttributePathForAttributes(attributes));
                }
            }
            return (MethodAttribute<?, Y>) lastAttribute;
        }

        @Override
        public List<MethodAttribute<?, ?>> resolveAll(ManagedViewType<X> baseType) {
            if (baseType == attributes.get(0).getDeclaringType()) {
                return attributes;
            }
            List<MethodAttribute<?, ?>> list = new ArrayList<>(attributes.size());
            Type<?> type = baseType;
            for (int i = 0; i < attributes.size(); i++) {
                MethodAttribute<?, ?> attribute = attributes.get(i);
                String part = attribute.getName();
                if (type instanceof ManagedViewType<?>) {
                    MethodAttribute<?, ?> lastAttribute = ((ManagedViewType<?>) type).getAttribute(part);
                    if (lastAttribute == null) {
                        throw new IllegalArgumentException("Error de-referencing attribute '" + part + "' because it does not exist on the type '" + type.getJavaType().getName() + "' while resolving attribute path: " + getAttributePathForAttributes(attributes));
                    }
                    if (attribute == lastAttribute) {
                        list.addAll(attributes.subList(i, attributes.size()));
                        break;
                    } else {
                        list.add(lastAttribute);
                    }
                    if (lastAttribute instanceof MethodSingularAttribute<?, ?>) {
                        type = ((MethodSingularAttribute<?, ?>) lastAttribute).getType();
                    } else {
                        type = ((MethodPluralAttribute<?, ?, ?>) lastAttribute).getElementType();
                    }
                } else {
                    throw new IllegalArgumentException("Can not de-reference attribute '" + part + "' on non-managed type '" + type.getJavaType().getName() + "' while resolving attribute path: " + getAttributePathForAttributes(attributes));
                }
            }
            return list;
        }

        @Override
        public List<String> getAttributeNames() {
            List<String> attributeNames = new ArrayList<>(attributes.size());
            for (MethodAttribute<?, ?> attribute : attributes) {
                attributeNames.add(attribute.getName());
            }
            return attributeNames;
        }

        @Override
        public List<MethodAttribute<?, ?>> getAttributes() {
            return attributes;
        }

        @Override
        public <E> AttributePath<X, E, E> get(String attributePath) {
            return new TypedAttributePath<>(this, attributePath);
        }

        @Override
        public <E, C extends Collection<E>> AttributePath<X, E, C> getMulti(String attributePath) {
            return new TypedAttributePath<>(this, attributePath);
        }

        @Override
        public <E> AttributePath<X, E, E> get(MethodSingularAttribute<B, E> attribute) {
            return new TypedAttributePath<>(this, Collections.singletonList(attribute), false);
        }

        @Override
        public <E> AttributePath<X, E, E> get(MethodPluralAttribute<B, ?, E> attribute) {
            return new TypedAttributePath<>(this, Collections.singletonList(attribute), false);
        }

        @Override
        public <C extends Collection<E>, E> AttributePath<X, E, C> get(MethodMultiListAttribute<B, E, C> attribute) {
            return new TypedAttributePath<>(this, Collections.singletonList(attribute), false);
        }

        @Override
        public <C extends Collection<E>, E> AttributePath<X, E, C> get(MethodMultiMapAttribute<B, ?, E, C> attribute) {
            return new TypedAttributePath<>(this, Collections.singletonList(attribute), false);
        }

        @Override
        public <C, E> AttributePath<X, C, E> get(AttributePath<B, C, E> attributePath) {
            if (attributePath instanceof TypedAttributePath<?, ?, ?>) {
                return new TypedAttributePath<>(this, ((TypedAttributePath<?, ?, ?>) attributePath).attributes, false);
            } else {
                return new TypedAttributePath<>(this, attributePath.getAttributeNames());
            }
        }

        @Override
        public String getPath() {
            return getAttributePathForAttributes(attributes);
        }

        @Override
        public String toString() {
            return getAttributePathForAttributes(attributes);
        }
    }

}
