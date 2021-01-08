/*
 * Copyright 2014 - 2021 Blazebit.
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

package com.blazebit.persistence.view.impl.objectbuilder;

import com.blazebit.persistence.view.impl.collection.MapInstantiatorImplementor;
import com.blazebit.persistence.view.impl.collection.RecordingMap;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class MapInstantiatorAccumulator implements ContainerAccumulator<Map<?, ?>>  {

    private final MapInstantiatorImplementor<?, ?> mapInstantiator;
    private final ContainerAccumulator<Object> valueAccumulator;
    private final boolean filterNulls;

    public MapInstantiatorAccumulator(MapInstantiatorImplementor<?, ?> mapInstantiator, ContainerAccumulator<?> valueAccumulator, boolean filterNulls) {
        this.mapInstantiator = mapInstantiator;
        this.valueAccumulator = (ContainerAccumulator<Object>) valueAccumulator;
        this.filterNulls = filterNulls;
    }

    public MapInstantiatorImplementor<?, ?> getMapInstantiator() {
        return mapInstantiator;
    }

    public ContainerAccumulator<Object> getValueAccumulator() {
        return valueAccumulator;
    }

    @Override
    public Map<?, ?> createContainer(boolean recording, int size) {
        if (recording) {
            return mapInstantiator.createRecordingMap(size);
        } else {
            return mapInstantiator.createMap(size);
        }
    }

    @Override
    public void add(Map<?, ?> container, Object index, Object value, boolean recording) {
        if (filterNulls && value == null) {
            return;
        }
        final Map<Object, Object> map;
        if (recording) {
            map = ((RecordingMap<Map<Object, Object>, ?, ?>) container).getDelegate();
        } else {
            map = (Map<Object, Object>) container;
        }
        if (valueAccumulator == null) {
            Object oldValue = map.put(index, value);
            if (oldValue != null && !oldValue.equals(value)) {
                throw new IllegalArgumentException("Value " + value + " replaces old value " + oldValue + " for key " + index + "! Use a proper accumulator!");
            }
        } else {
            Object valueContainer = map.get(index);
            if (valueContainer == null) {
                valueContainer = valueAccumulator.createContainer(false, 1);
                map.put(index, valueContainer);
            }
            valueAccumulator.add(valueContainer, null, value, false);
        }
    }

    @Override
    public void addAll(Map<?, ?> container, Map<?, ?> value, boolean recording) {
        final Map<Object, Object> map;
        if (recording) {
            map = ((RecordingMap<Map<Object, Object>, ?, ?>) container).getDelegate();
        } else {
            map = (Map<Object, Object>) container;
        }
        if (valueAccumulator == null) {
            if (filterNulls) {
                for (Map.Entry<?, ?> entry : value.entrySet()) {
                    if (entry.getValue() != null) {
                        map.put(entry.getKey(), entry.getValue());
                    }
                }
            } else {
                map.putAll(value);
            }
        } else {
            for (Map.Entry<?, ?> entry : value.entrySet()) {
                Object valueContainer = map.get(entry.getKey());
                if (valueContainer == null) {
                    valueContainer = valueAccumulator.createContainer(false, 1);
                    map.put(entry.getKey(), valueContainer);
                }
                valueAccumulator.addAll(valueContainer, entry.getValue(), false);
            }
        }
    }

    @Override
    public boolean requiresPostConstruct() {
        return false;
    }

    @Override
    public void postConstruct(Map<?, ?> collection) {
    }
}
