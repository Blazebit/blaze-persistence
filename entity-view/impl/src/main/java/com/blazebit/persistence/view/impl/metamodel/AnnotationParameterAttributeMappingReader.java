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
import com.blazebit.persistence.view.MappingInheritance;
import com.blazebit.persistence.view.MappingInheritanceMapKey;
import com.blazebit.persistence.view.MappingInheritanceSubtype;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.MappingSingular;
import com.blazebit.reflection.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AnnotationParameterAttributeMappingReader extends AbstractAnnotationAttributeMappingReader {

    public AnnotationParameterAttributeMappingReader(MetamodelBootContext context) {
        super(context);
    }

    public ParameterAttributeMapping readParameterAttributeMapping(ViewMapping viewMapping, Annotation mapping, ConstructorMapping constructorMapping, int index, Annotation[] annotations) {
        Map<Class<?>, Annotation> parameterAnnotations = new HashMap<>(annotations.length);
        for (Annotation annotation : annotations) {
            parameterAnnotations.put(annotation.annotationType(), annotation);
        }

        Constructor<?> constructor = constructorMapping.getConstructor();
        Class<?> type = resolveType(constructor, index);
        Class<?> keyType = resolveKeyType(constructor, index, parameterAnnotations);
        Class<?> elementType = resolveElementType(constructor, index, parameterAnnotations);
        Map<Class<?>, String> typeMappings = resolveInheritanceSubtypeMappings(parameterAnnotations, type);
        Map<Class<?>, String> keyTypeMappings = resolveKeyInheritanceSubtypeMappings(parameterAnnotations, keyType);
        Map<Class<?>, String> elementTypeMappings = resolveElementInheritanceSubtypeMappings(parameterAnnotations, elementType);

        boolean forceSingular = parameterAnnotations.containsKey(MappingSingular.class);
        boolean isCollection = !forceSingular && (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type));

        ParameterAttributeMapping parameterMapping = new ParameterAttributeMapping(viewMapping, mapping, context, constructorMapping, index, isCollection, type, keyType, elementType, typeMappings, keyTypeMappings, elementTypeMappings);

        CollectionMapping collectionMapping = (CollectionMapping) parameterAnnotations.get(CollectionMapping.class);

        applyCollectionMapping(parameterMapping, collectionMapping);

        BatchFetch batchFetch = (BatchFetch) parameterAnnotations.get(BatchFetch.class);
        if (batchFetch != null) {
            parameterMapping.setDefaultBatchSize(batchFetch.size());
        }

        return parameterMapping;
    }

    private Class<?> resolveType(Constructor<?> constructor, int index) {
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

    private Class<?> resolveKeyType(Constructor<?> constructor, int index, Map<Class<?>, Annotation> parameterAnnotations) {
        Class<?> concreteClass = constructor.getDeclaringClass();
        Type parameterType = constructor.getGenericParameterTypes()[index];
        Class<?>[] typeArguments = ReflectionUtils.resolveTypeArguments(concreteClass, parameterType);

        // Force singular mapping
        if (typeArguments.length == 0 || parameterAnnotations.get(MappingSingular.class) != null
                || parameterAnnotations.get(MappingParameter.class) != null || !Map.class.isAssignableFrom(resolveType(constructor, index))) {
            return null;
        }

        return typeArguments[0];
    }

    private Class<?> resolveElementType(Constructor<?> constructor, int index, Map<Class<?>, Annotation> parameterAnnotations) {
        Class<?> concreteClass = constructor.getDeclaringClass();
        Type parameterType = constructor.getGenericParameterTypes()[index];
        Class<?>[] typeArguments = ReflectionUtils.resolveTypeArguments(concreteClass, parameterType);
        // Force singular mapping
        if (typeArguments.length == 0 || parameterAnnotations.get(MappingSingular.class) != null
                || parameterAnnotations.get(MappingParameter.class) != null) {
            return resolveType(constructor, index);
        }

        return typeArguments[typeArguments.length - 1];
    }

    private Map<Class<?>, String> resolveInheritanceSubtypeMappings(Map<Class<?>, Annotation> parameterAnnotations, Class<?> type) {
        MappingInheritance inheritance = (MappingInheritance) parameterAnnotations.get(MappingInheritance.class);
        if (inheritance != null) {
            Class<?> baseType = null;
            if (!inheritance.onlySubtypes()) {
                baseType = type;
            }
            return resolveInheritanceSubtypeMappings(parameterAnnotations, baseType, inheritance.value());
        }
        return resolveInheritanceSubtypeMappings(parameterAnnotations, null, null);
    }

    private Map<Class<?>, String> resolveKeyInheritanceSubtypeMappings(Map<Class<?>, Annotation> parameterAnnotations, Class<?> keyType) {
        MappingInheritanceMapKey inheritance = (MappingInheritanceMapKey) parameterAnnotations.get(MappingInheritanceMapKey.class);
        if (inheritance != null) {
            Class<?> baseType = null;
            if (!inheritance.onlySubtypes()) {
                baseType = keyType;
            }
            return resolveInheritanceSubtypeMappings(parameterAnnotations, baseType, inheritance.value());
        }
        return null;
    }

    private Map<Class<?>, String> resolveElementInheritanceSubtypeMappings(Map<Class<?>, Annotation> parameterAnnotations, Class<?> elementType) {
        MappingInheritance inheritance = (MappingInheritance) parameterAnnotations.get(MappingInheritance.class);
        if (inheritance != null) {
            Class<?> baseType = null;
            if (!inheritance.onlySubtypes()) {
                baseType = elementType;
            }
            return resolveInheritanceSubtypeMappings(parameterAnnotations, baseType, inheritance.value());
        }
        return resolveInheritanceSubtypeMappings(parameterAnnotations, null, null);
    }

    private Map<Class<?>, String> resolveInheritanceSubtypeMappings(Map<Class<?>, Annotation> parameterAnnotations, Class<?> baseType, MappingInheritanceSubtype[] subtypes) {
        if (subtypes == null) {
            MappingInheritanceSubtype subtype = (MappingInheritanceSubtype) parameterAnnotations.get(MappingInheritanceSubtype.class);
            if (subtype == null) {
                return null;
            } else {
                subtypes = new MappingInheritanceSubtype[]{ subtype };
            }
        }

        Map<Class<?>, String> mappings = new HashMap<>(subtypes.length);

        if (baseType != null) {
            mappings.put(baseType, null);
        }

        for (MappingInheritanceSubtype subtype : subtypes) {
            String mapping = subtype.mapping();
            if (mapping.isEmpty()) {
                mapping = null;
            }
            mappings.put(subtype.value(), mapping);
        }

        return mappings;
    }
}
