/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder;

import com.blazebit.persistence.CTEBuilder;
import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.view.CTEProvider;
import com.blazebit.persistence.view.spi.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.spi.ViewJpqlMacro;

import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.6.0
 */
public class CteProviderSecondaryMapper implements SecondaryMapper {

    private final String attributePath;
    private final CTEProvider cteProvider;

    public CteProviderSecondaryMapper(String attributePath, CTEProvider cteProvider) {
        this.attributePath = attributePath;
        this.cteProvider = cteProvider;
    }

    @Override
    public String getAttributePath() {
        return attributePath;
    }

    @Override
    public void apply(FullQueryBuilder<?, ?> fullQueryBuilder, ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, ViewJpqlMacro viewJpqlMacro, EmbeddingViewJpqlMacro embeddingViewJpqlMacro) {
        if (fullQueryBuilder instanceof CTEBuilder) {
            cteProvider.applyCtes((CTEBuilder<?>) fullQueryBuilder, optionalParameters);
        }
    }
}
