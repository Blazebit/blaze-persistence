/*
 * Copyright 2014 - 2016 Blazebit.
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

import com.blazebit.persistence.impl.util.ClassUtils;
import com.blazebit.reflection.ReflectionUtils;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public final class JpaUtils {

    private static final Logger LOG = Logger.getLogger(JpaUtils.class.getName());

    private JpaUtils() {
    }

    public static ManagedType<?> getManagedType(EntityMetamodel metamodel, Class<?> managedTypeClass, String treatTypeName) {
        if (treatTypeName != null) {
            ManagedType<?> type = metamodel.managedType(treatTypeName);
            if (!type.getJavaType().isAssignableFrom(managedTypeClass)) {
                throw new IllegalArgumentException("Treat type '" + treatTypeName + "' is not a subtype of: " + managedTypeClass.getName());
            }

            return type;
        }

        return metamodel.managedType(managedTypeClass);
    }

    public static ManagedType<?> getManagedTypeOrNull(EntityMetamodel metamodel, Class<?> javaType) {
        return metamodel.getManagedType(javaType);
    }

    public static <T> Attribute<? super T, ?> getAttribute(ManagedType<T> type, String attributeName) {
        try {
            return type.getAttribute(attributeName);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
    
    public static AttributePath getBasicAttributePath(Metamodel metamodel, ManagedType<?> type, String attributePath) {
        List<Attribute<?, ?>> attrPath;
        
        if (attributePath.indexOf('.') == -1) {
            attrPath = new ArrayList<Attribute<?, ?>>(1);
            Attribute<?, ?> attribute = type.getAttribute(attributePath);
            if (attribute == null) {
                // Well, some implementations might not be fully spec compliant..
                throw new IllegalArgumentException("Attribute '" + attributePath + "' does not exist on '" + type.getJavaType().getName() + "'!");
            }
            
            attrPath.add(attribute);
            return new AttributePath(attrPath, resolveFieldClass(type.getJavaType(), attribute));
        } else {
            attrPath = new ArrayList<Attribute<?, ?>>();
        }
        
        String[] attributeParts = attributePath.split("\\.");
        ManagedType<?> currentType = type;
        Class<?> currentClass = type.getJavaType();

        boolean joinableAllowed = true;
        for (int i = 0; i < attributeParts.length; i++) {
            Attribute<?, ?> attr = null;
            if (currentType == null) {
                // dereference basic
                break;
            }
            attr = JpaUtils.getAttribute(currentType, attributeParts[i]);
            if (attr == null) {
                attrPath.clear();
                break;
            }
            if (attr.getPersistentAttributeType() == PersistentAttributeType.EMBEDDED) {
                currentType = metamodel.embeddable(attr.getJavaType());
            } else if (attr.getPersistentAttributeType() == PersistentAttributeType.BASIC) {
                currentType = null;
            } else if (JpaUtils.isJoinable(attr) && joinableAllowed) {
                joinableAllowed = false;
                if (i + 1 < attributeParts.length) {
                    currentType = metamodel.entity(attr.getJavaType());
                    // look ahead
                    Attribute<?, ?> nextAttr = JpaUtils.getAttribute(currentType, attributeParts[i + 1]);
                    if (!JpaUtils.getIdAttribute((EntityType<?>) currentType).equals(nextAttr)) {
                        throw new IllegalArgumentException("Path joining not allowed in returning expression: " + attributePath);
                    }
                }
            } else {
                throw new IllegalArgumentException("Path joining not allowed in returning expression: " + attributePath);
            }

            currentClass = resolveFieldClass(currentClass, attr);
            attrPath.add(attr);
        }
        
        if (attrPath.isEmpty()) {
            throw new IllegalArgumentException("Path " + attributePath + " does not exist on entity " + type.getJavaType().getName());
        }
        
        return new AttributePath(attrPath, currentClass);
    }

    public static Set<Attribute<?, ?>> getAttributesPolymorphic(Metamodel metamodel, ManagedType<?> type, String attributeName) {
        Attribute<?, ?> attr = getAttribute(type, attributeName);

        if (attr != null) {
            Set<Attribute<?, ?>> set = new HashSet<Attribute<?, ?>>(1);
            set.add(attr);
            return set;
        }

        // Try again polymorphic
        Class<?> javaType = type.getJavaType();
        Set<Attribute<?, ?>> resolvedAttributes = new HashSet<Attribute<?, ?>>();

        // Collect all possible subtypes of the given type
        for (ManagedType<?> subType : metamodel.getEntities()) {
            if (javaType.isAssignableFrom(subType.getJavaType()) && javaType != subType.getJavaType()) {
                // Collect all the attributes that resolve on every possible subtype
                attr = JpaUtils.getAttribute(subType, attributeName);
                if (attr != null) {
                    resolvedAttributes.add(attr);
                }
            }
        }

        return resolvedAttributes;
    }

    public static boolean isMap(Attribute<?, ?> attr) {
        return attr instanceof MapAttribute<?, ?, ?>;
    }

    public static boolean isOptional(Attribute<?, ?> attribute) {
        if (attribute instanceof SingularAttribute<?, ?>) {
            return ((SingularAttribute<?, ?>) attribute).isOptional();
        }

        return true;
    }

    public static SingularAttribute<?, ?> getIdAttribute(IdentifiableType<?> entityType) {
        Class<?> idClass = null;
        try {
            idClass = entityType.getIdType().getJavaType();
            return entityType.getId(idClass);
        } catch (IllegalArgumentException e) {
            /**
             * Eclipselink returns wrapper types from entityType.getIdType().getJavaType() even if the id type
             * is a primitive.
             * In this case, entityType.getId(...) throws an IllegalArgumentException. We catch it here and try again
             * with the corresponding primitive type.
             */
            if (idClass != null) {
                final Class<?> primitiveIdClass = ClassUtils.getPrimitiveClassOfWrapper(idClass);
                if (primitiveIdClass != null) {
                    return entityType.getId(primitiveIdClass);
                }
            }
            throw e;
        }
    }

    public static boolean isJoinable(Attribute<?, ?> attr) {
        if (attr.isCollection()) {
            return true;
        }
        SingularAttribute<?, ?> singularAttribute = (SingularAttribute<?, ?>) attr;
        // This is a special case for datanucleus... apparently an embedded id is an ONE_TO_ONE association although I think it should be an embedded
        // TODO: create a test case for datanucleus and report the problem 
        if (singularAttribute.isId()) {
            return false;
        }
        return attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE
            || attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_ONE;
    }

    public static Class<?> resolveType(Class<?> concreteClass, java.lang.reflect.Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else if (type instanceof TypeVariable) {
            return resolveType(concreteClass, ((TypeVariable<?>) type).getBounds()[0]);
        } else {
            throw new IllegalArgumentException("Unsupported type for resolving: " + type);
        }
    }

    private static Class<?> getConcreterClass(Class<?> class1, Class<?> class2) {
        if (class1.isAssignableFrom(class2)) {
            return class2;
        } else if (class2.isAssignableFrom(class1)) {
            return class1;
        } else {
            throw new IllegalArgumentException("The classes [" + class1.getName() + ", " + class2.getName()
                + "] are not in a inheritance relationship, so there is no concreter class!");
        }
    }

    public static Class<?> resolveFieldClass(Class<?> baseClass, Attribute<?, ?> attr) {
        Class<?> resolverBaseClass = getConcreterClass(baseClass, attr.getDeclaringType().getJavaType());
        Class<?> fieldClass;
    
        if (attr.isCollection()) {
            PluralAttribute<?, ?, ?> collectionAttr = (PluralAttribute<?, ?, ?>) attr;
    
            if (collectionAttr.getCollectionType() == PluralAttribute.CollectionType.MAP) {
                if (attr.getJavaMember() instanceof Method) {
                    Method method = (Method) attr.getJavaMember();
                    fieldClass = ReflectionUtils.getResolvedMethodReturnTypeArguments(resolverBaseClass, method)[1];
                    if (fieldClass == null) {
                        fieldClass = resolveType(resolverBaseClass, ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[1]);
                    }
                } else {
                    Field field = (Field) attr.getJavaMember();
                    fieldClass = ReflectionUtils.getResolvedFieldTypeArguments(resolverBaseClass, field)[1];
                    if (fieldClass == null) {
                        fieldClass = resolveType(resolverBaseClass, ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1]);
                    }
                }
            } else {
                if (attr.getJavaMember() instanceof Method) {
                    Method method = (Method) attr.getJavaMember();
                    fieldClass = ReflectionUtils.getResolvedMethodReturnTypeArguments(resolverBaseClass, method)[0];
                    if (fieldClass == null) {
                        fieldClass = resolveType(resolverBaseClass, ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0]);
                    }
                } else {
                    Field field = (Field) attr.getJavaMember();
                    fieldClass = ReflectionUtils.getResolvedFieldTypeArguments(resolverBaseClass, field)[0];
                    if (fieldClass == null) {
                        fieldClass = resolveType(resolverBaseClass, ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);
                    }
                }
            }
        } else {
            if (attr.getJavaMember() instanceof Method) {
                Method method = (Method) attr.getJavaMember();
                fieldClass = ReflectionUtils.getResolvedMethodReturnType(resolverBaseClass, method);
                if (fieldClass == null) {
                    fieldClass = resolveType(resolverBaseClass, method.getGenericReturnType());
                }
            } else {
                Field field = (Field) attr.getJavaMember();
                fieldClass = ReflectionUtils.getResolvedFieldType(resolverBaseClass, field);
                if (fieldClass == null) {
                    fieldClass = resolveType(resolverBaseClass, field.getGenericType());
                }
            }
        }
    
        return fieldClass;
    }

    public static Attribute<?, ?> getPolymorphicSimpleAttribute(Metamodel metamodel, ManagedType<?> type, String attributeName) {
        Set<Attribute<?, ?>> resolvedAttributes = getAttributesPolymorphic(metamodel, type, attributeName);
        Iterator<Attribute<?, ?>> iter = resolvedAttributes.iterator();

        if (resolvedAttributes.size() > 1) {
            // If there is more than one resolved attribute we can still save the user some trouble
            Attribute<?, ?> simpleAttribute = null;
            Set<Attribute<?, ?>> amiguousAttributes = new HashSet<Attribute<?, ?>>();

            for (Attribute<?, ?> attr : resolvedAttributes) {
                if (isJoinable(attr)) {
                    amiguousAttributes.add(attr);
                } else {
                    simpleAttribute = attr;
                }
            }

            if (simpleAttribute == null) {
                return null;
            } else {
                for (Attribute<?, ?> a : amiguousAttributes) {
                    LOG.warning("The attribute [" + attributeName + "] of the class [" + a.getDeclaringType().getJavaType().getName()
                        + "] is ambiguous for polymorphic implicit joining on the type [" + type.getJavaType().getName() + "]");
                }

                return simpleAttribute;
            }
        } else if (iter.hasNext()) {
            return iter.next();
        } else {
            return null;
        }
    }

    public static Attribute<?, ?> getPolymorphicAttribute(Metamodel metamodel, ManagedType<?> type, String attributeName) {
        Set<Attribute<?, ?>> resolvedAttributes = getAttributesPolymorphic(metamodel, type, attributeName);
        Iterator<Attribute<?, ?>> iter = resolvedAttributes.iterator();

        if (resolvedAttributes.size() > 1) {
            // If there is more than one resolved attribute we can still save the user some trouble
            Attribute<?, ?> joinableAttribute = null;
            Attribute<?, ?> attr = null;

            // Multiple non-joinable attributes would be fine since we only care for OUR join manager here
            // Multiple joinable attributes are only fine if they all have the same type
            while (iter.hasNext()) {
                attr = iter.next();
                if (isJoinable(attr)) {
                    if (joinableAttribute != null && !joinableAttribute.getJavaType().equals(attr.getJavaType())) {
                        throw new IllegalArgumentException("Multiple joinable attributes with the name [" + attributeName
                            + "] but different java types in the types [" + joinableAttribute.getDeclaringType().getJavaType().getName()
                            + "] and [" + attr.getDeclaringType().getJavaType().getName() + "] found!");
                    } else {
                        joinableAttribute = attr;
                    }
                }
            }

            // We return the joinable attribute because OUR join manager needs it's type for further joining
            if (joinableAttribute != null) {
                return joinableAttribute;
            }

            return attr;
        } else if (iter.hasNext()) {
            return iter.next();
        } else {
            return null;
        }
    }

    public static AttributeJoinResult getAttributeForJoining(EntityMetamodel metamodel, ManagedType<?> type, String attributeName) {
        Attribute<?, ?> attr;
        if (attributeName.indexOf('.') < 0) {
            attr = getPolymorphicAttribute(metamodel, type, attributeName);
            return new AttributeJoinResult(attr, type.getJavaType());
        }

        String[] attributeParts = attributeName.split("\\.");
        attr = getPolymorphicAttribute(metamodel, type, attributeParts[0]);

        for (int i = 1; i < attributeParts.length; i++) {
            type = metamodel.managedType(resolveFieldClass(type.getJavaType(), attr));
            attr = getPolymorphicAttribute(metamodel, type, attributeParts[i]);
        }

        return new AttributeJoinResult(attr, type.getJavaType());
    }

    public static Attribute<?, ?> getSimpleAttributeForImplicitJoining(EntityMetamodel metamodel, ManagedType<?> type, String attributeName) {
        Attribute<?, ?> attr;
        if (attributeName.indexOf('.') < 0) {
            attr = getPolymorphicSimpleAttribute(metamodel, type, attributeName);
            return attr;
        }

        String[] attributeParts = attributeName.split("\\.");
        attr = getPolymorphicSimpleAttribute(metamodel, type, attributeParts[0]);

        for (int i = 1; i < attributeParts.length; i++) {
            type = metamodel.managedType(resolveFieldClass(type.getJavaType(), attr));
            attr = getPolymorphicAttribute(metamodel, type, attributeParts[i]);
        }

        return attr;
    }
}
