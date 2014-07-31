/*
 * Copyright 2014 Blazebit.
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
import com.blazebit.lang.StringUtils;
import com.blazebit.persistence.view.Filter;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingFilter;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.SubqueryProvider;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.reflection.ReflectionUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 *
 * @author cpbec
 */
public abstract class AbstractMethodAttribute<X, Y> implements MethodAttribute<X, Y> {
    
    private final String name;
    private final ViewType<X> declaringType;
    private final Method javaMethod;
    private final Class<Y> javaType;
    private final Class<? extends Filter> filterMapping;
    private final String mapping;
    private final Class<? extends SubqueryProvider> subqueryProvider;
    private final boolean mappingParameter;
    private final boolean subqueryMapping;

    protected AbstractMethodAttribute(ViewType<X> viewType, Method method, Annotation mapping) {        
        this.name = StringUtils.firstToLower(method.getName().substring(3));
        this.declaringType = viewType;
        this.javaMethod = method;
        this.javaType = (Class<Y>) ReflectionUtils.getResolvedMethodReturnType(viewType.getJavaType(), method);
        MappingFilter mappingFilter = AnnotationUtils.findAnnotation(method, MappingFilter.class);
        this.filterMapping = mappingFilter == null ? null : mappingFilter.value();
        
        if (mapping instanceof Mapping) {
            this.mapping = ((Mapping) mapping).value();
            this.subqueryProvider = null;
            this.mappingParameter = false;
            this.subqueryMapping = false;
        } else if (mapping instanceof MappingParameter) {
            this.mapping = ((MappingParameter) mapping).value();
            this.subqueryProvider = null;
            this.mappingParameter = true;
            this.subqueryMapping = false;
        } else if (mapping instanceof MappingSubquery) {
            this.mapping = null;
            this.subqueryProvider = ((MappingSubquery) mapping).value();
            this.mappingParameter = false;
            this.subqueryMapping = true;
        } else {
            throw new IllegalArgumentException("Invalid mapping annotation " + mapping);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ViewType<X> getDeclaringType() {
        return declaringType;
    }

    @Override
    public Method getJavaMethod() {
        return javaMethod;
    }

    @Override
    public Class<Y> getJavaType() {
        return javaType;
    }
    
    public boolean isQueryParameter() {
        return mappingParameter;
    }
    
    public Class<? extends SubqueryProvider> getSubqueryProvider() {
        return subqueryProvider;
    }
    
    public String getMapping() {
        return mapping;
    }

    public PluralAttribute.CollectionType getCollectionType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Class<? extends Filter> getFilterMapping() {
        return filterMapping;
    }

    @Override
    public boolean isSubquery() {
        return subqueryMapping;
    }
    
    public static String validate(ViewType<?> viewType, Method m) {
        // Concrete methods are not mapped
        if (!Modifier.isAbstract(m.getModifiers()) || m.isBridge()) {
            return null;
        }
        
        // We only support bean style getters
        if (ReflectionUtils.isSetter(m)) {
            String attributeName = StringUtils.firstToLower(m.getName().substring(3));
            Method getter = ReflectionUtils.getGetter(viewType.getJavaType(), attributeName);
            
            if (getter == null) {
                throw new RuntimeException("The setter '" + m.getName() + "' from the entity view '" + viewType.getJavaType().getName() + "' has no corresponding getter!");
            }
            
            return null;
        } else if (!ReflectionUtils.isGetter(m)) {
            throw new IllegalArgumentException("The given method '" + m.getName() + "' from the entity view '" + viewType.getJavaType().getName() + "' is no bean style getter or setter!");
        }
        
        if (m.getExceptionTypes().length > 0) {
            throw new IllegalArgumentException("The given method '" + m.getName() + "' from the entity view '" + viewType.getJavaType().getName() + "' must not throw an exception!");
        }
        
        return StringUtils.firstToLower(m.getName().substring(3));
    }
    
    public static Annotation getMapping(ViewType<?> viewType, Method m) {        
        Class<?> entityClass = viewType.getEntityClass();
        Mapping mapping = AnnotationUtils.findAnnotation(m, Mapping.class);
        
        if (mapping == null) {
            MappingParameter mappingParameter = AnnotationUtils.findAnnotation(m, MappingParameter.class);
            
            if (mappingParameter != null) {
                if (mappingParameter.value().isEmpty()) {
                    throw new IllegalArgumentException("Illegal empty mapping parameter for the getter '" + m.getName() +  "' in the entity view'" + viewType.getJavaType().getName() + "'!");
                }
        
                return mappingParameter;
            }
            
            MappingSubquery mappingSubquery = AnnotationUtils.findAnnotation(m, MappingSubquery.class);
            
            if (mappingSubquery != null) {
                return mappingSubquery;
            }
            
            // Implicit mapping
            String attributeName = StringUtils.firstToLower(m.getName().substring(3));
            
            // First check if a the same method exists in the entity class
            boolean entityAttributeExists = ReflectionUtils.getMethod(entityClass, m.getName()) != null;
            // If not, check if a field with the given attribute name exists in the entity class
            entityAttributeExists = entityAttributeExists || ReflectionUtils.getField(entityClass, attributeName) != null;
            
            if (!entityAttributeExists) {
                throw new IllegalArgumentException("The entity class '" + entityClass.getName() + "' has no attribute '" + attributeName + "' that was implicitly mapped in entity view '" + viewType.getName() + "' in class '" + m.getDeclaringClass().getName() + "'");
            }
            
            mapping = new MappingLiteral(attributeName);
        }
        
        if (mapping.value().isEmpty()) {
            throw new IllegalArgumentException("Illegal empty mapping for the getter '" + m.getName() +  "' in the entity view'" + viewType.getJavaType().getName() + "'!");
        }
        
        return mapping;
    }
}
