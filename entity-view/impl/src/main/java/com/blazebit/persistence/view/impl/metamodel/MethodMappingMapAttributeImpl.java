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

import com.blazebit.persistence.view.metamodel.MapAttribute;
import com.blazebit.persistence.view.metamodel.ViewType;
import com.blazebit.reflection.ReflectionUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class MethodMappingMapAttributeImpl<X, K, V> extends AbstractMethodMappingPluralAttribute<X, Map<K, V>, V> implements MapAttribute<X, K, V> {

    private final Class<K> keyType;

    public MethodMappingMapAttributeImpl(ViewType<X> viewType, Method method, Annotation mapping, Set<Class<?>> entityViews) {
        super(viewType, method, mapping, entityViews, MetamodelUtils.isSorted(viewType.getJavaType(), method));
        Class<?>[] typeArguments = ReflectionUtils.getResolvedMethodReturnTypeArguments(viewType.getJavaType(), method);
        this.keyType = (Class<K>) typeArguments[0];
        if (keyType == null) {
            throw new IllegalArgumentException("The key type is not resolvable " + "for the attribute '" + getName() + "' of the class '" + viewType.getJavaType().getName() + "'!");
        }
        if (isIgnoreIndex()) {
            throw new IllegalArgumentException("Illegal ignoreIndex mapping for the attribute '" + getName() + "' of the class '" + viewType.getJavaType().getName() + "'!");
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
