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

import com.blazebit.persistence.view.BatchFetch;
import com.blazebit.persistence.view.CollectionMapping;
import com.blazebit.persistence.view.MappingCorrelated;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.MappingSingular;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedParameterCollectionAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedParameterListAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedParameterMappingSingularAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedParameterSetAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingParameterCollectionAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingParameterListAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingParameterMapAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingParameterMappingSingularAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingParameterSetAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.SubqueryParameterSingularAttribute;
import com.blazebit.reflection.ReflectionUtils;

import javax.persistence.metamodel.ManagedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
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
public class ParameterAttributeMapping extends AttributeMapping {

    private final Constructor<?> constructor;
    private final int index;

    public ParameterAttributeMapping(Class<?> entityViewClass, ManagedType<?> managedType, Annotation mapping, MetamodelBuildingContext context, Constructor<?> constructor, int index) {
        super(entityViewClass, managedType, mapping, context);
        this.constructor = constructor;
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String getErrorLocation() {
        return getLocation(constructor, index);
    }

    public static String getLocation(Constructor<?> constructor, int index) {
        return "parameter at index " + index + " of constructor[" + constructor + "]";
    }

    @Override
    public CollectionMapping getCollectionMapping() {
        return MetamodelUtils.getCollectionMapping(constructor, index);
    }

    @Override
    public BatchFetch getBatchFetch() {
        return findAnnotation(BatchFetch.class);
    }

    // If you change something here don't forget to also update MethodAttributeMapping#getMethodAttribute
    @SuppressWarnings("unchecked")
    public <X> AbstractParameterAttribute<? super X, ?> getParameterAttribute(MappingConstructorImpl<X> constructor) {
        if (attribute == null) {
            Type parameterType = constructor.getJavaConstructor().getGenericParameterTypes()[index];
            Class<?> attributeType;

            if (parameterType instanceof TypeVariable<?>) {
                attributeType = ReflectionUtils.resolveTypeVariable(constructor.getDeclaringType().getJavaType(), (TypeVariable<?>) parameterType);
            } else {
                attributeType = constructor.getJavaConstructor().getParameterTypes()[index];
            }

            boolean correlated = mapping instanceof MappingCorrelated || mapping instanceof MappingCorrelatedSimple;

            if (mapping instanceof MappingParameter) {
                attribute = new MappingParameterMappingSingularAttribute<X, Object>(constructor, this, context);
                return (AbstractParameterAttribute<? super X, ?>) attribute;
            }

            Annotation[] annotations = constructor.getJavaConstructor().getParameterAnnotations()[index];

            for (Annotation a : annotations) {
                // Force singular mapping
                if (MappingSingular.class == a.annotationType()) {
                    if (correlated) {
                        attribute = new CorrelatedParameterMappingSingularAttribute<X, Object>(constructor, this, context);
                        return (AbstractParameterAttribute<? super X, ?>) attribute;
                    } else {
                        attribute = new MappingParameterMappingSingularAttribute<X, Object>(constructor, this, context);
                        return (AbstractParameterAttribute<? super X, ?>) attribute;
                    }
                }
            }

            if (Collection.class == attributeType) {
                if (correlated) {
                    attribute = new CorrelatedParameterCollectionAttribute<X, Object>(constructor, this, context);
                } else {
                    attribute = new MappingParameterCollectionAttribute<X, Object>(constructor, this, context);
                }
            } else if (List.class == attributeType) {
                if (correlated) {
                    attribute = new CorrelatedParameterListAttribute<X, Object>(constructor, this, context);
                } else {
                    attribute = new MappingParameterListAttribute<X, Object>(constructor, this, context);
                }
            } else if (Set.class == attributeType || SortedSet.class == attributeType || NavigableSet.class == attributeType) {
                if (correlated) {
                    attribute = new CorrelatedParameterSetAttribute<X, Object>(constructor, this, context);
                } else {
                    attribute = new MappingParameterSetAttribute<X, Object>(constructor, this, context);
                }
            } else if (Map.class == attributeType || SortedMap.class == attributeType || NavigableMap.class == attributeType) {
                if (correlated) {
                    context.addError("Parameter with the index '" + index + "' of the constructor '" + constructor.getJavaConstructor() + "' uses a Map type with a correlated mapping which is unsupported!");
                    attribute = null;
                } else {
                    attribute = new MappingParameterMapAttribute<X, Object, Object>(constructor, this, context);
                }
            } else if (mapping instanceof MappingSubquery) {
                attribute = new SubqueryParameterSingularAttribute<X, Object>(constructor, this, context);
            } else {
                if (correlated) {
                    attribute = new CorrelatedParameterMappingSingularAttribute<X, Object>(constructor, this, context);
                } else {
                    attribute = new MappingParameterMappingSingularAttribute<X, Object>(constructor, this, context);
                }
            }
        }

        return (AbstractParameterAttribute<? super X, ?>) attribute;
    }

    @Override
    protected Class<?> resolveType() {
        Class<?> concreteClass = constructor.getDeclaringClass();
        Type type = constructor.getGenericParameterTypes()[index];

        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof TypeVariable<?>) {
            return ReflectionUtils.resolveTypeVariable(concreteClass, (TypeVariable<?>) type);
        } else {
            return constructor.getParameterTypes()[index];
        }
    }

    @Override
    protected Class<?> resolveKeyType() {
        Class<?> concreteClass = constructor.getDeclaringClass();
        Type parameterType = constructor.getGenericParameterTypes()[index];
        Class<?>[] typeArguments = ReflectionUtils.resolveTypeArguments(concreteClass, parameterType);

        // Force singular mapping
        if (typeArguments.length == 0 || findAnnotation(MappingSingular.class) != null || !Map.class.isAssignableFrom(resolveType())) {
            return null;
        }

        return typeArguments[0];
    }

    @Override
    protected Class<?> resolveElementType() {
        Class<?> concreteClass = constructor.getDeclaringClass();
        Type parameterType = constructor.getGenericParameterTypes()[index];
        Class<?>[] typeArguments = ReflectionUtils.resolveTypeArguments(concreteClass, parameterType);
        // Force singular mapping
        if (typeArguments.length == 0 || findAnnotation(MappingSingular.class) != null) {
            return resolveType();
        }

        return typeArguments[typeArguments.length - 1];
    }

    private <T extends Annotation> T findAnnotation(Class<T> annotationType) {
        return MetamodelUtils.findAnnotation(constructor, index, annotationType);
    }

}
