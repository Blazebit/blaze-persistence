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

import com.blazebit.persistence.view.MappingCorrelated;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.MappingSingular;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.ViewConstructor;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedParameterCollectionAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedParameterListAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedParameterMappingSingularAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedParameterSetAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingParameterCollectionAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingParameterListAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingParameterMapAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingParameterMappingSingularAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingParameterSetAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.SubqueryParameterSingularAttribute;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;
import com.blazebit.reflection.ReflectionUtils;

import javax.persistence.metamodel.ManagedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

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

    public MappingConstructorImpl(ManagedViewType<X> viewType, String name, Constructor<X> constructor, MetamodelBuildingContext context) {
        this.name = name;
        this.declaringType = viewType;
        this.javaConstructor = constructor;

        if (constructor.getExceptionTypes().length != 0) {
            context.addError("The constructor '" + constructor.toString() + "' of the class '" + constructor.getDeclaringClass().getName()
                + "' may not throw an exception!");
        }
        
        int parameterCount = constructor.getParameterTypes().length;
        List<AbstractParameterAttribute<? super X, ?>> parameters = new ArrayList<AbstractParameterAttribute<? super X, ?>>(parameterCount);
        for (int i = 0; i < parameterCount; i++) {
            AbstractParameterAttribute.validate(this, i, context);
            AbstractParameterAttribute<? super X, ?> parameter = createParameterAttribute(this, i, context);
            if (parameter != null) {
                parameters.add(parameter);
            }
        }

        this.parameters = Collections.unmodifiableList(parameters);
    }

    public void checkParameterCorrelationUsage(Map<Class<?>, ManagedViewTypeImpl<?>> managedViews, HashSet<ManagedViewType<?>> seenViewTypes, HashSet<MappingConstructor<?>> seenConstructors, MetamodelBuildingContext context) {
        if (seenConstructors.contains(this)) {
            return;
        }

        seenConstructors.add(this);
        for (AbstractParameterAttribute<? super X, ?> parameter : parameters) {
            parameter.checkAttributeCorrelationUsage(managedViews, seenViewTypes, seenConstructors, context);
        }
    }
    
    public void checkParameters(ManagedType<?> managedType, Map<Class<?>, ManagedViewTypeImpl<?>> managedViews, Map<String, List<String>> collectionMappings, MetamodelBuildingContext context) {
        for (AbstractParameterAttribute<? super X, ?> parameter : parameters) {
            parameter.checkAttribute(managedType, managedViews, context);

            for (String mapping : parameter.getCollectionJoinMappings(managedType, context)) {
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
    private static <X> AbstractParameterAttribute<? super X, ?> createParameterAttribute(MappingConstructor<X> constructor, int index, MetamodelBuildingContext context) {
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

        boolean correlated = mapping instanceof MappingCorrelated || mapping instanceof MappingCorrelatedSimple;
        
        if (mapping instanceof MappingParameter) {
            return new MappingParameterMappingSingularAttribute<X, Object>(constructor, index, mapping, context);
        }
        
        Annotation[] annotations = constructor.getJavaConstructor().getParameterAnnotations()[index];
        
        for (Annotation a : annotations) {
            // Force singular mapping
            if (MappingSingular.class == a.annotationType()) {
                if (correlated) {
                    return new CorrelatedParameterMappingSingularAttribute<X, Object>(constructor, index, mapping, context);
                } else {
                    return new MappingParameterMappingSingularAttribute<X, Object>(constructor, index, mapping, context);
                }
            }
        }

        if (Collection.class == attributeType) {
            if (correlated) {
                return new CorrelatedParameterCollectionAttribute<X, Object>(constructor, index, mapping, context);
            } else {
                return new MappingParameterCollectionAttribute<X, Object>(constructor, index, mapping, context);
            }
        } else if (List.class == attributeType) {
            if (correlated) {
                return new CorrelatedParameterListAttribute<X, Object>(constructor, index, mapping, context);
            } else {
                return new MappingParameterListAttribute<X, Object>(constructor, index, mapping, context);
            }
        } else if (Set.class == attributeType || SortedSet.class == attributeType || NavigableSet.class == attributeType) {
            if (correlated) {
                return new CorrelatedParameterSetAttribute<X, Object>(constructor, index, mapping, context);
            } else {
                return new MappingParameterSetAttribute<X, Object>(constructor, index, mapping, context);
            }
        } else if (Map.class == attributeType || SortedMap.class == attributeType || NavigableMap.class == attributeType) {
            if (correlated) {
                context.addError("Parameter with the index '" + index + "' of the constructor '" + constructor.getJavaConstructor() + "' uses a Map type with a correlated mapping which is unsupported!");
                return null;
            } else {
                return new MappingParameterMapAttribute<X, Object, Object>(constructor, index, mapping, context);
            }
        } else if (mapping instanceof MappingSubquery) {
            return new SubqueryParameterSingularAttribute<X, Object>(constructor, index, mapping, context);
        } else {
            if (correlated) {
                return new CorrelatedParameterMappingSingularAttribute<X, Object>(constructor, index, mapping, context);
            } else {
                return new MappingParameterMappingSingularAttribute<X, Object>(constructor, index, mapping, context);
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
