/*
 * Copyright 2014 - 2024 Blazebit.
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

import com.blazebit.persistence.view.MappingIndex;
import com.blazebit.persistence.view.MappingInheritance;
import com.blazebit.persistence.view.MappingInheritanceMapKey;
import com.blazebit.persistence.view.MappingInheritanceSubtype;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.MappingSingular;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.reflection.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
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

    public ParameterAttributeMapping readParameterAttributeMapping(ViewMapping viewMapping, Annotation mapping, ConstructorMapping constructorMapping, int index, AnnotatedElement annotatedElement) {
        Constructor<?> constructor = constructorMapping.getConstructor();

        Class<?> entityViewClass = viewMapping.getEntityViewClass();
        Type parameterType = ReflectionUtils.resolve(entityViewClass, constructor.getGenericParameterTypes()[index]);
        Class<?> type = ReflectionUtils.resolveType(entityViewClass, parameterType);
        boolean forceSingular = annotatedElement.isAnnotationPresent(MappingSingular.class) || annotatedElement.isAnnotationPresent(MappingParameter.class);
        boolean isCollection = !forceSingular && (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type));
        Type declaredType;
        Type declaredKeyType;
        Type declaredElementType;
        Class<?> keyType;
        Class<?> elementType;
        PluralAttribute.ElementCollectionType elementCollectionType = null;
        if (isCollection) {
            Type[] typeArguments = ((ParameterizedType) parameterType).getActualTypeArguments();
            declaredType = parameterType;
            declaredKeyType = typeArguments.length > 1 ? typeArguments[0] : null;
            declaredElementType = typeArguments[typeArguments.length - 1];
            keyType = ReflectionUtils.resolveType(entityViewClass, declaredKeyType);
            elementType = ReflectionUtils.resolveType(entityViewClass, declaredElementType);
            if (elementType != null && Collection.class.isAssignableFrom(elementType)) {
                Type[] elementTypeArguments = ((ParameterizedType) declaredElementType).getActualTypeArguments();
                elementCollectionType = getElementCollectionType(elementType);
                declaredElementType = elementTypeArguments[0];
                elementType = ReflectionUtils.resolveType(entityViewClass, declaredElementType);
            }
        } else {
            declaredType = parameterType;
            declaredKeyType = null;
            declaredElementType = null;
            keyType = null;
            elementType = null;
        }

        Map<Class<?>, String> typeMappings = resolveInheritanceSubtypeMappings(annotatedElement, type);
        Map<Class<?>, String> keyTypeMappings = resolveKeyInheritanceSubtypeMappings(annotatedElement, keyType);
        Map<Class<?>, String> elementTypeMappings = resolveElementInheritanceSubtypeMappings(annotatedElement, elementType);

        ParameterAttributeMapping parameterMapping = new ParameterAttributeMapping(viewMapping, mapping, annotatedElement.getAnnotation(MappingIndex.class), context, constructorMapping, index, isCollection, elementCollectionType, type, keyType, elementType, declaredType, declaredKeyType, declaredElementType, typeMappings, keyTypeMappings, elementTypeMappings);

        applyCommonMappings(parameterMapping, annotatedElement);

        return parameterMapping;
    }

    private Map<Class<?>, String> resolveInheritanceSubtypeMappings(AnnotatedElement annotatedElement, Class<?> type) {
        MappingInheritance inheritance = annotatedElement.getAnnotation(MappingInheritance.class);
        if (inheritance != null) {
            Class<?> baseType = null;
            if (!inheritance.onlySubtypes()) {
                baseType = type;
            }
            return resolveInheritanceSubtypeMappings(annotatedElement, baseType, inheritance.value());
        }
        return resolveInheritanceSubtypeMappings(annotatedElement, null, null);
    }

    private Map<Class<?>, String> resolveKeyInheritanceSubtypeMappings(AnnotatedElement annotatedElement, Class<?> keyType) {
        MappingInheritanceMapKey inheritance = annotatedElement.getAnnotation(MappingInheritanceMapKey.class);
        if (inheritance != null) {
            Class<?> baseType = null;
            if (!inheritance.onlySubtypes()) {
                baseType = keyType;
            }
            return resolveInheritanceSubtypeMappings(annotatedElement, baseType, inheritance.value());
        }
        return null;
    }

    private Map<Class<?>, String> resolveElementInheritanceSubtypeMappings(AnnotatedElement annotatedElement, Class<?> elementType) {
        MappingInheritance inheritance = annotatedElement.getAnnotation(MappingInheritance.class);
        if (inheritance != null) {
            Class<?> baseType = null;
            if (!inheritance.onlySubtypes()) {
                baseType = elementType;
            }
            return resolveInheritanceSubtypeMappings(annotatedElement, baseType, inheritance.value());
        }
        return resolveInheritanceSubtypeMappings(annotatedElement, null, null);
    }

    private Map<Class<?>, String> resolveInheritanceSubtypeMappings(AnnotatedElement annotatedElement, Class<?> baseType, MappingInheritanceSubtype[] subtypes) {
        if (subtypes == null) {
            MappingInheritanceSubtype subtype = annotatedElement.getAnnotation(MappingInheritanceSubtype.class);
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
