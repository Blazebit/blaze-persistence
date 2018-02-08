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

import com.blazebit.persistence.view.InverseRemoveStrategy;
import com.blazebit.persistence.view.MappingCorrelated;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedParameterCollectionAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedParameterListAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedParameterSingularAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedParameterSetAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingParameterCollectionAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingParameterListAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingParameterMapAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingParameterSingularAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingParameterSetAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.SubqueryParameterSingularAttribute;
import com.blazebit.persistence.view.spi.EntityViewConstructorMapping;
import com.blazebit.persistence.view.spi.EntityViewMapping;
import com.blazebit.persistence.view.spi.EntityViewParameterMapping;

import javax.persistence.metamodel.ManagedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Collection;
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
 * @since 1.2.0
 */
public class ParameterAttributeMapping extends AttributeMapping implements EntityViewParameterMapping {

    private final ConstructorMapping constructor;
    private final int index;

    public ParameterAttributeMapping(ViewMapping viewMapping, Annotation mapping, MetamodelBootContext context, ConstructorMapping constructor, int index, boolean isCollection, Class<?> declaredTypeClass, Class<?> declaredKeyTypeClass, Class declaredElementTypeClass,
                                     Type type, Type keyType, Type elementType, Map<Class<?>, String> inheritanceSubtypeClassMappings, Map<Class<?>, String> keyInheritanceSubtypeClassMappings, Map<Class<?>, String> elementInheritanceSubtypeClassMappings) {
        super(viewMapping, mapping, context, isCollection, declaredTypeClass, declaredKeyTypeClass, declaredElementTypeClass, type, keyType, elementType, inheritanceSubtypeClassMappings, keyInheritanceSubtypeClassMappings, elementInheritanceSubtypeClassMappings);
        this.constructor = constructor;
        this.index = index;
    }

    @Override
    public EntityViewConstructorMapping getDeclaringConstructor() {
        return constructor;
    }

    @Override
    public EntityViewMapping getDeclaringView() {
        return constructor.getDeclaringView();
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public boolean isId() {
        return false;
    }

    @Override
    public boolean isVersion() {
        return false;
    }

    @Override
    public String getErrorLocation() {
        return getLocation(constructor.getConstructor(), index);
    }

    @Override
    public String getMappedBy() {
        return null;
    }

    @Override
    public String determineMappedBy(ManagedType<?> managedType, String mapping, MetamodelBuildingContext context) {
        return null;
    }

    @Override
    public Map<String, String> determineWritableMappedByMappings(ManagedType<?> managedType, String mappedBy, MetamodelBuildingContext context) {
        return null;
    }

    @Override
    public InverseRemoveStrategy getInverseRemoveStrategy() {
        return null;
    }

    public static String getLocation(Constructor<?> constructor, int index) {
        return "parameter at index " + index + " of constructor[" + constructor + "]";
    }

    // If you change something here don't forget to also update MethodAttributeMapping#getMethodAttribute
    @SuppressWarnings("unchecked")
    public <X> AbstractParameterAttribute<? super X, ?> getParameterAttribute(MappingConstructorImpl<X> constructor, MetamodelBuildingContext context) {
        if (attribute == null) {
            if (mapping instanceof MappingParameter) {
                attribute = new MappingParameterSingularAttribute<X, Object>(constructor, this, context);
                return (AbstractParameterAttribute<? super X, ?>) attribute;
            }

            boolean correlated = mapping instanceof MappingCorrelated || mapping instanceof MappingCorrelatedSimple;

            if (isCollection) {
                if (Collection.class == declaredTypeClass) {
                    if (correlated) {
                        attribute = new CorrelatedParameterCollectionAttribute<X, Object>(constructor, this, context);
                    } else {
                        attribute = new MappingParameterCollectionAttribute<X, Object>(constructor, this, context);
                    }
                } else if (List.class == declaredTypeClass) {
                    if (correlated) {
                        attribute = new CorrelatedParameterListAttribute<X, Object>(constructor, this, context);
                    } else {
                        attribute = new MappingParameterListAttribute<X, Object>(constructor, this, context);
                    }
                } else if (Set.class == declaredTypeClass || SortedSet.class == declaredTypeClass || NavigableSet.class == declaredTypeClass) {
                    if (correlated) {
                        attribute = new CorrelatedParameterSetAttribute<X, Object>(constructor, this, context);
                    } else {
                        attribute = new MappingParameterSetAttribute<X, Object>(constructor, this, context);
                    }
                } else if (Map.class == declaredTypeClass || SortedMap.class == declaredTypeClass || NavigableMap.class == declaredTypeClass) {
                    if (correlated) {
                        context.addError("Parameter with the index '" + index + "' of the constructor '" + constructor.getJavaConstructor() + "' uses a Map type with a correlated mapping which is unsupported!");
                    } else {
                        attribute = new MappingParameterMapAttribute<X, Object, Object>(constructor, this, context);
                    }
                } else {
                    context.addError("Parameter with the index '" + index + "' of the constructor '" + constructor.getJavaConstructor() + "' uses an unknown collection type: " + declaredTypeClass);
                }
            } else {
                if (mapping instanceof MappingSubquery) {
                    attribute = new SubqueryParameterSingularAttribute<X, Object>(constructor, this, context);
                } else if (correlated) {
                    attribute = new CorrelatedParameterSingularAttribute<X, Object>(constructor, this, context);
                } else {
                    attribute = new MappingParameterSingularAttribute<X, Object>(constructor, this, context);
                }
            }
        }

        return (AbstractParameterAttribute<? super X, ?>) attribute;
    }

}
