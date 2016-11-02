/*
 * Copyright 2014 - 2016 Blazebit.
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
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.Set;

import com.blazebit.persistence.view.CollectionMapping;
import com.blazebit.persistence.view.SubqueryProvider;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.persistence.view.metamodel.ParameterAttribute;
import com.blazebit.persistence.view.metamodel.PluralAttribute;
import com.blazebit.reflection.ReflectionUtils;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public abstract class AbstractParameterMappingPluralAttribute<X, C, Y> extends AbstractParameterAttribute<X, C> implements PluralAttribute<X, C, Y>, ParameterAttribute<X, C> {

    private final Class<Y> elementType;
    private final boolean subview;
    private final boolean sorted;
    private final boolean ordered;
    private final boolean ignoreIndex;
    private final Class<Comparator<Y>> comparatorClass;
    private final Comparator<Y> comparator;

    @SuppressWarnings("unchecked")
    public AbstractParameterMappingPluralAttribute(MappingConstructor<X> mappingConstructor, int index, Annotation mapping, Set<Class<?>> entityViews, boolean sorted) {
        super(mappingConstructor, index, mapping, entityViews);
        Type parameterType = mappingConstructor.getJavaConstructor().getGenericParameterTypes()[index];
        Class<?>[] typeArguments = ReflectionUtils.resolveTypeArguments(mappingConstructor.getDeclaringType().getJavaType(), parameterType);
        this.elementType = (Class<Y>) typeArguments[typeArguments.length - 1];
        this.subview = entityViews.contains(elementType);
        this.sorted = sorted;
        
        CollectionMapping collectionMapping = MetamodelUtils.getCollectionMapping(mappingConstructor, index);
        this.ordered = collectionMapping.ordered();
        this.ignoreIndex = collectionMapping.ignoreIndex();
        this.comparatorClass = MetamodelUtils.getComparatorClass(collectionMapping);
        this.comparator = MetamodelUtils.getComparator(comparatorClass);
    }

    @Override
    public Class<Y> getElementType() {
        return elementType;
    }

    @Override
    public boolean isSubquery() {
        return false;
    }

    @Override
    public boolean isCollection() {
        return true;
    }

    @Override
    public boolean isSubview() {
        return subview;
    }

    @Override
    public Class<? extends SubqueryProvider> getSubqueryProvider() {
        throw new IllegalStateException("This method should not be accessible!");
    }

    @Override
    public boolean isQueryParameter() {
        return false;
    }

    @Override
    public boolean isSorted() {
        return sorted;
    }

    @Override
    public boolean isOrdered() {
        return ordered;
    }
    
    protected boolean isIgnoreIndex() {
        return ignoreIndex;
    }

    @Override
    public Class<Comparator<Y>> getComparatorClass() {
        return comparatorClass;
    }

    @Override
    public Comparator<Y> getComparator() {
        return comparator;
    }
}
