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
import com.blazebit.persistence.view.AttributeFilter;
import com.blazebit.persistence.view.AttributeFilters;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingCorrelated;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.UpdatableMapping;
import com.blazebit.persistence.view.metamodel.AttributeFilterMapping;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.reflection.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public abstract class AbstractMethodAttribute<X, Y> extends AbstractAttribute<X, Y> implements MethodAttribute<X, Y> {

    private final String name;
    private final boolean updatable;
    private final Method javaMethod;
    private final Map<String, AttributeFilterMapping> filterMappings;

    @SuppressWarnings("unchecked")
    protected AbstractMethodAttribute(ManagedViewTypeImpl<X> viewType, MethodAttributeMapping mapping, MetamodelBuildingContext context) {
        super(viewType, mapping, context);
        this.name = mapping.getAttributeName();
        this.javaMethod = mapping.getMethod();

        UpdatableMapping updatableMapping = AnnotationUtils.findAnnotation(javaMethod, UpdatableMapping.class);
        // TODO: maybe we should only consider abstract setters?
        boolean hasSetter = ReflectionUtils.getSetter(viewType.getJavaType(), name) != null;

        // TODO: this is not correct for collections, maybe collections should be mutable by default?
        // Btw. what shall we do if the attribute would be updatable but the viewType isn't? Since it could be a subview, we should keep the updatable state I think, 
        // but then I'd also need to create updatable proxy classes even if the entity view is not updatable by itself 
        if (updatableMapping == null) {
            this.updatable = hasSetter;
        } else {
            this.updatable = updatableMapping.updatable();
        }

        Map<String, AttributeFilterMapping> filterMappings = new HashMap<String, AttributeFilterMapping>();
        
        AttributeFilter filterMapping = AnnotationUtils.findAnnotation(javaMethod, AttributeFilter.class);
        AttributeFilters filtersMapping = AnnotationUtils.findAnnotation(javaMethod, AttributeFilters.class);
        
        if (filterMapping != null) {
            if (filtersMapping != null) {
                context.addError("Illegal occurrences of @Filter and @Filters on the " + mapping.getErrorLocation() + "!");
            } else {
                addFilterMapping(filterMapping, filterMappings, context);
            }
        } else if (filtersMapping != null) {
            for (AttributeFilter f : filtersMapping.value()) {
                addFilterMapping(f, filterMappings, context);
            }
        }

        this.filterMappings = Collections.unmodifiableMap(filterMappings);
    }

    @Override
    protected Class[] getTypeArguments() {
        return ReflectionUtils.getResolvedMethodReturnTypeArguments(getDeclaringType().getJavaType(), getJavaMethod());
    }

    protected static String getAttributeName(Method getterOrSetter) {
        String name = getterOrSetter.getName();
        StringBuilder sb = new StringBuilder(name.length());
        int index = name.startsWith("is") ? 2 : 3;
        char firstAttributeNameChar = name.charAt(index);
        return sb.append(Character.toLowerCase(firstAttributeNameChar))
                .append(name, index + 1, name.length())
                .toString();
    }

    private void addFilterMapping(AttributeFilter filterMapping, Map<String, AttributeFilterMapping> filterMappings, MetamodelBuildingContext context) {
        String filterName = filterMapping.name();
        boolean errorOccurred = false;
        
        if (filterMappings.containsKey(filterName)) {
            errorOccurred = true;
            context.addError("Illegal duplicate filter name mapping '" + filterName + "' at " + getLocation());
        }

        if (!errorOccurred) {
            AttributeFilterMapping attributeFilterMapping = new AttributeFilterMappingImpl(this, filterName, filterMapping.value());
            filterMappings.put(attributeFilterMapping.getName(), attributeFilterMapping);
        }
    }

    @Override
    protected String getLocation() {
        return MethodAttributeMapping.getLocation(getName(), getJavaMethod());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isUpdatable() {
        return updatable;
    }

    @Override
    public Method getJavaMethod() {
        return javaMethod;
    }

    @Override
    public MemberType getMemberType() {
        return MemberType.METHOD;
    }

    @Override
    public AttributeFilterMapping getFilter(String filterName) {
        return filterMappings.get(filterName);
    }

    @Override
    public Set<AttributeFilterMapping> getFilters() {
        return new SetView<AttributeFilterMapping>(filterMappings.values());
    }
    
    public Map<String, AttributeFilterMapping> getFilterMappings() {
        return filterMappings;
    }

    public static String extractAttributeName(Class<?> viewType, Method m, MetamodelBuildingContext context) {
        String attributeName;

        // We only support bean style getters
        if (ReflectionUtils.isSetter(m)) {
            attributeName = getAttributeName(m);
            Method getter = ReflectionUtils.getGetter(viewType, attributeName);

            if (getter == null) {
                context.addError("The setter '" + m.getName() + "' from the entity view '" + viewType.getName() + "' has no corresponding getter!");
                return null;
            }

            if (m.getParameterTypes()[0] != getter.getReturnType()) {
                context.addError("The setter '" + m.getName() + "' of the class '" + viewType.getName()
                    + "' must accept an argument of the same type as it's corresponding getter returns!");
                return null;
            }

            return null;
        } else if (!ReflectionUtils.isGetter(m)) {
            context.addError("The given method '" + m.getName() + "' from the entity view '" + viewType.getName()
                + "' is no bean style getter or setter!");
            return null;
        } else {
            attributeName = getAttributeName(m);
            Method setter = ReflectionUtils.getSetter(viewType, attributeName);

            if (setter != null && setter.getParameterTypes()[0] != m.getReturnType()) {
                context.addError("The getter '" + m.getName() + "' of the class '" + viewType.getName()
                    + "' must have the same return type as it's corresponding setter accepts!");
                return null;
            }
        }

        if (m.getExceptionTypes().length > 0) {
            context.addError("The given method '" + m.getName() + "' from the entity view '" + viewType.getName() + "' must not throw an exception!");
            return null;
        }

        return attributeName;
    }

    public static Annotation getMapping(String attributeName, Method m, MetamodelBuildingContext context) {
        Mapping mapping = AnnotationUtils.findAnnotation(m, Mapping.class);

        if (mapping == null) {
            IdMapping idMapping = AnnotationUtils.findAnnotation(m, IdMapping.class);

            if (idMapping != null) {
                if (idMapping.value().isEmpty()) {
                    idMapping = new IdMappingLiteral(getAttributeName(m));
                }

                return idMapping;
            }
            
            MappingParameter mappingParameter = AnnotationUtils.findAnnotation(m, MappingParameter.class);

            if (mappingParameter != null) {
                if (mappingParameter.value().isEmpty()) {
                    context.addError("Illegal empty mapping parameter for the " + MethodAttributeMapping.getLocation(attributeName, m));
                }

                return mappingParameter;
            }

            MappingSubquery mappingSubquery = AnnotationUtils.findAnnotation(m, MappingSubquery.class);

            if (mappingSubquery != null) {
                return mappingSubquery;
            }

            MappingCorrelated mappingCorrelated = AnnotationUtils.findAnnotation(m, MappingCorrelated.class);

            if (mappingCorrelated != null) {
                return mappingCorrelated;
            }

            MappingCorrelatedSimple mappingCorrelatedSimple = AnnotationUtils.findAnnotation(m, MappingCorrelatedSimple.class);

            if (mappingCorrelatedSimple != null) {
                return mappingCorrelatedSimple;
            }

            // Implicit mapping
            mapping = new MappingLiteral(getAttributeName(m));
        }

        if (mapping.value().isEmpty()) {
            mapping = new MappingLiteral(getAttributeName(m), mapping);
        }

        return mapping;
    }
}
