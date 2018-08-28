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

package com.blazebit.persistence.parser.util;

import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.reflection.ReflectionUtils;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class JpaMetamodelUtils {

    public static final Comparator<Attribute<?, ?>> ATTRIBUTE_NAME_COMPARATOR = new Comparator<Attribute<?, ?>>() {
        @Override
        public int compare(Attribute<?, ?> o1, Attribute<?, ?> o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    private JpaMetamodelUtils() {
    }

    public static String getTypeName(Type<?> type) {
        // Envers audited models don't have a java type
        if (type.getJavaType() == null || type instanceof EntityType<?>) {
            return ((EntityType<?>) type).getName();
        } else {
            return type.getJavaType().getName();
        }
    }

    public static String getSimpleTypeName(Type<?> type) {
        // Envers audited models don't have a java type
        if (type.getJavaType() == null) {
            return ((EntityType<?>) type).getName();
        } else if (type instanceof EntityType<?>) {
            return ((EntityType) type).getName();
        } else {
            return type.getJavaType().getSimpleName();
        }
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
        Class<?> resolverBaseClass = baseClass == null ? null : getConcreterClass(baseClass, attr.getDeclaringType().getJavaType());
        Class<?> jpaReportedFieldClass;
        Class<?> fieldClass;

        if (attr.isCollection()) {
            PluralAttribute<?, ?, ?> collectionAttr = (PluralAttribute<?, ?, ?>) attr;
            // If it's a raw type, we use the element type the jpa provider thinks is right
            fieldClass = collectionAttr.getElementType().getJavaType();
            jpaReportedFieldClass = fieldClass;

            if (resolverBaseClass == null) {
                return jpaReportedFieldClass;
            }

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

            if (resolverBaseClass == null) {
                return jpaReportedFieldClass;
            }
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
            } else if (attr.getJavaMember() instanceof Field) {
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
            } else {
                fieldClass = jpaReportedFieldClass;
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

    public static SingularAttribute<?, ?> getSingleIdAttribute(IdentifiableType<?> entityType) {
        Iterator<SingularAttribute<?, ?>> iterator = getIdAttributes(entityType).iterator();

        if (!iterator.hasNext()) {
            return null;
        }

        SingularAttribute<?, ?> next = iterator.next();

        if (iterator.hasNext()) {
            throw new IllegalStateException("Can't access a single id attribute as the entity has multiple id attributes i.e. uses @IdClass!");
        }

        return next;
    }

    public static Set<SingularAttribute<?, ?>> getIdAttributes(IdentifiableType<?> entityType) {
        try {
            if (entityType.hasSingleIdAttribute()) {
                return Collections.<SingularAttribute<?, ?>>singleton(entityType.getId(entityType.getIdType().getJavaType()));
            } else {
                if (entityType.getIdType() == null) {
                    // Hibernate treats ManyToOne's mapped as @Id differently, we need to scan the type and look for the id..
                    return collectIdAttributes(entityType);
                } else {
                    Set<SingularAttribute<?, ?>> idTypes = new TreeSet<>(ATTRIBUTE_NAME_COMPARATOR);
                    idTypes.addAll(entityType.getIdClassAttributes());
                    return idTypes;
                }
            }
        } catch (IllegalArgumentException e) {
            /**
             * Eclipselink returns wrapper types from entityType.getIdType().getJavaType() even if the id type
             * is a primitive.
             * In this case, entityType.getId(...) throws an IllegalArgumentException. We catch it here and try again
             * with the corresponding primitive type.
             * Note that it also returns just "any" type of an id class attribute in case there is no dedicated id class type.
             */
            if (entityType.getIdType() != null) {
                final Class<?> primitiveIdClass = ReflectionUtils.getPrimitiveClassOfWrapper(entityType.getIdType().getJavaType());
                if (primitiveIdClass == null) {
                    // Discover the identifier attributes like this instead for EclipseLink
                    Set<SingularAttribute<?, ?>> idTypes = collectIdAttributes(entityType);
                    if (!idTypes.isEmpty()) {
                        return idTypes;
                    }
                } else {
                    return Collections.<SingularAttribute<?, ?>>singleton(entityType.getId(primitiveIdClass));
                }
            }
            throw e;
        } catch (IllegalStateException e) {
            // Hibernate 4 treats ManyToOne's mapped as @Id differently, we need to scan the type and look for the id..
            Set<SingularAttribute<?, ?>> idTypes = collectIdAttributes(entityType);
            if (!idTypes.isEmpty()) {
                return idTypes;
            }
            throw e;
        } catch (RuntimeException e) {
            // Datanucleus 4 can't properly handle entities for "views" with id columns, so we ignore the id column in this case
            if (e.getClass().getSimpleName().equals("ClassNotResolvedException")) {
                return Collections.emptySet();
            }
            throw e;
        }
    }

    private static Set<SingularAttribute<?, ?>> collectIdAttributes(IdentifiableType<?> entityType) {
        Set<SingularAttribute<?, ?>> idTypes = new TreeSet<>(ATTRIBUTE_NAME_COMPARATOR);
        for (SingularAttribute<?, ?> attribute : entityType.getSingularAttributes()) {
            if (attribute.isId()) {
                idTypes.add(attribute);
            }
        }
        return idTypes;
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

    public static List<String> getEmbeddedPropertyNames(EmbeddableType<?> embeddedType) {
        List<String> attributes = new ArrayList<>();
        for (Attribute<?,?> attribute : embeddedType.getAttributes()) {
            attributes.add(attribute.getName());
        }
        return attributes;
    }

}
