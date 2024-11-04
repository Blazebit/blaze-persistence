/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.correlation.general.model;

import com.blazebit.persistence.view.CorrelationBuilder;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.testsuite.entity.Document;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class OwnerOnlyRelatedCorrelationProviderNormal implements CorrelationProvider {

    @Override
    public void applyCorrelation(CorrelationBuilder correlationBuilder, String correlationExpression) {
        String correlatedDocument = correlationBuilder.getCorrelationAlias();
        correlationBuilder.correlate(Document.class)
            .on(correlatedDocument + ".owner").inExpressions(correlationExpression)
        .end();
    }
}