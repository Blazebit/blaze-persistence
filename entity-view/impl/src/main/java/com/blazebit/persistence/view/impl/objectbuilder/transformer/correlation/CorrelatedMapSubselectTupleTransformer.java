/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.view.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.impl.metamodel.ManagedViewTypeImplementor;
import com.blazebit.persistence.view.impl.objectbuilder.ContainerAccumulator;
import com.blazebit.persistence.view.impl.objectbuilder.Limiter;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CorrelatedMapSubselectTupleTransformer extends AbstractCorrelatedSubselectTupleTransformer {

    private final boolean recording;

    public CorrelatedMapSubselectTupleTransformer(ExpressionFactory ef, Correlator correlator, ContainerAccumulator<?> containerAccumulator, EntityViewManagerImpl evm, ManagedViewTypeImplementor<?> viewRootType, String viewRootAlias, ManagedViewTypeImplementor<?> embeddingViewType, String embeddingViewPath,
                                                  Expression correlationResult, String correlationBasisExpression, String correlationKeyExpression, CorrelationProviderFactory correlationProviderFactory, String attributePath, String[] fetches,
                                                  String[] indexFetches, Expression indexExpression, Correlator indexCorrelator, int viewRootIndex, int embeddingViewIndex, int tupleIndex, Class<?> correlationBasisType, Class<?> correlationBasisEntity,
                                                  Limiter limiter, EntityViewConfiguration entityViewConfiguration, boolean recording) {
        super(ef, correlator, containerAccumulator, evm, viewRootType, viewRootAlias, embeddingViewType, embeddingViewPath, correlationResult, correlationBasisExpression, correlationKeyExpression, correlationProviderFactory, attributePath, fetches, indexFetches, indexExpression, indexCorrelator, viewRootIndex, embeddingViewIndex,
                tupleIndex, correlationBasisType, correlationBasisEntity, limiter, entityViewConfiguration);
        this.recording = recording;
    }

    @Override
    protected boolean isRecording() {
        return recording;
    }

}
