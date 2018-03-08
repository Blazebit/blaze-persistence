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

package com.blazebit.persistence.view.impl.objectbuilder;

import java.util.Map;

import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.view.impl.objectbuilder.mapper.TupleParameterMapper;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ParameterViewTypeObjectBuilder<T> extends DelegatingObjectBuilder<T> {

    private final TupleParameterMapper parameterMapper;
    private final ParameterHolder<?> parameterHolder;
    private final Map<String, Object> optionalParameters;

    public ParameterViewTypeObjectBuilder(ObjectBuilder<T> delegate, ViewTypeObjectBuilderTemplate<T> template, ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, int startIndex) {
        super(delegate);

        if (!template.hasParameters()) {
            throw new IllegalArgumentException("No templates without parameters allowed for this object builder!");
        }

        this.parameterMapper = template.getParameterMapper();
        this.parameterHolder = parameterHolder;
        this.optionalParameters = optionalParameters;
    }

    @Override
    public T build(Object[] tuple) {
        parameterMapper.applyMapping(parameterHolder, optionalParameters, tuple);
        return super.build(tuple);
    }
}
