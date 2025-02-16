/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.mapper;

import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.SelectBuilder;
import com.blazebit.persistence.view.spi.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.spi.ViewJpqlMacro;
import com.blazebit.persistence.view.spi.type.BasicUserTypeStringSupport;

import java.util.Map;
import java.util.NavigableSet;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface TupleElementMapper {

    public String getAttributePath();

    public BasicUserTypeStringSupport<Object> getBasicTypeStringSupport();

    public void applyMapping(SelectBuilder<?> queryBuilder, ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro,
                             NavigableSet<String> fetches, boolean asString);
}
