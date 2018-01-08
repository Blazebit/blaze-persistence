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

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.view.impl.entity.MapViewToEntityMapper;
import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SimpleMapViewToEntityMapper implements MapViewToEntityMapper {

    private final ViewToEntityMapper keyMapper;
    private final ViewToEntityMapper valueMapper;

    public SimpleMapViewToEntityMapper(ViewToEntityMapper keyMapper, ViewToEntityMapper valueMapper) {
        this.keyMapper = keyMapper;
        this.valueMapper = valueMapper;
    }

    @Override
    public ViewToEntityMapper getKeyMapper() {
        return keyMapper;
    }

    @Override
    public ViewToEntityMapper getValueMapper() {
        return valueMapper;
    }
}
