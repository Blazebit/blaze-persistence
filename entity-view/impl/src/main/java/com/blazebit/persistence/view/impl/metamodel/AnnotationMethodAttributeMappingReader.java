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
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.MappingInheritance;
import com.blazebit.persistence.view.MappingInheritanceMapKey;
import com.blazebit.persistence.view.MappingInheritanceSubtype;
import com.blazebit.persistence.view.MappingInverse;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.MappingSingular;
import com.blazebit.persistence.view.OptimisticLock;
import com.blazebit.persistence.view.UpdatableMapping;
import com.blazebit.reflection.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AnnotationMethodAttributeMappingReader extends AbstractAnnotationAttributeMappingReader {

    public AnnotationMethodAttributeMappingReader(MetamodelBootContext context) {
        super(context);
    }

    public MethodAttributeMapping readMethodAttributeMapping(ViewMapping viewMapping, Annotation mapping, String attributeName, Method method) {
        Class<?> entityViewClass = viewMapping.getEntityViewClass();
        Class<?> type = resolveType(entityViewClass, method);
        Class<?> keyType = resolveKeyType(entityViewClass, method);
        Class<?> elementType = resolveElementType(entityViewClass, method);
        Map<Class<?>, String> typeMappings = resolveInheritanceSubtypeMappings(method, type);
        Map<Class<?>, String> keyTypeMappings = resolveKeyInheritanceSubtypeMappings(method, keyType);
        Map<Class<?>, String> elementTypeMappings = resolveElementInheritanceSubtypeMappings(method, elementType);

        boolean forceSingular = AnnotationUtils.findAnnotation(method, MappingSingular.class) != null;
        boolean isCollection = !forceSingular && (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type));

        MethodAttributeMapping attributeMapping = new MethodAttributeMapping(viewMapping, mapping, context, attributeName, method, isCollection, type, keyType, elementType, typeMappings, keyTypeMappings, elementTypeMappings);

        if (AnnotationUtils.findAnnotation(method, IdMapping.class) != null) {
            viewMapping.setIdAttributeMapping(attributeMapping);
        }
        OptimisticLock optimisticLock = AnnotationUtils.findAnnotation(method, OptimisticLock.class);
        if (optimisticLock != null) {
            attributeMapping.setOptimisticLockProtected(!optimisticLock.exclude());
        }

        CollectionMapping collectionMapping = AnnotationUtils.findAnnotation(method, CollectionMapping.class);

        applyCollectionMapping(attributeMapping, collectionMapping);

        BatchFetch batchFetch = AnnotationUtils.findAnnotation(method, BatchFetch.class);
        if (batchFetch != null) {
            attributeMapping.setDefaultBatchSize(batchFetch.size());
        }

        UpdatableMapping updatableMapping = AnnotationUtils.findAnnotation(method, UpdatableMapping.class);
        if (updatableMapping != null) {
            attributeMapping.setUpdatable(updatableMapping.updatable(), updatableMapping.cascade(), updatableMapping.subtypes(), updatableMapping.persistSubtypes(), updatableMapping.updateSubtypes());
        }

        MappingInverse inverseMapping = AnnotationUtils.findAnnotation(method, MappingInverse.class);
        if (inverseMapping != null) {
            attributeMapping.setInverseRemoveStrategy(inverseMapping.removeStrategy());
            String mappedBy = inverseMapping.mappedBy();
            if (!mappedBy.isEmpty()) {
                attributeMapping.setMappedBy(mappedBy);
            }
        }

        return attributeMapping;
    }


    private Class<?> resolveType(Class<?> entityViewClass, Method method) {
        return ReflectionUtils.getResolvedMethodReturnType(entityViewClass, method);
    }

    private Class<?> resolveKeyType(Class<?> entityViewClass, Method method) {
        Class<?> attributeType = ReflectionUtils.getResolvedMethodReturnType(entityViewClass, method);
        Class<?>[] typeArguments = ReflectionUtils.getResolvedMethodReturnTypeArguments(entityViewClass, method);
        // Force singular mapping
        if (typeArguments.length == 0 || AnnotationUtils.findAnnotation(method, MappingSingular.class) != null || AnnotationUtils.findAnnotation(method, MappingParameter.class) != null || !Map.class.isAssignableFrom(attributeType)) {
            return null;
        }

        return typeArguments[0];
    }

    private Class<?> resolveElementType(Class<?> entityViewClass, Method method) {
        Class<?> attributeType = ReflectionUtils.getResolvedMethodReturnType(entityViewClass, method);
        Class<?>[] typeArguments = ReflectionUtils.getResolvedMethodReturnTypeArguments(entityViewClass, method);
        // Force singular mapping
        if (typeArguments.length == 0 || AnnotationUtils.findAnnotation(method, MappingSingular.class) != null || AnnotationUtils.findAnnotation(method, MappingParameter.class) != null) {
            return attributeType;
        }

        return typeArguments[typeArguments.length - 1];
    }

    private Map<Class<?>, String> resolveInheritanceSubtypeMappings(Method method, Class<?> type) {
        MappingInheritance inheritance = AnnotationUtils.findAnnotation(method, MappingInheritance.class);
        if (inheritance != null) {
            Class<?> baseType = null;
            if (!inheritance.onlySubtypes()) {
                baseType = type;
            }
            return resolveInheritanceSubtypeMappings(method, baseType, inheritance.value());
        }
        return resolveInheritanceSubtypeMappings(method, null, null);
    }

    private Map<Class<?>, String> resolveKeyInheritanceSubtypeMappings(Method method, Class<?> keyType) {
        MappingInheritanceMapKey inheritance = AnnotationUtils.findAnnotation(method, MappingInheritanceMapKey.class);
        if (inheritance != null) {
            Class<?> baseType = null;
            if (!inheritance.onlySubtypes()) {
                baseType = keyType;
            }
            return resolveInheritanceSubtypeMappings(method, baseType, inheritance.value());
        }
        return null;
    }

    private Map<Class<?>, String> resolveElementInheritanceSubtypeMappings(Method method, Class<?> elementType) {
        MappingInheritance inheritance = AnnotationUtils.findAnnotation(method, MappingInheritance.class);
        if (inheritance != null) {
            Class<?> baseType = null;
            if (!inheritance.onlySubtypes()) {
                baseType = elementType;
            }
            return resolveInheritanceSubtypeMappings(method, baseType, inheritance.value());
        }
        return resolveInheritanceSubtypeMappings(method, null, null);
    }

    private Map<Class<?>, String> resolveInheritanceSubtypeMappings(Method method, Class<?> baseType, MappingInheritanceSubtype[] subtypes) {
        if (subtypes == null) {
            MappingInheritanceSubtype subtype = AnnotationUtils.findAnnotation(method, MappingInheritanceSubtype.class);
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
