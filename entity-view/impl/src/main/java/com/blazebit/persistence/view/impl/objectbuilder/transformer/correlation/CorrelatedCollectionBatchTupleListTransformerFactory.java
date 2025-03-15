/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation;

import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.view.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.objectbuilder.ContainerAccumulator;
import com.blazebit.persistence.view.impl.objectbuilder.Limiter;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.NullListTupleTransformer;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleListTransformer;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CorrelatedCollectionBatchTupleListTransformerFactory extends AbstractCorrelatedBatchTupleListTransformerFactory {

    private final String[] indexFetches;
    private final Expression indexExpression;
    private final Correlator indexCorrelator;
    private final ContainerAccumulator<?> containerAccumulator;
    private final boolean recording;

    public CorrelatedCollectionBatchTupleListTransformerFactory(Correlator correlator, ManagedViewTypeImplementor<?> viewRoot, ManagedViewTypeImplementor<?> embeddingViewType, Expression correlationResult, CorrelationProviderFactory correlationProviderFactory, String attributePath, String[] fetches,
                                                                boolean correlatesThis, int viewRootIndex, int embeddingViewIndex, int tupleIndex, int batchSize, Class<?> correlationBasisType, Class<?> correlationBasisEntity, Limiter limiter, String[] indexFetches,
                                                                Expression indexExpression, Correlator indexCorrelator, ContainerAccumulator<?> containerAccumulator, boolean recording) {
        super(correlator, viewRoot, embeddingViewType, correlationResult, correlationProviderFactory, attributePath, fetches, correlatesThis, viewRootIndex, embeddingViewIndex, tupleIndex, batchSize, correlationBasisType, correlationBasisEntity, limiter);
        this.indexFetches = indexFetches;
        this.indexExpression = indexExpression;
        this.indexCorrelator = indexCorrelator;
        this.containerAccumulator = containerAccumulator;
        this.recording = recording;
    }

    @Override
    public TupleListTransformer create(ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters, EntityViewConfiguration config) {
        if (!config.hasSubFetches(attributePath)) {
            return new NullListTupleTransformer(tupleIndex, tupleIndex);
        }
        return new CorrelatedCollectionBatchTupleListTransformer(config.getExpressionFactory(), correlator, containerAccumulator, viewRootType, embeddingViewType, correlationResult, correlationProviderFactory, attributePath, fetches, indexFetches, indexExpression, indexCorrelator, correlatesThis,
                viewRootIndex, embeddingViewIndex, tupleIndex, batchSize, correlationBasisType, correlationBasisEntity, limiter, config, recording);
    }

}
