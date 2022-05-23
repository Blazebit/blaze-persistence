/*
 * Copyright 2014 - 2022 Blazebit.
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

import com.blazebit.persistence.view.MapInstantiator;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface MapInstantiatorImplementor<C extends Map<?, ?>, R extends RecordingMap<C, ?, ?>> extends MapInstantiator<C, R> {

    /**
     * Creates a map for the JPA model.
     *
     * @param size The size estimate
     * @return the map
     */
    public Map<?, ?> createJpaMap(int size);

}
