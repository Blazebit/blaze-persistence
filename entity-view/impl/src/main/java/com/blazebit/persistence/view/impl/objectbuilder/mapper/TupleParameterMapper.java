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

package com.blazebit.persistence.view.impl.objectbuilder.mapper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.blazebit.persistence.ParameterHolder;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class TupleParameterMapper {

    private final String[] parameterMappings;
    private final int[] parameterIndices;

    public TupleParameterMapper(List<String> fullParamMappings, int startIndex) {
        String[] paramMappings = new String[fullParamMappings.size()];
        int[] paramIndices = new int[fullParamMappings.size()];
        int size = 0;

        for (int i = 0; i < fullParamMappings.size(); i++) {
            if (fullParamMappings.get(i) != null) {
                paramMappings[size] = fullParamMappings.get(i);
                paramIndices[size] = i + startIndex;
                size++;
            }
        }

        this.parameterMappings = Arrays.copyOf(paramMappings, size);
        this.parameterIndices = Arrays.copyOf(paramIndices, size);
    }

    public void applyMapping(ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, Object[] tuple) {
        for (int i = 0; i < parameterMappings.length; i++) {
            if (parameterHolder.isParameterSet(parameterMappings[i])) {
                tuple[parameterIndices[i]] = parameterHolder.getParameterValue(parameterMappings[i]);
            } else {
                tuple[parameterIndices[i]] = optionalParameters.get(parameterMappings[i]);
            }
        }
    }
}
