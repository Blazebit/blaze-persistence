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

package com.blazebit.persistence.view.impl.collection;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapPutAllAction<C extends Map<K, V>, K, V> implements MapAction<C> {

    private final Map<? extends K, ? extends V> elements;
    
    public MapPutAllAction(Map<? extends K, ? extends V> map) {
        this.elements = new LinkedHashMap<K, V>(map);
    }

    @Override
    public void doAction(C map) {
        map.putAll(elements);
    }

}
