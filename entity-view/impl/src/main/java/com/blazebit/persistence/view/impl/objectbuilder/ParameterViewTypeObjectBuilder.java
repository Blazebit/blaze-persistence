/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
