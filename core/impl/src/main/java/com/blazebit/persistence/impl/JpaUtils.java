/*
 * Copyright 2015 Blazebit.
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import com.blazebit.reflection.ReflectionUtils;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public final class JpaUtils {

    public static <T> Attribute<? super T, ?> getAttribute(ManagedType<T> type, String attributeName) {
        try {
            return type.getAttribute(attributeName);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
    
    public static List<Attribute<?, ?>> getBasicAttributePath(Metamodel metamodel, ManagedType<?> type, String attributePath) {
        List<Attribute<?, ?>> attrPath;
        
        if (attributePath.indexOf('.') == -1) {
            attrPath = new ArrayList<Attribute<?, ?>>(1);
            Attribute<?, ?> attribute = type.getAttribute(attributePath);
            if (attribute == null) {
                // Well, some implementations might not be fully spec compliant..
                throw new IllegalArgumentException("Attribute '" + attributePath + "' does not exist on '" + type.getJavaType().getName() + "'!");
            }
            
            attrPath.add(attribute);
            return attrPath;
        } else {
            attrPath = new ArrayList<Attribute<?, ?>>();
        }
        
        String[] attributeParts = attributePath.split("\\.");
        ManagedType<?> currentType = type;
        
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
            } else if(attr.getPersistentAttributeType() == PersistentAttributeType.BASIC) {
                currentType = null;
            } else if(JpaUtils.isJoinable(attr) && joinableAllowed) {
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
            attrPath.add(attr);
        }
        
        if (attrPath.isEmpty()) {
            throw new IllegalArgumentException("Path " + attributePath + " does not exist on entity " + type.getJavaType().getName());
        }
        
        return attrPath;
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
        for (ManagedType<?> subType : metamodel.getManagedTypes()) {
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

	public static Attribute<?, ?> getIdAttribute(EntityType<?> entityType) {
		return entityType.getId(entityType.getIdType().getJavaType());
	}

    public static boolean isJoinable(Attribute<?, ?> attr) {
        return attr.isCollection() || attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE
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
}
