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

package com.blazebit.persistence.view.impl.accessor;

import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.spi.AttributePath;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.metamodel.AbstractMethodAttribute;
import com.blazebit.persistence.view.impl.metamodel.BasicTypeImpl;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MappingAttribute;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.metamodel.Type;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.reflection.ReflectionUtils;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public final class Accessors {

    private Accessors() {
    }

    public static AttributeAccessor forViewId(EntityViewManagerImpl evm, ViewType<?> viewType, boolean readonly) {
        return new ViewIdAttributeAccessor(evm, viewType, readonly);
    }

    public static AttributeAccessor forViewAttribute(EntityViewManagerImpl evm, MethodAttribute<?, ?> attribute, boolean readonly) {
        if (readonly || ((AbstractMethodAttribute<?, ?>) attribute).getDirtyStateIndex() == -1) {
            return new ViewAttributeAccessor(evm, attribute, readonly);
        } else {
            return forMutableViewAttribute(evm, attribute);
        }
    }

    public static InitialValueAttributeAccessor forMutableViewAttribute(EntityViewManagerImpl evm, MethodAttribute<?, ?> attribute) {
        return new DirtyStateViewAttributeAccessor(evm, attribute);
    }

    public static AttributeAccessor forViewAttributePath(EntityViewManagerImpl evm, ManagedViewType<?> viewType, String attributePath, boolean readonly) {
        if (attributePath.indexOf('.') == -1) {
            return forViewAttribute(evm, viewType.getAttribute(attributePath), readonly);
        }

        String[] attributeParts = attributePath.split("\\.");
        List<AttributeAccessor> mappers = new ArrayList<>(attributeParts.length);
        for (int i = 0; i < attributeParts.length - 1; i++) {
            MethodAttribute<?, ?> attribute = viewType.getAttribute(attributeParts[i]);
            mappers.add(forViewAttribute(evm, attribute, readonly));
            viewType = (ManagedViewType<?>) ((SingularAttribute<?, ?>) attribute).getType();
        }

        mappers.add(forViewAttribute(evm, viewType.getAttribute(attributeParts[attributeParts.length - 1]), readonly));
        return new NestedAttributeAccessor(mappers);
    }

    public static AttributeAccessor forEntityMappingAsViewAccessor(EntityViewManagerImpl evm, ManagedViewType<?> viewType, String attributePath, boolean readonly) {
        if (attributePath.indexOf('.') == -1) {
            MethodAttribute<?, ?> foundAttribute = null;
            for (MethodAttribute<?, ?> methodAttribute : viewType.getAttributes()) {
                if (methodAttribute instanceof MappingAttribute<?, ?>) {
                    String attributeMapping = ((MappingAttribute) methodAttribute).getMapping();
                    if (attributeMapping.equals(attributePath)) {
                        if (foundAttribute != null) {
                            throw new IllegalArgumentException("Could not determine view attribute accessor because of ambiguous attributes: [" + foundAttribute + ", " + methodAttribute + "]");
                        }
                        foundAttribute = methodAttribute;
                    }
                }
            }
            return forViewAttribute(evm, foundAttribute, readonly);
        }

        String[] attributeParts = attributePath.split("\\.");
        AttributeEntry root = new AttributeEntry(viewType, attributeParts[0]);
        for (int i = 1; i < attributeParts.length; i++) {
            root.addAttributeMapping(attributeParts[i]);
        }

        return root.getAttributeAccessor(attributeParts.length, evm, readonly);
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static final class AttributeEntry {
        private final AttributeEntry parent;
        private final MethodAttribute<?, ?> attribute;
        private final ManagedType<?> jpaAttributeOwner;
        private final Attribute<?, ?> jpaAttribute;
        private final List<AttributeEntry> possibleAttributes = new ArrayList<>(1);
        private List<List<AttributeEntry>> currentPossibleAttributes;

        public AttributeEntry(ManagedViewType<?> viewType, String mapping) {
            for (MethodAttribute<?, ?> methodAttribute : viewType.getAttributes()) {
                if (methodAttribute instanceof MappingAttribute<?, ?>) {
                    String attributeMapping = ((MappingAttribute) methodAttribute).getMapping();
                    if (attributeMapping.equals(mapping)) {
                        possibleAttributes.add(new AttributeEntry(this, methodAttribute));
                    }
                }
            }
            this.currentPossibleAttributes = new ArrayList<>();
            this.currentPossibleAttributes.add(possibleAttributes);
            this.parent = null;
            this.attribute = null;
            this.jpaAttributeOwner = null;
            this.jpaAttribute = null;
        }

        private AttributeEntry(AttributeEntry parent, MethodAttribute<?, ?> attribute) {
            this.parent = parent;
            this.attribute = attribute;
            this.jpaAttributeOwner = null;
            this.jpaAttribute = null;
        }

        public AttributeEntry(AttributeEntry parent, ManagedType<?> jpaAttributeOwner, Attribute<?, ?> jpaAttribute) {
            this.parent = parent;
            this.attribute = null;
            this.jpaAttributeOwner = jpaAttributeOwner;
            this.jpaAttribute = jpaAttribute;
        }

        final void removeIfEmpty() {
            if (possibleAttributes.isEmpty() && parent != null) {
                parent.possibleAttributes.remove(this);
                parent.removeIfEmpty();
            }
        }

        final void collectAttributeAccessors(List<AttributeAccessor> accessors, EntityViewManagerImpl evm, boolean readonly) {
            if (attribute != null) {
                accessors.add(forViewAttribute(evm, attribute, readonly));
            }
            if (jpaAttribute != null) {
                accessors.add(forEntityAttribute(jpaAttributeOwner.getJavaType(), jpaAttribute));
            }
            if (!possibleAttributes.isEmpty()) {
                if (possibleAttributes.size() > 1) {
                    throw new IllegalArgumentException("Could not determine view attribute accessor because of ambiguous attributes: " + possibleAttributes);
                }
                possibleAttributes.get(0).collectAttributeAccessors(accessors, evm, readonly);
            }
        }

        public final void addAttributeMapping(String mapping) {
            List<List<AttributeEntry>> newCurrentPossibleAttributes = new ArrayList<>();
            for (List<AttributeEntry> possibleAttributesCandidate : currentPossibleAttributes) {
                for (AttributeEntry attributeEntry : possibleAttributesCandidate) {
                    Type<?> type = ((SingularAttribute<?, ?>) attributeEntry.attribute).getType();
                    if (type instanceof ManagedViewType<?>) {
                        ManagedViewType<?> viewType = (ManagedViewType<?>) type;
                        for (MethodAttribute<?, ?> methodAttribute : viewType.getAttributes()) {
                            if (methodAttribute instanceof MappingAttribute<?, ?>) {
                                String attributeMapping = ((MappingAttribute) methodAttribute).getMapping();
                                if (attributeMapping.equals(mapping)) {
                                    attributeEntry.possibleAttributes.add(new AttributeEntry(this, methodAttribute));
                                    newCurrentPossibleAttributes.add(attributeEntry.possibleAttributes);
                                }
                            }
                        }

                        attributeEntry.removeIfEmpty();
                    } else {
                        ManagedType<?> managedType = ((BasicTypeImpl<?>) type).getManagedType();
                        for (Attribute<?, ?> attribute : managedType.getAttributes()) {
                            if (attribute.getName().equals(mapping)) {
                                attributeEntry.possibleAttributes.add(new AttributeEntry(this, managedType, attribute));
                                newCurrentPossibleAttributes.add(attributeEntry.possibleAttributes);
                            }
                        }

                        attributeEntry.removeIfEmpty();
                    }
                }
            }

            this.currentPossibleAttributes = newCurrentPossibleAttributes;
        }

        public final AttributeAccessor getAttributeAccessor(int attributeParts, EntityViewManagerImpl evm, boolean readonly) {
            List<AttributeAccessor> mappers = new ArrayList<>(attributeParts);
            collectAttributeAccessors(mappers, evm, readonly);
            return new NestedAttributeAccessor(mappers);
        }
    }

    public static AttributeAccessor forEntityAttribute(Class<?> entityClass, Attribute<?, ?> attribute) {
        return forEntityAttribute(entityClass, attribute, null);
    }

    public static AttributeAccessor forEntityMapping(EntityViewManagerImpl evm, MethodAttribute<?, ?> attribute) {
        if (attribute instanceof MappingAttribute<?, ?>) {
            return forEntityMapping(evm, attribute.getDeclaringType().getEntityClass(), ((MappingAttribute<?, ?>) attribute).getMapping());
        } else {
            return null;
        }
    }

    public static AttributeAccessor forEntityMapping(EntityViewManagerImpl evm, Class<?> entityClass, String mapping) {
        EntityMetamodel entityMetamodel = evm.getMetamodel().getEntityMetamodel();
        AttributePath path = evm.getJpaProvider().getJpaMetamodelAccessor().getBasicAttributePath(entityMetamodel, entityMetamodel.managedType(entityClass), mapping);
        List<Attribute<?, ?>> attributes = path.getAttributes();

        if (attributes.size() == 1) {
            return forEntityAttribute(entityClass, attributes.get(0), null);
        }

        List<AttributeAccessor> mappers = new ArrayList<>(attributes.size());
        Class<?> targetClass = entityClass;
        for (int i = 0; i < attributes.size() - 1; i++) {
            javax.persistence.metamodel.Attribute<?, ?> attribute = attributes.get(i);
            Class<?> attributeClass = JpaMetamodelUtils.resolveFieldClass(targetClass, attribute);
            mappers.add(forEntityAttribute(targetClass, attribute, attributeClass));
            targetClass = attributeClass;
        }

        mappers.add(forEntityAttribute(targetClass, attributes.get(attributes.size() - 1), null));
        return new NestedAttributeAccessor(mappers);
    }

    private static AttributeAccessor forEntityAttribute(Class<?> entityClass, Attribute<?, ?> attribute, Class<?> targetType) {
        Member member = attribute.getJavaMember();
        if (member instanceof Field) {
            return new EntityFieldAttributeAccessor((Field) member, targetType);
        } else if (member instanceof Method) {
            Method getter = ReflectionUtils.getGetter(entityClass, attribute.getName());
            Method setter = ReflectionUtils.getSetter(entityClass, attribute.getName());
            return new EntityMethodAttributeAccessor(getter, setter, targetType);
        } else {
            throw new IllegalArgumentException("Unsupported java member for id attribute: " + member);
        }
    }
}
