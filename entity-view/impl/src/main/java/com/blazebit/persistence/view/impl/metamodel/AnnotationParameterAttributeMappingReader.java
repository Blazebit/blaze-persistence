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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
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

        Class<?> entityViewClass = viewMapping.getEntityViewClass();
        Type parameterType = ReflectionUtils.resolve(entityViewClass, constructor.getGenericParameterTypes()[index]);
        Class<?> type = ReflectionUtils.resolveType(entityViewClass, parameterType);
        boolean forceSingular = parameterAnnotations.containsKey(MappingSingular.class) || parameterAnnotations.containsKey(MappingParameter.class);
        boolean isCollection = !forceSingular && (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type));
        Type declaredType;
        Type declaredKeyType;
        Type declaredElementType;
        Class<?> keyType;
        Class<?> elementType;
        if (isCollection) {
            Type[] typeArguments = ((ParameterizedType) parameterType).getActualTypeArguments();
            declaredType = parameterType;
            declaredKeyType = typeArguments.length > 1 ? typeArguments[0] : null;
            declaredElementType = typeArguments[typeArguments.length - 1];
            keyType = ReflectionUtils.resolveType(entityViewClass, declaredKeyType);
            elementType = ReflectionUtils.resolveType(entityViewClass, declaredElementType);
        } else {
            declaredType = parameterType;
            declaredKeyType = null;
            declaredElementType = null;
            keyType = null;
            elementType = null;
        }

        Map<Class<?>, String> typeMappings = resolveInheritanceSubtypeMappings(parameterAnnotations, type);
        Map<Class<?>, String> keyTypeMappings = resolveKeyInheritanceSubtypeMappings(parameterAnnotations, keyType);
        Map<Class<?>, String> elementTypeMappings = resolveElementInheritanceSubtypeMappings(parameterAnnotations, elementType);

        ParameterAttributeMapping parameterMapping = new ParameterAttributeMapping(viewMapping, mapping, context, constructorMapping, index, isCollection, type, keyType, elementType, declaredType, declaredKeyType, declaredElementType, typeMappings, keyTypeMappings, elementTypeMappings);

        CollectionMapping collectionMapping = (CollectionMapping) parameterAnnotations.get(CollectionMapping.class);

        applyCollectionMapping(parameterMapping, collectionMapping);

        BatchFetch batchFetch = (BatchFetch) parameterAnnotations.get(BatchFetch.class);
        if (batchFetch != null) {
            parameterMapping.setDefaultBatchSize(batchFetch.size());
        }

        return parameterMapping;
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
