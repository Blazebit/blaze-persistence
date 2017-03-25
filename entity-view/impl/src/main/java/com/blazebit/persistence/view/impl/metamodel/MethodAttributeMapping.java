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

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.annotation.AnnotationUtils;
import com.blazebit.persistence.view.BatchFetch;
import com.blazebit.persistence.view.CollectionMapping;
import com.blazebit.persistence.view.MappingCorrelated;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.MappingSingular;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedMethodCollectionAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedMethodListAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedMethodMappingSingularAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedMethodSetAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingMethodCollectionAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingMethodListAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingMethodMapAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingMethodSetAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingMethodSingularAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.SubqueryMethodSingularAttribute;
import com.blazebit.reflection.ReflectionUtils;

import javax.persistence.metamodel.ManagedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MethodAttributeMapping extends AttributeMapping {

    private final String attributeName;
    private final Method method;

    public MethodAttributeMapping(Class<?> entityViewClass, ManagedType<?> managedType, Annotation mapping, MetamodelBuildingContext context, String attributeName, Method method) {
        super(entityViewClass, managedType, mapping, context);
        this.attributeName = attributeName;
        this.method = method;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public Method getMethod() {
        return method;
    }

    @Override
    public String getErrorLocation() {
        return getLocation(attributeName, method);
    }

    public static String getLocation(String attributeName, Method method) {
        return "attribute " + attributeName + "[" + methodReference(method) + "]";
    }

    @Override
    public CollectionMapping getCollectionMapping() {
        return MetamodelUtils.getCollectionMapping(method);
    }

    @Override
    public BatchFetch getBatchFetch() {
        return AnnotationUtils.findAnnotation(method, BatchFetch.class);
    }

    public MethodAttributeMapping handleReplacement(AttributeMapping original) {
        if (original == null) {
            return this;
        }
        if (!(original instanceof MethodAttributeMapping)) {
            throw new IllegalStateException("Tried to replace attribute [" + original + "] with method attribute: " + this);
        }

        MethodAttributeMapping originalAttribute = (MethodAttributeMapping) original;
        // If the mapping is the same, just let it through
        if (mapping.equals(originalAttribute.getMapping())) {
            return originalAttribute;
        }

        // Also let through the attributes that are "specialized" in subclasses
        if (method.getDeclaringClass() != originalAttribute.getMethod().getDeclaringClass()
                && method.getDeclaringClass().isAssignableFrom(originalAttribute.getMethod().getDeclaringClass())) {
            // The method is overridden/specialized by the method of the existing attribute
            return originalAttribute;
        }

        // If the original is implicitly mapped, but this attribute isn't, we have to replace it
        if (originalAttribute.getMapping() instanceof MappingLiteral) {
            return this;
        }

        context.addError("Conflicting attribute mapping for attribute '" + attributeName + "' at the methods [" + methodReference(method) + ", " + methodReference(originalAttribute.getMethod()) + "] for managed view type '" + entityViewClass.getName() + "'");
        return originalAttribute;
    }

    private static String methodReference(Method method) {
        return method.getDeclaringClass().getName() + "." + method.getName();
    }

    // If you change something here don't forget to also update ParameterAttributeMapping#getParameterAttribute
    @SuppressWarnings("unchecked")
    public <X> AbstractMethodAttribute<? super X, ?> getMethodAttribute(ManagedViewTypeImpl<X> viewType) {
        if (attribute == null) {
            Class<?> attributeType = ReflectionUtils.getResolvedMethodReturnType(viewType.getJavaType(), method);
            boolean correlated = mapping instanceof MappingCorrelated || mapping instanceof MappingCorrelatedSimple;

            // Force singular mapping
            if (AnnotationUtils.findAnnotation(method, MappingSingular.class) != null || mapping instanceof MappingParameter) {
                if (correlated) {
                    attribute = new CorrelatedMethodMappingSingularAttribute<X, Object>(viewType, this, context);
                    return (AbstractMethodAttribute<? super X, ?>) attribute;
                } else {
                    attribute = new MappingMethodSingularAttribute<X, Object>(viewType, this, context);
                    return (AbstractMethodAttribute<? super X, ?>) attribute;
                }
            }

            if (Collection.class == attributeType) {
                if (correlated) {
                    attribute = new CorrelatedMethodCollectionAttribute<X, Object>(viewType, this, context);
                } else {
                    attribute = new MappingMethodCollectionAttribute<X, Object>(viewType, this, context);
                }
            } else if (List.class == attributeType) {
                if (correlated) {
                    attribute = new CorrelatedMethodListAttribute<X, Object>(viewType, this, context);
                } else {
                    attribute = new MappingMethodListAttribute<X, Object>(viewType, this, context);
                }
            } else if (Set.class == attributeType || SortedSet.class == attributeType || NavigableSet.class == attributeType) {
                if (correlated) {
                    attribute = new CorrelatedMethodSetAttribute<X, Object>(viewType, this, context);
                } else {
                    attribute = new MappingMethodSetAttribute<X, Object>(viewType, this, context);
                }
            } else if (Map.class == attributeType || SortedMap.class == attributeType || NavigableMap.class == attributeType) {
                if (correlated) {
                    context.addError("The mapping defined on method '" + viewType.getJavaType().getName() + "." + method.getName() + "' uses a Map type with a correlated mapping which is unsupported!");
                    attribute = null;
                } else {
                    attribute = new MappingMethodMapAttribute<X, Object, Object>(viewType, this, context);
                }
            } else if (mapping instanceof MappingSubquery) {
                attribute = new SubqueryMethodSingularAttribute<X, Object>(viewType, this, context);
            } else if (correlated) {
                attribute = new CorrelatedMethodMappingSingularAttribute<X, Object>(viewType, this, context);
            } else {
                attribute = new MappingMethodSingularAttribute<X, Object>(viewType, this, context);
            }
        }

        return (AbstractMethodAttribute<? super X, ?>) attribute;
    }

    @Override
    protected Class<?> resolveType() {
        return ReflectionUtils.getResolvedMethodReturnType(entityViewClass, method);
    }

    @Override
    protected Class<?> resolveKeyType() {
        Class<?> attributeType = ReflectionUtils.getResolvedMethodReturnType(entityViewClass, method);
        Class<?>[] typeArguments = ReflectionUtils.getResolvedMethodReturnTypeArguments(entityViewClass, method);
        // Force singular mapping
        if (typeArguments.length == 0 || AnnotationUtils.findAnnotation(method, MappingSingular.class) != null || !Map.class.isAssignableFrom(attributeType)) {
            return null;
        }

        return typeArguments[0];
    }

    @Override
    protected Class<?> resolveElementType() {
        Class<?> attributeType = ReflectionUtils.getResolvedMethodReturnType(entityViewClass, method);
        Class<?>[] typeArguments = ReflectionUtils.getResolvedMethodReturnTypeArguments(entityViewClass, method);
        // Force singular mapping
        if (typeArguments.length == 0 || AnnotationUtils.findAnnotation(method, MappingSingular.class) != null) {
            return attributeType;
        }

        return typeArguments[typeArguments.length - 1];
    }

}
