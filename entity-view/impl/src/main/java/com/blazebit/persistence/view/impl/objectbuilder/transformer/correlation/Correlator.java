/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation;

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.spi.EmbeddingViewJpqlMacro;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface Correlator {

    public int getElementOffset();

    public ObjectBuilder<?> finish(FullQueryBuilder<?, ?> criteriaBuilder, EntityViewConfiguration entityViewConfiguration, int offset, int tupleSuffix, String correlationRoot, EmbeddingViewJpqlMacro embeddingViewJpqlMacro, boolean nullFlatViewIfEmpty);

}
