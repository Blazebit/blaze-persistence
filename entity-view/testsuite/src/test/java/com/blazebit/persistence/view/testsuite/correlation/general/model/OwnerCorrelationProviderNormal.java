/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.correlation.general.model;

import com.blazebit.persistence.view.CorrelationBuilder;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class OwnerCorrelationProviderNormal implements CorrelationProvider {

    @Override
    public void applyCorrelation(CorrelationBuilder correlationBuilder, String correlationExpression) {
        String correlatedPerson = correlationBuilder.getCorrelationAlias();
        correlationBuilder.correlate(Person.class)
            .on(correlatedPerson).inExpressions(correlationExpression)
        .end();
    }
}