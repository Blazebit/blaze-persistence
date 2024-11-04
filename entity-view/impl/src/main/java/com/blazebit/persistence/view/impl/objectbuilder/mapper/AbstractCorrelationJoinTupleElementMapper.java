/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.mapper;

import com.blazebit.persistence.parser.SimpleQueryGenerator;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.view.impl.CorrelationProviderHelper;
import com.blazebit.persistence.view.impl.PrefixingQueryGenerator;
import com.blazebit.persistence.view.impl.objectbuilder.Limiter;
import com.blazebit.persistence.view.spi.EmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.spi.type.BasicUserTypeStringSupport;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractCorrelationJoinTupleElementMapper implements AliasedTupleElementMapper {

    protected final String correlationBasis;
    protected final String correlationResult;
    protected final String correlationAlias;
    protected final String correlationExternalAlias;
    protected final BasicUserTypeStringSupport<Object> correlationResultBasicType;
    protected final String joinBase;
    protected final String alias;
    protected final String attributePath;
    protected final String embeddingViewPath;
    protected final String[] fetches;
    protected final Limiter limiter;

    public AbstractCorrelationJoinTupleElementMapper(ExpressionFactory ef, String joinBase, String correlationBasis, Expression correlationResult, BasicUserTypeStringSupport<Object> correlationResultBasicType, String alias, String attributePath, String embeddingViewPath, String[] fetches, Limiter limiter, Set<String> rootAliases) {
        this.correlationBasis = correlationBasis.intern();
        this.alias = alias;
        this.attributePath = attributePath;
        this.embeddingViewPath = embeddingViewPath;
        this.fetches = fetches;
        this.joinBase = joinBase.intern();
        this.correlationAlias = CorrelationProviderHelper.getDefaultCorrelationAlias(attributePath);
        this.limiter = limiter;
        if (limiter == null) {
            this.correlationExternalAlias = correlationAlias;
        } else {
            this.correlationExternalAlias = CorrelationProviderHelper.getDefaultExternalCorrelationAlias(attributePath);
        }
        this.correlationResultBasicType = correlationResultBasicType;
        if (correlationResult == null || correlationResult instanceof PathExpression && ((PathExpression) correlationResult).getExpressions().isEmpty()) {
            this.correlationResult = correlationExternalAlias;
        } else {
            StringBuilder sb = new StringBuilder(correlationExternalAlias.length() + 20);
            EmbeddingViewJpqlMacro embeddingViewJpqlMacro = (EmbeddingViewJpqlMacro) ef.getDefaultMacroConfiguration().get("EMBEDDING_VIEW").getState()[0];
            String oldEmbeddingViewPath = embeddingViewJpqlMacro.getEmbeddingViewPath();
            embeddingViewJpqlMacro.setEmbeddingViewPath(embeddingViewPath);
            SimpleQueryGenerator generator = new PrefixingQueryGenerator(ef, correlationExternalAlias, joinBase, null, rootAliases, true, false);
            generator.setQueryBuffer(sb);
            correlationResult.accept(generator);
            embeddingViewJpqlMacro.setEmbeddingViewPath(oldEmbeddingViewPath);
            this.correlationResult = sb.toString().intern();
        }
    }

    @Override
    public String getAttributePath() {
        return attributePath;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public BasicUserTypeStringSupport<Object> getBasicTypeStringSupport() {
        throw new UnsupportedOperationException();
    }
}
