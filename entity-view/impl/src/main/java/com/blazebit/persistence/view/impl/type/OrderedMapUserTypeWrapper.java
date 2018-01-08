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

package com.blazebit.persistence.view.impl.type;

import com.blazebit.persistence.view.spi.type.BasicUserType;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class OrderedMapUserTypeWrapper<K, V> extends AbstractMapUserTypeWrapper<Map<K, V>, K, V> {

    public OrderedMapUserTypeWrapper(BasicUserType<K> keyUserType, BasicUserType<V> elementUserType) {
        super(keyUserType, elementUserType);
    }

    @Override
    protected Map<K, V> createCollection(int size) {
        return new LinkedHashMap<>(size);
    }
}
