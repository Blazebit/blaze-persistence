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
package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.annotation.AnnotationUtils;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.ExpressionFactoryImpl;
import com.blazebit.persistence.impl.expression.SimpleCachingExpressionFactory;
import com.blazebit.persistence.view.CollectionMapping;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.impl.TargetResolvingExpressionVisitor;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.reflection.ReflectionUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.OrderColumn;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public final class MetamodelUtils {
    
    private static final ExpressionFactory expressionFactory = new SimpleCachingExpressionFactory(new ExpressionFactoryImpl());
    
    public static CollectionMapping getCollectionMapping(MappingConstructor<?> mappingConstructor, int index) {
        return getCollectionMapping(findAnnotation(mappingConstructor, index, CollectionMapping.class));
    }
    
    public static CollectionMapping getCollectionMapping(Method method) {
        return getCollectionMapping(AnnotationUtils.findAnnotation(method, CollectionMapping.class));
    }
    
    public static boolean isSorted(MappingConstructor<?> mappingConstructor, int index) {
        Class<?> concreteClass = mappingConstructor.getDeclaringType().getJavaType();
        Type type = mappingConstructor.getJavaConstructor().getGenericParameterTypes()[index];
        
        if (type instanceof Class<?>) {
            return isSorted((Class<?>) type);
        } else if (type instanceof TypeVariable<?>) {
            return isSorted(ReflectionUtils.resolveTypeVariable(concreteClass, (TypeVariable<?>) type));
        } else {
            return isSorted(mappingConstructor.getJavaConstructor().getParameterTypes()[index]);
        }
    }
    
    public static boolean isSorted(Class<?> clazz, Method method) {
        return isSorted(ReflectionUtils.getResolvedMethodReturnType(clazz, method));
    }
    
    private static boolean isSorted(Class<?> clazz) {
        if (SortedSet.class.isAssignableFrom(clazz)) {
            return true;
        } else if (SortedMap.class.isAssignableFrom(clazz)) {
            return true;
        } else {
            return false;
        }
    }
    
    public static <T> Class<Comparator<T>> getComparatorClass(CollectionMapping mapping) {
        if (Comparator.class == mapping.comparator()) {
            return null;
        }
        
        return (Class<Comparator<T>>) mapping.comparator();
    }
    
    public static <T> Comparator<T> getComparator(Class<Comparator<T>> clazz) {
        if (clazz == null) {
            return null;
        }
        
        try {
            return clazz.newInstance();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public static boolean isIndexedList(Class<?> entityClass, Annotation mappingAnnotation) {
        if (mappingAnnotation instanceof MappingSubquery || mappingAnnotation instanceof MappingParameter) {
            return false;
        }
        
        String mapping;
        
        if (mappingAnnotation instanceof IdMapping) {
            mapping = ((IdMapping) mappingAnnotation).value();
        } else if (mappingAnnotation instanceof Mapping) {
            mapping = ((Mapping) mappingAnnotation).value();
        } else {
            throw new IllegalArgumentException("Unkown mapping encountered: " + mappingAnnotation);
        }
        
        TargetResolvingExpressionVisitor visitor = new TargetResolvingExpressionVisitor(entityClass);
        expressionFactory.createSimpleExpression(mapping).accept(visitor);
        Map<Method, Class<?>> possibleTargets = visitor.getPossibleTargets();
        Iterator<Map.Entry<Method, Class<?>>> iter = possibleTargets.entrySet().iterator();
        // It must have one, otherwise a parse error would have been thrown already
        Map.Entry<Method, Class<?>> targetEntry = iter.next();
        boolean indexed = isIndexedList(targetEntry.getKey(), targetEntry.getValue());
        
        while (iter.hasNext()) {
            targetEntry = iter.next();
            if (indexed != isIndexedList(targetEntry.getKey(), targetEntry.getValue())) {
                throw new IllegalArgumentException("Inconclusive result on checking whether the expression [" + mapping + "] resolves to an indexed list on class [" + entityClass.getName() + "].");
            }
        }
        
        return indexed;
    }
    
    private static boolean isIndexedList(Method targetMethod, Class<?> targetType) {
        if (!List.class.isAssignableFrom(targetType)) {
            return false;
        }
        
        return AnnotationUtils.findAnnotation(targetMethod, OrderColumn.class) != null;
    }
    
    private static <A extends Annotation> A findAnnotation(MappingConstructor<?> mappingConstructor, int index, Class<A> annotationClass) {
        return findAnnotation(mappingConstructor.getJavaConstructor().getParameterAnnotations()[index], annotationClass);
    }
    
    private static <A extends Annotation> A findAnnotation(Annotation[] annotations, Class<A> annotationClass) {
        for (int i = 0; i < annotations.length; i++) {
            if (annotationClass.isAssignableFrom(annotations[i].annotationType())) {
                return (A) annotations[i];
            }
        }
        
        return null;
    }
    
    private static CollectionMapping getCollectionMapping(CollectionMapping mapping) {
        if (mapping != null) {
            return mapping;
        }
        
        return new CollectionMappingLiteral(Comparator.class, false, false);
    }
}
