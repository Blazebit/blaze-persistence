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

import com.blazebit.annotation.AnnotationUtils;
import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.PathTargetResolvingExpressionVisitor;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.view.CollectionMapping;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.reflection.ReflectionUtils;

import javax.persistence.OrderColumn;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ListAttribute;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public final class MetamodelUtils {

    private MetamodelUtils() {
    }

    public static CollectionMapping getCollectionMapping(Constructor<?> constructor, int index) {
        return getCollectionMapping(findAnnotation(constructor, index, CollectionMapping.class));
    }
    
    public static CollectionMapping getCollectionMapping(Method method) {
        return getCollectionMapping(AnnotationUtils.findAnnotation(method, CollectionMapping.class));
    }

    public static boolean isSorted(MappingConstructor<?> mappingConstructor, int index) {
        return isSorted(mappingConstructor.getJavaConstructor(), index);
    }

    public static boolean isSorted(Constructor<?> constructor, int index) {
        Class<?> concreteClass = constructor.getDeclaringClass();
        Type type = constructor.getGenericParameterTypes()[index];
        
        if (type instanceof Class<?>) {
            return isSorted((Class<?>) type);
        } else if (type instanceof TypeVariable<?>) {
            return isSorted(ReflectionUtils.resolveTypeVariable(concreteClass, (TypeVariable<?>) type));
        } else {
            return isSorted(constructor.getParameterTypes()[index]);
        }
    }
    
    public static boolean isSorted(Class<?> clazz, Method method) {
        return isSorted(ReflectionUtils.getResolvedMethodReturnType(clazz, method));
    }
    
    public static boolean isSorted(Class<?> clazz) {
        if (SortedSet.class.isAssignableFrom(clazz)) {
            return true;
        } else if (SortedMap.class.isAssignableFrom(clazz)) {
            return true;
        } else {
            return false;
        }
    }
    
    @SuppressWarnings("unchecked")
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

    public static boolean isIndexedList(EntityMetamodel metamodel, ExpressionFactory expressionFactory, Class<?> entityClass, String mapping) {
        if (mapping == null || mapping.isEmpty()) {
            return false;
        }

        PathTargetResolvingExpressionVisitor visitor = new PathTargetResolvingExpressionVisitor(metamodel, metamodel.managedType(entityClass), null);
        expressionFactory.createSimpleExpression(mapping, false).accept(visitor);
        Map<Attribute<?, ?>, javax.persistence.metamodel.Type<?>> possibleTargets = visitor.getPossibleTargets();
        Iterator<Map.Entry<Attribute<?, ?>, javax.persistence.metamodel.Type<?>>> iter = possibleTargets.entrySet().iterator();
        // It must have one, otherwise a parse error would have been thrown already
        Map.Entry<Attribute<?, ?>, ?> targetEntry = iter.next();
        boolean indexed = isIndexedList(targetEntry.getKey());
        
        while (iter.hasNext()) {
            targetEntry = iter.next();
            if (indexed != isIndexedList(targetEntry.getKey())) {
                throw new IllegalArgumentException("Inconclusive result on checking whether the expression [" + mapping + "] resolves to an indexed list on class [" + entityClass.getName() + "].");
            }
        }
        
        return indexed;
    }
    
    private static boolean isIndexedList(Attribute<?, ?> targetAttribute) {
        if (!(targetAttribute instanceof ListAttribute<?, ?>)) {
            return false;
        }

        Member member = targetAttribute.getJavaMember();
        if (member instanceof Field) {
            return ((Field) member).getAnnotation(OrderColumn.class) != null;
        } else {
            return AnnotationUtils.findAnnotation((Method) member, OrderColumn.class) != null;
        }
    }
    
    public static <A extends Annotation> A findAnnotation(Constructor<?> constructor, int index, Class<A> annotationClass) {
        return findAnnotation(constructor.getParameterAnnotations()[index], annotationClass);
    }

    @SuppressWarnings("unchecked")
    private static <A extends Annotation> A findAnnotation(Annotation[] annotations, Class<A> annotationClass) {
        for (int i = 0; i < annotations.length; i++) {
            if (annotationClass.isAssignableFrom(annotations[i].annotationType())) {
                return (A) annotations[i];
            }
        }
        
        return null;
    }
    
    public static CollectionMapping getCollectionMapping(CollectionMapping mapping) {
        if (mapping != null) {
            return mapping;
        }
        
        return new CollectionMappingLiteral(Comparator.class, false, false);
    }
}
