/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.transformer;

import java.util.Map;

import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface TupleTransformerFactory {

    public int getConsumeStartIndex();

    public int getConsumeEndIndex();

    public TupleTransformer create(ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, EntityViewConfiguration entityViewConfiguration);
}
