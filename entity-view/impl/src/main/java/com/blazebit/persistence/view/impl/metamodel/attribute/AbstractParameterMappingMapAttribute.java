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

package com.blazebit.persistence.view.impl.metamodel.attribute;

import com.blazebit.persistence.view.impl.metamodel.AbstractParameterMappingPluralAttribute;
import com.blazebit.persistence.view.impl.metamodel.MetamodelUtils;
import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.MappingConstructor;
import com.blazebit.reflection.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractParameterMappingMapAttribute<X, K, V> extends AbstractParameterMappingPluralAttribute<X, Map<K, V>, V> implements MapAttribute<X, K, V> {

    private final Class<K> keyType;

    @SuppressWarnings("unchecked")
    public AbstractParameterMappingMapAttribute(MappingConstructor<X> mappingConstructor, int index, Annotation mapping, Set<Class<?>> entityViews, Set<String> errors) {
        super(mappingConstructor, index, mapping, entityViews, MetamodelUtils.isSorted(mappingConstructor, index), errors);
        Type parameterType = mappingConstructor.getJavaConstructor().getGenericParameterTypes()[index];
        Class<?>[] typeArguments = ReflectionUtils.resolveTypeArguments(mappingConstructor.getDeclaringType().getJavaType(), parameterType);
        this.keyType = (Class<K>) typeArguments[0];
        if (isIgnoreIndex()) {
            errors.add("Illegal ignoreIndex mapping for the parameter of the constructor '" + mappingConstructor.getJavaConstructor().toString()
                + "' at the index '" + index + "'!");
        }
    }

    @Override
    public Class<K> getKeyType() {
        return keyType;
    }

    @Override
    public CollectionType getCollectionType() {
        return CollectionType.MAP;
    }

    @Override
    public boolean isIndexed() {
        return true;
    }

}
