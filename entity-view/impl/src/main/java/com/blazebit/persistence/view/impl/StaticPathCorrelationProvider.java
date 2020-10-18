/*
 * Copyright 2014 - 2020 Blazebit.
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
 * @since 1.5.0
 */
public class StaticPathCorrelationProvider implements CorrelationProvider, CorrelationProviderFactory {

    private final String correlationPath;
    private final String correlationKeyAlias;
    private final int approximateExpressionSize;
    private final Predicate correlationPredicate;
    private final Set<String> rootAliases;

    public StaticPathCorrelationProvider(String correlationPath, Set<String> rootAliases) {
        this.correlationPath = correlationPath;
        this.correlationKeyAlias = null;
        this.approximateExpressionSize = -1;
        this.correlationPredicate = null;
        this.rootAliases = rootAliases;
    }
    public StaticPathCorrelationProvider(String correlationPath, String correlationKeyAlias, String correlationExpression, Predicate correlationPredicate, Set<String> rootAliases) {
        this.correlationPath = correlationPath;
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

    public String getCorrelationPath() {
        return correlationPath;
    }

    public Predicate getCorrelationPredicate() {
        return correlationPredicate;
    }

    @Override
    public void applyCorrelation(CorrelationBuilder correlationBuilder, String correlationExpression) {
        ExpressionFactory expressionFactory = correlationBuilder.getService(ExpressionFactory.class);
        // Prefix by correlationExpression because that is the base alias
        String finalCorrelationPath = PrefixingQueryGenerator.prefix(expressionFactory, expressionFactory.createSimpleExpression(correlationPath, false), correlationExpression, rootAliases, false);
        if (correlationPredicate == null) {
            correlationBuilder.correlate(finalCorrelationPath).onExpression("1=1").end();
        } else {
            String alias = correlationBuilder.getCorrelationAlias();

            PrefixingQueryGenerator prefixingQueryGenerator = new PrefixingQueryGenerator(expressionFactory, alias, correlationExpression, correlationKeyAlias, rootAliases, true, false);
            StringBuilder sb = new StringBuilder(approximateExpressionSize);
            prefixingQueryGenerator.setQueryBuffer(sb);
            correlationPredicate.accept(prefixingQueryGenerator);
            String finalExpression = sb.toString();

            correlationBuilder.correlate(finalCorrelationPath).onExpression(finalExpression).end();
        }
    }

}
