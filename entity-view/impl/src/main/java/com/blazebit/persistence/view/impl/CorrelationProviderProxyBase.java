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

import com.blazebit.persistence.parser.PrefixingAndAliasReplacementQueryGenerator;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.expression.MacroFunction;
import com.blazebit.persistence.view.CorrelationBuilder;
import com.blazebit.persistence.view.CorrelationProvider;
import com.blazebit.persistence.view.spi.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.impl.metamodel.AbstractAttribute;
import com.blazebit.persistence.view.spi.ViewRootJpqlMacro;

/**
 * This serves as base implementation for correlation providers that is copied at runtime for simple correlation mappings.
 *
 * WARNING: When doing changes here, check if it doesn't violate assumtions in {@link com.blazebit.persistence.view.impl.proxy.ProxyFactory#getCorrelationProviderProxy(Class, String, String)}.
 *
 * One of the assumptions is that two constructors exist, the default one and the parameterized one.
 * The order of the parameters is also important.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CorrelationProviderProxyBase implements CorrelationProvider {

    private final Class<?> correlated;
    private final String correlationKeyAlias;
    private final int approximateExpressionSize;
    private final String correlationExpression;

    private CorrelationProviderProxyBase(Class<?> correlated, String correlationKeyAlias, String correlationExpression) {
        this.correlated = correlated;
        this.correlationKeyAlias = correlationKeyAlias;
        this.approximateExpressionSize = correlationExpression.length() * 2;
        this.correlationExpression = correlationExpression;
    }

    // NOTE: Careful, you can't debug into this method as it is copied
    @Override
    public void applyCorrelation(CorrelationBuilder correlationBuilder, String correlationExpression) {
        String alias = correlationBuilder.getCorrelationAlias();

        // Exercise the parser first to resolve the view root
        ExpressionFactory expressionFactory = correlationBuilder.getService(ExpressionFactory.class);
        String originalExpressionString = AbstractAttribute.replaceThisFromMapping(this.correlationExpression, alias);
        String expressionString = originalExpressionString;

                // Find out the view root alias
        MacroFunction viewRootFunction = expressionFactory.getDefaultMacroConfiguration().get("VIEW_ROOT");
        ViewRootJpqlMacro viewRootMacro = (ViewRootJpqlMacro) viewRootFunction.getState()[0];
        EmbeddingViewJpqlMacro embeddingViewJpqlMacro = (EmbeddingViewJpqlMacro) expressionFactory.getDefaultMacroConfiguration().get("EMBEDDING_VIEW").getState()[0];
        String viewRoot = viewRootMacro.getViewRoot();
        String embeddingViewPath = embeddingViewJpqlMacro.getEmbeddingViewPath();
        // Don't replace the correlationKey alias
        expressionString = CorrelationProviderHelper.temporaryReplace(expressionString, correlationKeyAlias.equals(viewRoot) ? null : viewRoot, CorrelationProviderHelper.TEMPORARY_VIEW_ROOT_MATCHING_NAME);
        expressionString = CorrelationProviderHelper.temporaryReplace(expressionString, correlationKeyAlias.equals(embeddingViewPath) ? null : embeddingViewPath , CorrelationProviderHelper.TEMPORARY_EMBEDDING_VIEW_MATCHING_NAME);

        Expression expression = expressionFactory.createBooleanExpression(expressionString, false);

        // Prefix all paths except view root alias based ones and substitute the key alias with the correlation expression
        String aliasToSkip;
        if (viewRootMacro.getViewRoot() == null) {
            aliasToSkip = embeddingViewJpqlMacro.getEmbeddingViewPath();
        } else {
            aliasToSkip = viewRootMacro.getViewRoot();
        }
        PrefixingAndAliasReplacementQueryGenerator generator = new PrefixingAndAliasReplacementQueryGenerator(alias, correlationExpression, correlationKeyAlias, aliasToSkip, true);
        StringBuilder buffer = new StringBuilder(approximateExpressionSize);
        generator.setQueryBuffer(buffer);
        expression.accept(generator);
        String finalExpression = buffer.toString();

        if (originalExpressionString != expressionString) {
            if (viewRoot != null) {
                finalExpression = finalExpression.replaceAll(CorrelationProviderHelper.TEMPORARY_VIEW_ROOT_MATCHING_NAME, viewRoot);
            }
            if (embeddingViewPath != null) {
                finalExpression = finalExpression.replaceAll(CorrelationProviderHelper.TEMPORARY_EMBEDDING_VIEW_MATCHING_NAME, embeddingViewPath);
            }
        }

        correlationBuilder.correlate(correlated)
                .setOnExpression(finalExpression);
    }

}
