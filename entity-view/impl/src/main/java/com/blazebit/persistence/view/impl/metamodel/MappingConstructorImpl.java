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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;

import javax.persistence.metamodel.ManagedType;

import com.blazebit.persistence.impl.EntityMetamodel;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.view.MappingCorrelated;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.MappingSingular;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.ViewConstructor;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedParameterMappingCollectionAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedParameterMappingListAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedParameterMappingSetAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedParameterMappingSingularAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.DefaultParameterMappingCollectionAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.DefaultParameterMappingListAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.DefaultParameterMappingMapAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.DefaultParameterMappingSetAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.DefaultParameterMappingSingularAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.DefaultParameterSubquerySingularAttribute;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;
import com.blazebit.reflection.ReflectionUtils;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class MappingConstructorImpl<X> implements MappingConstructor<X> {

    private final String name;
    private final ManagedViewType<X> declaringType;
    private final Constructor<X> javaConstructor;
    private final List<AbstractParameterAttribute<? super X, ?>> parameters;

    public MappingConstructorImpl(ManagedViewType<X> viewType, String name, Constructor<X> constructor, Set<Class<?>> entityViews, EntityMetamodel metamodel, ExpressionFactory expressionFactory, Set<String> errors) {
        this.name = name;
        this.declaringType = viewType;
        this.javaConstructor = constructor;

        if (constructor.getExceptionTypes().length != 0) {
            errors.add("The constructor '" + constructor.toString() + "' of the class '" + constructor.getDeclaringClass().getName()
                + "' may not throw an exception!");
        }
        
        int parameterCount = constructor.getParameterTypes().length;
        List<AbstractParameterAttribute<? super X, ?>> parameters = new ArrayList<AbstractParameterAttribute<? super X, ?>>(parameterCount);
        for (int i = 0; i < parameterCount; i++) {
            AbstractParameterAttribute.validate(this, i, errors);
            AbstractParameterAttribute<? super X, ?> parameter = createParameterAttribute(this, i, entityViews, metamodel, expressionFactory, errors);
            if (parameter != null) {
                parameters.add(parameter);
            }
        }

        this.parameters = Collections.unmodifiableList(parameters);
    }

    public void checkParameterCorrelationUsage(Collection<String> errors, HashMap<Class<?>, String> seenCorrelationProviders, Map<Class<?>, ManagedViewTypeImpl<?>> managedViews, HashSet<ManagedViewType<?>> seenViewTypes, HashSet<MappingConstructor<?>> seenConstructors) {
        if (seenConstructors.contains(this)) {
            return;
        }

        seenConstructors.add(this);
        for (AbstractParameterAttribute<? super X, ?> parameter : parameters) {
            parameter.checkAttributeCorrelationUsage(errors, seenCorrelationProviders, managedViews, seenViewTypes, seenConstructors);
        }
    }
    
    public void checkParameters(ManagedType<?> managedType, Map<Class<?>, ManagedViewTypeImpl<?>> managedViews, ExpressionFactory expressionFactory, EntityMetamodel metamodel, Map<String, List<String>> collectionMappings, Set<String> errors) {
        for (AbstractParameterAttribute<? super X, ?> parameter : parameters) {
            errors.addAll(parameter.checkAttribute(managedType, managedViews, expressionFactory, metamodel));

            for (String mapping : parameter.getCollectionJoinMappings(managedType, metamodel, expressionFactory)) {
                List<String> locations = collectionMappings.get(mapping);
                if (locations == null) {
                    locations = new ArrayList<String>(2);
                    collectionMappings.put(mapping, locations);
                }
                
                locations.add("Parameter with the index '" + parameter.getIndex() + "' of the constructor '" + parameter.getDeclaringConstructor().getJavaConstructor() + "'");
            }
        }
    }

    public static String extractConstructorName(ManagedViewType<?> viewType, Constructor<?> c) {
        ViewConstructor viewConstructor = c.getAnnotation(ViewConstructor.class);

        if (viewConstructor == null) {
            return "init";
        }

        return viewConstructor.value();
    }

    // If you change something here don't forget to also update ViewTypeImpl#createMethodAttribute
    private static <X> AbstractParameterAttribute<? super X, ?> createParameterAttribute(MappingConstructor<X> constructor, int index, Set<Class<?>> entityViews, EntityMetamodel metamodel, ExpressionFactory expressionFactory, Set<String> errors) {
        Annotation mapping = AbstractParameterAttribute.getMapping(constructor, index);
        if (mapping == null) {
            return null;
        }

        Type parameterType = constructor.getJavaConstructor().getGenericParameterTypes()[index];
        Class<?> attributeType;
        
        if (parameterType instanceof TypeVariable<?>) {
            attributeType = ReflectionUtils.resolveTypeVariable(constructor.getDeclaringType().getJavaType(), (TypeVariable<?>) parameterType);
        } else {
            attributeType = constructor.getJavaConstructor().getParameterTypes()[index];
        }
        
        if (mapping instanceof MappingParameter) {
            return new CorrelatedParameterMappingSingularAttribute<X, Object>(constructor, index, mapping, entityViews, errors);
        }
        
        Annotation[] annotations = constructor.getJavaConstructor().getParameterAnnotations()[index];
        
        for (Annotation a : annotations) {
            // Force singular mapping
            if (MappingSingular.class == a.annotationType()) {
                if (mapping instanceof MappingCorrelated) {
                    return new CorrelatedParameterMappingSingularAttribute<X, Object>(constructor, index, mapping, entityViews, errors);
                } else {
                    return new DefaultParameterMappingSingularAttribute<X, Object>(constructor, index, mapping, entityViews, errors);
                }
            }
        }

        if (Collection.class == attributeType) {
            if (mapping instanceof MappingCorrelated) {
                return new CorrelatedParameterMappingCollectionAttribute<X, Object>(constructor, index, mapping, entityViews, errors);
            } else {
                return new DefaultParameterMappingCollectionAttribute<X, Object>(constructor, index, mapping, entityViews, errors);
            }
        } else if (List.class == attributeType) {
            if (mapping instanceof MappingCorrelated) {
                return new CorrelatedParameterMappingListAttribute<X, Object>(constructor, index, mapping, entityViews, metamodel, expressionFactory, errors);
            } else {
                return new DefaultParameterMappingListAttribute<X, Object>(constructor, index, mapping, entityViews, metamodel, expressionFactory, errors);
            }
        } else if (Set.class == attributeType || SortedSet.class == attributeType || NavigableSet.class == attributeType) {
            if (mapping instanceof MappingCorrelated) {
                return new CorrelatedParameterMappingSetAttribute<X, Object>(constructor, index, mapping, entityViews, errors);
            } else {
                return new DefaultParameterMappingSetAttribute<X, Object>(constructor, index, mapping, entityViews, errors);
            }
        } else if (Map.class == attributeType || SortedMap.class == attributeType || NavigableMap.class == attributeType) {
            if (mapping instanceof MappingCorrelated) {
                errors.add("Parameter with the index '" + index + "' of the constructor '" + constructor.getJavaConstructor() + "' uses a Map type with a correlated mapping which is unsupported!");
                return null;
            } else {
                return new DefaultParameterMappingMapAttribute<X, Object, Object>(constructor, index, mapping, entityViews, errors);
            }
        } else if (mapping instanceof MappingSubquery) {
            return new DefaultParameterSubquerySingularAttribute<X, Object>(constructor, index, mapping, entityViews, errors);
        } else {
            if (mapping instanceof MappingCorrelated) {
                return new CorrelatedParameterMappingSingularAttribute<X, Object>(constructor, index, mapping, entityViews, errors);
            } else {
                return new DefaultParameterMappingSingularAttribute<X, Object>(constructor, index, mapping, entityViews, errors);
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ManagedViewType<X> getDeclaringType() {
        return declaringType;
    }

    @Override
    public Constructor<X> getJavaConstructor() {
        return javaConstructor;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ParameterAttribute<? super X, ?>> getParameterAttributes() {
        return (List<ParameterAttribute<? super X, ?>>) (List<?>) parameters;
    }

    @Override
    public ParameterAttribute<? super X, ?> getParameterAttribute(int index) {
        return parameters.get(index);
    }

}
