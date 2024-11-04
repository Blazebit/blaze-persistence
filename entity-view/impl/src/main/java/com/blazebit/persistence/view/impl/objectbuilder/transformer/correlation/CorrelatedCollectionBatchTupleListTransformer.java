/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.view.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.objectbuilder.ContainerAccumulator;
import com.blazebit.persistence.view.impl.objectbuilder.Limiter;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CorrelatedCollectionBatchTupleListTransformer extends AbstractCorrelatedBatchTupleListTransformer {

    private final boolean recording;

    public CorrelatedCollectionBatchTupleListTransformer(ExpressionFactory ef, Correlator correlator, ContainerAccumulator<?> containerAccumulator, ManagedViewTypeImplementor<?> viewRootType, ManagedViewTypeImplementor<?> embeddingViewType, Expression correlationResult, CorrelationProviderFactory correlationProviderFactory, String attributePath, String[] fetches,
                                                         String[] indexFetches, Expression indexExpression, Correlator indexCorrelator, boolean correlatesThis, int viewRootIndex, int embeddingViewIndex, int tupleIndex, int batchSize, Class<?> correlationBasisType, Class<?> correlationBasisEntity, Limiter limiter, EntityViewConfiguration entityViewConfiguration, boolean recording) {
        super(ef, correlator, containerAccumulator, viewRootType, embeddingViewType, correlationResult, correlationProviderFactory, attributePath, fetches, indexFetches, indexExpression, indexCorrelator, correlatesThis, viewRootIndex, embeddingViewIndex, tupleIndex, batchSize, correlationBasisType, correlationBasisEntity, limiter, entityViewConfiguration);
        this.recording = recording;
    }

    @Override
    protected boolean isRecording() {
        return recording;
    }

}
