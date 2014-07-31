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
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.MethodAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.reflection.ReflectionUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author cpbec
 */
public class ViewTypeImpl<X> implements ViewType<X> {

    private final Class<X> javaType;
    private final String name;
    private final Class<?> entityClass;
    private final Map<String, MethodAttribute<? super X, ?>> attributes;
    private final Map<ParametersKey, MappingConstructor<X>> constructors;
    private final Map<String, MappingConstructor<X>> constructorIndex;
    
    public ViewTypeImpl(Class<? extends X> clazz) {
        this.javaType = (Class<X>) clazz;
        
        if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
            throw new IllegalArgumentException("Only interfaces or abstract classes are allowed as entity views. '" + clazz.getName() + "' is neither of those.");
        }
        
        EntityView entityViewAnnot = AnnotationUtils.findAnnotation(clazz, EntityView.class);
        
        if (entityViewAnnot == null) {
            throw new IllegalArgumentException("Could not find any EntityView annotation for the class '" + clazz.getName() + "'");
        }
        
        if (entityViewAnnot.name().isEmpty()) {
            this.name = clazz.getSimpleName();
        } else {
            this.name = entityViewAnnot.name();
        }
        
        this.entityClass = entityViewAnnot.value();
        // We use a tree map to get a deterministic attribute order
        this.attributes = new TreeMap<String, MethodAttribute<? super X, ?>>();
        
        for (Class<?> type : ReflectionUtils.getSuperTypes(clazz)) {
            for (Method method : type.getDeclaredMethods()) {
                String attributeName = AbstractMethodAttribute.validate(this, method);
                
                if (attributeName != null && !attributes.containsKey(attributeName)) {
                    MethodAttribute<? super X, ?> attribute = createMethodAttribute(this, method);
                    if (attribute != null) {
                        attributes.put(attribute.getName(), attribute);
                    }
                }
            }
        }
        
        this.constructors = new HashMap<ParametersKey, MappingConstructor<X>>();
        this.constructorIndex = new HashMap<String, MappingConstructor<X>>();
        
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            String constructorName = MappingConstructorImpl.validate(this, constructor);
            if (constructorIndex.containsKey(constructorName)) {
                constructorName += constructorIndex.size();
            }
            MappingConstructor<X> mappingConstructor = new MappingConstructorImpl<X>(this, constructorName, (Constructor<X>) constructor);
            constructors.put(new ParametersKey(constructor.getParameterTypes()), mappingConstructor);
            constructorIndex.put(constructorName, mappingConstructor);
        }
    }
    
    private static <X> MethodAttribute<? super X, ?> createMethodAttribute(ViewType<X> viewType, Method method) {
        Annotation mapping = AbstractMethodAttribute.getMapping(viewType, method);
        if (mapping == null) {
            return null;
        }
        
        Class<?> attributeType = method.getReturnType();
        
        if (Collection.class == attributeType) {
            return new MethodMappingCollectionAttributeImpl<X, Object>(viewType, method, mapping);
        } else if (List.class == attributeType) {
            return new MethodMappingListAttributeImpl<X, Object>(viewType, method, mapping);
        } else if (Set.class == attributeType) {
            return new MethodMappingSetAttributeImpl<X, Object>(viewType, method, mapping);
        } else if (Map.class == attributeType) {
            return new MethodMappingMapAttributeImpl<X, Object, Object>(viewType, method, mapping);
        } else if (mapping instanceof MappingSubquery) {
            return new MethodSubquerySingularAttributeImpl<X, Object>(viewType, method, mapping);
        } else {
            return new MethodMappingSingularAttributeImpl<X, Object>(viewType, method, mapping);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<X> getJavaType() {
        return javaType;
    }

    @Override
    public Class<?> getEntityClass() {
        return entityClass;
    }
    
    @Override
    public Set<MethodAttribute<? super X, ?>> getAttributes() {
        return new LinkedHashSet<MethodAttribute<? super X, ?>>(attributes.values());
    }

    @Override
    public MethodAttribute<? super X, ?> getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Set<MappingConstructor<X>> getConstructors() {
        return new HashSet<MappingConstructor<X>>(constructors.values());
    }
    
    @Override
    public MappingConstructor<X> getConstructor(Class<?>... parameterTypes) {
        return constructors.get(new ParametersKey(parameterTypes));
    }
    
    @Override
    public Set<String> getConstructorNames() {
        return constructorIndex.keySet();
    }
    
    
    @Override
    public MappingConstructor<X> getConstructor(String name) {
        return constructorIndex.get(name);
    }
    
    private static class ParametersKey {
        private final Class<?>[] parameterTypes;

        public ParametersKey(Class<?>[] parameterTypes) {
            this.parameterTypes = parameterTypes;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + Arrays.deepHashCode(this.parameterTypes);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ParametersKey other = (ParametersKey) obj;
            if (!Arrays.deepEquals(this.parameterTypes, other.parameterTypes)) {
                return false;
            }
            return true;
        }
    }
    
}
