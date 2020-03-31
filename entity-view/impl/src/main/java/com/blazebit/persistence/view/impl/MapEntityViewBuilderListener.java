/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.view.impl;

import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class MapEntityViewBuilderListener implements EntityViewBuilderListener {

    private final Map<Object, Object> map;
    private final Object key;

    public MapEntityViewBuilderListener(Map<Object, Object> map, Object key) {
        this.map = map;
        this.key = key;
    }

    @Override
    public void onBuildComplete(Object object) {
        map.put(key, object);
    }
}
