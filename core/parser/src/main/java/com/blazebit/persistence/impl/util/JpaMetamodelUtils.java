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

package com.blazebit.persistence.impl.util;

import com.blazebit.persistence.impl.AttributePath;
import com.blazebit.persistence.impl.EntityMetamodel;
import com.blazebit.persistence.impl.ListIndexAttribute;
import com.blazebit.persistence.impl.MapKeyAttribute;
import com.blazebit.reflection.ReflectionUtils;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class JpaMetamodelUtils {

    private JpaMetamodelUtils() {
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
        Class<?> jpaReportedFieldClass;
        Class<?> fieldClass;

        if (attr.isCollection()) {
            PluralAttribute<?, ?, ?> collectionAttr = (PluralAttribute<?, ?, ?>) attr;
            // If it's a raw type, we use the element type the jpa provider thinks is right
            fieldClass = collectionAttr.getElementType().getJavaType();
            jpaReportedFieldClass = fieldClass;

            if (collectionAttr.getCollectionType() == PluralAttribute.CollectionType.MAP) {
                if (attr.getJavaMember() instanceof Method) {
                    Method method = (Method) attr.getJavaMember();
                    Class<?>[] typeArguments = ReflectionUtils.getResolvedMethodReturnTypeArguments(resolverBaseClass, method);

                    // Skip raw types
                    if (typeArguments.length != 0) {
                        fieldClass = typeArguments[1];
                        if (fieldClass == null) {
                            fieldClass = resolveType(resolverBaseClass, ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[1]);
                        }
                    }
                } else {
                    Field field = (Field) attr.getJavaMember();
                    Class<?>[] typeArguments = ReflectionUtils.getResolvedFieldTypeArguments(resolverBaseClass, field);

                    // Skip raw types
                    if (typeArguments.length != 0) {
                        fieldClass = typeArguments[1];
                        if (fieldClass == null) {
                            fieldClass = resolveType(resolverBaseClass, ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[1]);
                        }
                    }
                }
            } else {
                if (attr.getJavaMember() instanceof Method) {
                    Method method = (Method) attr.getJavaMember();
                    Class<?>[] typeArguments = ReflectionUtils.getResolvedMethodReturnTypeArguments(resolverBaseClass, method);

                    // Skip raw types
                    if (typeArguments.length != 0) {
                        fieldClass = typeArguments[0];
                        if (fieldClass == null) {
                            fieldClass = resolveType(resolverBaseClass, ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0]);
                        }
                    }
                } else {
                    Field field = (Field) attr.getJavaMember();
                    Class<?>[] typeArguments = ReflectionUtils.getResolvedFieldTypeArguments(resolverBaseClass, field);

                    // Skip raw types
                    if (typeArguments.length != 0) {
                        fieldClass = typeArguments[0];
                        if (fieldClass == null) {
                            fieldClass = resolveType(resolverBaseClass, ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]);
                        }
                    }
                }
            }
        } else {
            jpaReportedFieldClass = ((SingularAttribute<?, ?>) attr).getType().getJavaType();
            if (attr.getJavaMember() instanceof Method) {
                Method method = (Method) attr.getJavaMember();
                // EclipseLink returns an accessor method for attributes when using runtime weaving which has a completely different return type
                if (!method.getName().startsWith("get") && !method.getName().startsWith("is")) {
                    return jpaReportedFieldClass;
                }
                fieldClass = ReflectionUtils.getResolvedMethodReturnType(resolverBaseClass, method);
                if (fieldClass == null) {
                    fieldClass = resolveType(resolverBaseClass, method.getGenericReturnType());
                }
                if (fieldClass.isAssignableFrom(jpaReportedFieldClass)) {
                    return jpaReportedFieldClass;
                } else if (jpaReportedFieldClass.isAssignableFrom(fieldClass)) {
                    return fieldClass;
                } else if (method.getGenericReturnType() instanceof TypeVariable<?>) {
                    // EclipseLink workaround. Apparently EclipseLink reports a wrong type for the attribute
                    return fieldClass;
                } else {
                    // Default to the JPA reported type if the declared type and the resolved type are unrelated
                    // See https://github.com/Blazebit/blaze-persistence/issues/457 for the actual use case
                    return jpaReportedFieldClass;
                }
            } else {
                Field field = (Field) attr.getJavaMember();
                fieldClass = ReflectionUtils.getResolvedFieldType(resolverBaseClass, field);
                if (fieldClass == null) {
                    fieldClass = resolveType(resolverBaseClass, field.getGenericType());
                }
                if (fieldClass.isAssignableFrom(jpaReportedFieldClass)) {
                    return jpaReportedFieldClass;
                } else if (jpaReportedFieldClass.isAssignableFrom(fieldClass)) {
                    return fieldClass;
                } else if (field.getGenericType() instanceof TypeVariable<?>) {
                    // EclipseLink workaround. Apparently EclipseLink reports a wrong type for the attribute
                    return fieldClass;
                } else {
                    // Default to the JPA reported type if the declared type and the resolved type are unrelated
                    // See https://github.com/Blazebit/blaze-persistence/issues/457 for the actual use case
                    return jpaReportedFieldClass;
                }
            }
        }

        if (fieldClass.isAssignableFrom(jpaReportedFieldClass)) {
            return jpaReportedFieldClass;
        } else if (jpaReportedFieldClass.isAssignableFrom(fieldClass)) {
            return fieldClass;
        } else {
            // Hibernate reports the wrong type for fields that are differently bound via a type variable
            // so we default in this erroneous case to the resolved java type instead of the jpa resolved type
            return fieldClass;
        }
    }

    public static <T> Attribute<? super T, ?> getAttribute(ManagedType<T> type, String attributeName) {
        try {
            return type.getAttribute(attributeName);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public static SingularAttribute<?, ?> getIdAttribute(IdentifiableType<?> entityType) {
        Class<?> idClass = null;
        try {
            Type<?> idType = entityType.getIdType();
            if (idType == null) {
                // Hibernate treats ManyToOne's mapped as @Id differently, we need to scan the type and look for the id..
                for (SingularAttribute<?, ?> attribute : entityType.getSingularAttributes()) {
                    if (attribute.isId()) {
                        return attribute;
                    }
                }
            }
            idClass = idType.getJavaType();
            return entityType.getId(idClass);
        } catch (IllegalArgumentException e) {
            /**
             * Eclipselink returns wrapper types from entityType.getIdType().getJavaType() even if the id type
             * is a primitive.
             * In this case, entityType.getId(...) throws an IllegalArgumentException. We catch it here and try again
             * with the corresponding primitive type.
             */
            if (idClass != null) {
                final Class<?> primitiveIdClass = ReflectionUtils.getPrimitiveClassOfWrapper(idClass);
                if (primitiveIdClass != null) {
                    return entityType.getId(primitiveIdClass);
                }
            }
            throw e;
        } catch (IllegalStateException e) {
            // Hibernate 4 treats ManyToOne's mapped as @Id differently, we need to scan the type and look for the id..
            for (SingularAttribute<?, ?> attribute : entityType.getSingularAttributes()) {
                if (attribute.isId()) {
                    return attribute;
                }
            }
            throw e;
        } catch (RuntimeException e) {
            // Datanucleus 4 can't properly handle entities for "views" with id columns, so we ignore the id column in this case
            if (e.getClass().getSimpleName().equals("ClassNotResolvedException")) {
                return null;
            }
            throw e;
        }
    }

    public static SingularAttribute<?, ?> getVersionAttribute(IdentifiableType<?> entityType) {
        if (!entityType.hasVersionAttribute()) {
            return null;
        }
        for (SingularAttribute<?, ?> attribute : entityType.getSingularAttributes()) {
            if (attribute.isVersion()) {
                return attribute;
            }
        }

        return null;
    }

    public static ManagedType<?> getManagedType(EntityMetamodel metamodel, Class<?> managedTypeClass, String treatTypeName) {
        if (treatTypeName != null) {
            ManagedType<?> type = metamodel.managedType(treatTypeName);
            if (!managedTypeClass.isAssignableFrom(type.getJavaType())) {
                throw new IllegalArgumentException("Treat type '" + treatTypeName + "' is not a subtype of: " + managedTypeClass.getName());
            }

            return type;
        }

        return metamodel.managedType(managedTypeClass);
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

    public static AttributePath getAttributePath(Metamodel metamodel, ManagedType<?> type, String attributePath) {
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

        for (int i = 0; i < attributeParts.length; i++) {
            Attribute<?, ?> attr = null;
            if (currentType == null) {
                // dereference basic
                break;
            }
            attr = getAttribute(currentType, attributeParts[i]);
            if (attr == null) {
                attrPath.clear();
                break;
            }

            currentClass = resolveFieldClass(currentClass, attr);
            if (attr instanceof PluralAttribute<?, ?, ?>) {
                PluralAttribute<?, ?, ?> pluralAttr = (PluralAttribute<?, ?, ?>) attr;
                Type<?> elementType = pluralAttr.getElementType();
                if (elementType.getPersistenceType() == Type.PersistenceType.EMBEDDABLE) {
                    currentType = metamodel.embeddable(currentClass);
                } else if (elementType.getPersistenceType() == Type.PersistenceType.BASIC) {
                    currentType = null;
                } else {
                    currentType = metamodel.entity(currentClass);
                }
            } else if (attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {
                currentType = metamodel.embeddable(currentClass);
            } else if (attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC) {
                currentType = null;
            } else {
                currentType = metamodel.entity(currentClass);
            }

            attrPath.add(attr);
        }

        if (attrPath.isEmpty()) {
            throw new IllegalArgumentException("Path " + attributePath + " does not exist on entity " + type.getJavaType().getName());
        }

        return new AttributePath(attrPath, currentClass);
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
            attr = getAttribute(currentType, attributeParts[i]);
            if (attr == null) {
                attrPath.clear();
                break;
            }

            currentClass = resolveFieldClass(currentClass, attr);
            if (attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {
                currentType = metamodel.embeddable(currentClass);
            } else if (attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC) {
                currentType = null;
            } else if (isJoinable(attr) && joinableAllowed) {
                joinableAllowed = false;
                if (i + 1 < attributeParts.length) {
                    currentType = metamodel.entity(currentClass);
                    // look ahead
                    Attribute<?, ?> nextAttr = getAttribute(currentType, attributeParts[i + 1]);
                    if (!getIdAttribute((EntityType<?>) currentType).getName().equals(nextAttr.getName())) {
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

        return new AttributePath(attrPath, currentClass);
    }

    public static AttributePath getJoinTableCollectionAttributePath(Metamodel metamodel, EntityType<?> type, String attributePath, String collectionName) {
        String trimmedPath = attributePath.trim();
        String indexStart = "index(";
        String keyStart = "key(";
        int collectionArgumentStart;
        Attribute<?, ?> collectionFunction;
        if (trimmedPath.regionMatches(true, 0, indexStart, 0, indexStart.length())) {
            collectionArgumentStart = indexStart.length();
            collectionFunction = new ListIndexAttribute<>(type.getList(collectionName));
        } else if (trimmedPath.regionMatches(true, 0, keyStart, 0, keyStart.length())) {
            collectionArgumentStart = keyStart.length();
            collectionFunction = new MapKeyAttribute<>(type.getMap(collectionName));
        } else {
            int dotIndex = trimmedPath.indexOf('.');
            if (!trimmedPath.equals(collectionName) && (dotIndex == -1 || !trimmedPath.substring(0, dotIndex).equals(collectionName))) {
                SingularAttribute<?, ?> idAttribute = getIdAttribute(type);
                if (!idAttribute.getName().equals(attributePath)) {
                    throw new IllegalArgumentException("Only access to the owner type's id attribute '" + idAttribute.getName() + "' is allowed. Invalid access to different attribute through the expression: " + attributePath);
                }
                return new AttributePath(new ArrayList<Attribute<?, ?>>(Collections.singletonList(idAttribute)), resolveFieldClass(type.getJavaType(), idAttribute));
            }

            Attribute<?, ?> collectionAttribute = getAttribute(type, collectionName);
            Class<?> targetClass = resolveFieldClass(type.getJavaType(), collectionAttribute);
            if (dotIndex == -1) {
                return new AttributePath(new ArrayList<Attribute<?, ?>>(Collections.singletonList(collectionAttribute)), resolveFieldClass(targetClass, collectionAttribute));
            }

            String collectionElementAttributeName = trimmedPath.substring(dotIndex + 1);
            ManagedType<?> targetManagedType = metamodel.managedType(targetClass);
            if (targetManagedType instanceof EntityType<?>) {
                EntityType<?> targetEntityType = (EntityType<?>) targetManagedType;
                SingularAttribute<?, ?> idAttribute = getIdAttribute(targetEntityType);
                String actualIdAttributeName = idAttribute.getName();
                if (!actualIdAttributeName.equals(collectionElementAttributeName)) {
                    throw new IllegalArgumentException("Only access to the target element type's id attribute '" + actualIdAttributeName + "' is allowed. Invalid access to different attribute through the expression: " + attributePath);
                }
                return new AttributePath(new ArrayList<>(Arrays.asList(collectionAttribute, idAttribute)), resolveFieldClass(targetClass, idAttribute));
            } else {
                Attribute<?, ?> attribute = null;
                Throwable cause = null;
                try {
                    attribute = targetManagedType.getAttribute(collectionElementAttributeName);
                } catch (IllegalArgumentException ex) {
                    cause = ex;
                }
                if (attribute == null) {
                    throw new IllegalArgumentException("Couldn't find attribute '" + collectionElementAttributeName + "' on managed type '" + targetClass.getName() + "'. Invalid access through the expression: " + attributePath, cause);
                }
                return new AttributePath(new ArrayList<>(Arrays.asList(collectionAttribute, attribute)), resolveFieldClass(targetClass, attribute));
            }
        }

        // assume the last character is the closing parenthesis
        String collectionAttributeName = trimmedPath.substring(collectionArgumentStart, trimmedPath.length() - 1);
        if (!collectionAttributeName.equals(collectionName)) {
            throw new IllegalArgumentException("Collection functions are only allowed to be used with the collection '" + collectionName + "'!. Invalid use in the expression: " + attributePath);
        }

        return new AttributePath(new ArrayList<Attribute<?, ?>>(Collections.singletonList(collectionFunction)), collectionFunction.getJavaType());
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
}
