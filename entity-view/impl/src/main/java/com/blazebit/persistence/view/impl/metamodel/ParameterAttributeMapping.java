/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.view.InverseRemoveStrategy;
import com.blazebit.persistence.view.MappingCorrelated;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.MappingIndex;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.Self;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedParameterCollectionAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedParameterListAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedParameterMapAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedParameterSingularAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.CorrelatedParameterSetAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingParameterCollectionAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingParameterListAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingParameterMapAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingParameterSingularAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.MappingParameterSetAttribute;
import com.blazebit.persistence.view.impl.metamodel.attribute.SubqueryParameterSingularAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.persistence.view.spi.EntityViewConstructorMapping;
import com.blazebit.persistence.view.spi.EntityViewMapping;
import com.blazebit.persistence.view.spi.EntityViewParameterMapping;

import javax.persistence.metamodel.Attribute;
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

    public ParameterAttributeMapping(ViewMapping viewMapping, Annotation mapping, MappingIndex mappingIndex, MetamodelBootContext context, ConstructorMapping constructor, int index, boolean isCollection, PluralAttribute.ElementCollectionType elementCollectionType, Class<?> declaredTypeClass, Class<?> declaredKeyTypeClass, Class declaredElementTypeClass,
                                     Type type, Type keyType, Type elementType, Map<Class<?>, String> inheritanceSubtypeClassMappings, Map<Class<?>, String> keyInheritanceSubtypeClassMappings, Map<Class<?>, String> elementInheritanceSubtypeClassMappings) {
        super(viewMapping, mapping, mappingIndex, context, isCollection, elementCollectionType, declaredTypeClass, declaredKeyTypeClass, declaredElementTypeClass, type, keyType, elementType, inheritanceSubtypeClassMappings, keyInheritanceSubtypeClassMappings, elementInheritanceSubtypeClassMappings);
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
    public boolean determineDisallowOwnedUpdatableSubview(MetamodelBuildingContext context, EmbeddableOwner embeddableMapping, Attribute<?, ?> updateMappableAttribute) {
        return false;
    }

    @Override
    public String determineMappedBy(ManagedType<?> managedType, String mapping, MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
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
    public <X> AbstractParameterAttribute<? super X, ?> getParameterAttribute(MappingConstructorImpl<X> constructor, MetamodelBuildingContext context, EmbeddableOwner embeddableMapping) {
        if (attribute == null) {
            if (mapping instanceof MappingParameter) {
                attribute = new MappingParameterSingularAttribute<X, Object>(constructor, this, context, embeddableMapping);
                return (AbstractParameterAttribute<? super X, ?>) attribute;
            } else if (mapping instanceof Self) {
                attribute = new MappingParameterSingularAttribute<X, Object>(constructor, this, context, embeddableMapping);
                return (AbstractParameterAttribute<? super X, ?>) attribute;
            }

            boolean correlated = mapping instanceof MappingCorrelated || mapping instanceof MappingCorrelatedSimple;

            if (isCollection) {
                if (Collection.class == declaredTypeClass) {
                    if (correlated) {
                        attribute = new CorrelatedParameterCollectionAttribute<X, Object>(constructor, this, context, embeddableMapping);
                    } else {
                        attribute = new MappingParameterCollectionAttribute<X, Object>(constructor, this, context, embeddableMapping);
                    }
                } else if (List.class == declaredTypeClass) {
                    if (correlated) {
                        attribute = new CorrelatedParameterListAttribute<X, Object>(constructor, this, context, embeddableMapping);
                    } else {
                        attribute = new MappingParameterListAttribute<X, Object>(constructor, this, context, embeddableMapping);
                    }
                } else if (Set.class == declaredTypeClass || SortedSet.class == declaredTypeClass || NavigableSet.class == declaredTypeClass) {
                    if (correlated) {
                        attribute = new CorrelatedParameterSetAttribute<X, Object>(constructor, this, context, embeddableMapping);
                    } else {
                        attribute = new MappingParameterSetAttribute<X, Object>(constructor, this, context, embeddableMapping);
                    }
                } else if (Map.class == declaredTypeClass || SortedMap.class == declaredTypeClass || NavigableMap.class == declaredTypeClass) {
                    if (correlated) {
                        attribute = new CorrelatedParameterMapAttribute<X, Object, Object>(constructor, this, context, embeddableMapping);
                    } else {
                        attribute = new MappingParameterMapAttribute<X, Object, Object>(constructor, this, context, embeddableMapping);
                    }
                } else {
                    context.addError("Parameter with the index '" + index + "' of the constructor '" + constructor.getJavaConstructor() + "' uses an unknown collection type: " + declaredTypeClass);
                }
            } else {
                if (mapping instanceof MappingSubquery) {
                    attribute = new SubqueryParameterSingularAttribute<X, Object>(constructor, this, context);
                } else if (correlated) {
                    attribute = new CorrelatedParameterSingularAttribute<X, Object>(constructor, this, context, embeddableMapping);
                } else {
                    attribute = new MappingParameterSingularAttribute<X, Object>(constructor, this, context, embeddableMapping);
                }
            }
        }

        return (AbstractParameterAttribute<? super X, ?>) attribute;
    }

}
