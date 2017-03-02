/*
 * Copyright 2014 - 2017 Blazebit.
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

import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.PathExpression;
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
import java.util.List;
import java.util.Map;
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
            if (!managedTypeClass.isAssignableFrom(type.getJavaType())) {
                throw new IllegalArgumentException("Treat type '" + treatTypeName + "' is not a subtype of: " + managedTypeClass.getName());
            }

            return type;
        }

        return metamodel.managedType(managedTypeClass);
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

            currentClass = resolveFieldClass(currentClass, attr);
            if (attr.getPersistentAttributeType() == PersistentAttributeType.EMBEDDED) {
                currentType = metamodel.embeddable(currentClass);
            } else if (attr.getPersistentAttributeType() == PersistentAttributeType.BASIC) {
                currentType = null;
            } else if (JpaUtils.isJoinable(attr) && joinableAllowed) {
                joinableAllowed = false;
                if (i + 1 < attributeParts.length) {
                    currentType = metamodel.entity(currentClass);
                    // look ahead
                    Attribute<?, ?> nextAttr = JpaUtils.getAttribute(currentType, attributeParts[i + 1]);
                    if (!JpaUtils.getIdAttribute((EntityType<?>) currentType).getName().equals(nextAttr.getName())) {
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
                final Class<?> primitiveIdClass = ReflectionUtils.getPrimitiveClassOfWrapper(idClass);
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
            jpaReportedFieldClass = attr.getJavaType();
            if (attr.getJavaMember() instanceof Method) {
                Method method = (Method) attr.getJavaMember();
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
                }
            }
        }

        return getConcreterClass(fieldClass, jpaReportedFieldClass);
    }

    public static AttributeHolder getAttributeForJoining(EntityMetamodel metamodel, PathExpression expression) {
        JoinNode expressionBaseNode = ((JoinNode) expression.getPathReference().getBaseNode());
        String firstElementString = expression.getExpressions().get(0).toString();
        String baseNodeAlias;
        JoinNode baseNode = expressionBaseNode;

        do {
            baseNodeAlias = baseNode.getAlias();
        } while (!firstElementString.equals(baseNodeAlias) && (baseNode = baseNode.getParent()) != null);

        if (baseNode == null) {
            baseNodeAlias = null;
            if (expressionBaseNode.getParent() == null) {
                baseNode = expressionBaseNode;
            } else {
                baseNode = expressionBaseNode.getParent();
            }
        }

        return getAttributeForJoining(metamodel, baseNode.getPropertyClass(), baseNode.getTreatType(), expression, baseNodeAlias);
    }

    public static AttributeHolder getAttributeForJoining(EntityMetamodel metamodel, Class<?> type, String treatTypeName, Expression joinExpression, String baseNodeAlias) {
        return getAttributeForJoining(metamodel, getManagedType(metamodel, type, treatTypeName).getJavaType(), joinExpression, baseNodeAlias);
    }

    public static AttributeHolder getAttributeForJoining(EntityMetamodel metamodel, Class<?> type, Expression joinExpression, String baseNodeAlias) {
        PathTargetResolvingExpressionVisitor visitor = new PathTargetResolvingExpressionVisitor(metamodel, type, baseNodeAlias);
        joinExpression.accept(visitor);

        if (visitor.getPossibleTargets().size() > 1) {
            throw new IllegalArgumentException("Multiple possible target types for expression: " + joinExpression);
        }

        Map.Entry<Attribute<?, ?>, Class<?>> entry = visitor.getPossibleTargets().entrySet().iterator().next();
        return new AttributeHolder(entry.getKey(), entry.getValue());
    }
}
