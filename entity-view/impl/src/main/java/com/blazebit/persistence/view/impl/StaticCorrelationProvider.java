/*
 * Copyright 2014 - 2024 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.ParameterHolder;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.predicate.Predicate;
import com.blazebit.persistence.view.CorrelationBuilder;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.CorrelationProviderFactory;

import java.util.Map;
import java.util.Set;

/**
 * A static correlation provider implementation.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class StaticCorrelationProvider implements CorrelationProvider, CorrelationProviderFactory {

    private final Class<?> correlated;
    private final String correlationKeyAlias;
    private final int approximateExpressionSize;
    private final Predicate correlationPredicate;
    private final Set<String> rootAliases;

    public StaticCorrelationProvider(Class<?> correlated, String correlationKeyAlias, String correlationExpression, Predicate correlationPredicate, Set<String> rootAliases) {
        this.correlated = correlated;
        this.correlationKeyAlias = correlationKeyAlias;
        this.approximateExpressionSize = correlationExpression.length() * 2;
        this.correlationPredicate = correlationPredicate;
        this.rootAliases = rootAliases;
    }

    @Override
    public boolean isParameterized() {
        return false;
    }

    @Override
    public CorrelationProvider create(ParameterHolder<?> parameterHolder, Map<String, Object> optionalParameters) {
        return this;
    }

    public Predicate getCorrelationPredicate() {
        return correlationPredicate;
    }

    @Override
    public void applyCorrelation(CorrelationBuilder correlationBuilder, String correlationExpression) {
        String alias = correlationBuilder.getCorrelationAlias();

        ExpressionFactory expressionFactory = correlationBuilder.getService(ExpressionFactory.class);
        PrefixingQueryGenerator prefixingQueryGenerator = new PrefixingQueryGenerator(expressionFactory, alias, correlationExpression, correlationKeyAlias, rootAliases, true, false);
        StringBuilder sb = new StringBuilder(approximateExpressionSize);
        prefixingQueryGenerator.setQueryBuffer(sb);
        correlationPredicate.accept(prefixingQueryGenerator);
        String finalExpression = sb.toString();

        correlationBuilder.correlate(correlated).onExpression(finalExpression).end();
    }

}
