/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.view.impl.objectbuilder.mapper;

import com.blazebit.persistence.parser.SimpleQueryGenerator;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.view.impl.CorrelationProviderHelper;
import com.blazebit.persistence.view.impl.PrefixingQueryGenerator;

import java.util.Collections;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractCorrelationJoinTupleElementMapper implements TupleElementMapper {

    protected final String correlationBasis;
    protected final String correlationResult;
    protected final String correlationAlias;
    protected final String joinBase;
    protected final String alias;
    protected final String embeddingViewPath;
    protected final String[] fetches;

    public AbstractCorrelationJoinTupleElementMapper(ExpressionFactory ef, String joinBase, String correlationBasis, String correlationResult, String alias, String attributePath, String embeddingViewPath, String[] fetches) {
        this.correlationBasis = correlationBasis.intern();
        this.alias = alias;
        this.embeddingViewPath = embeddingViewPath;
        this.fetches = fetches;
        this.joinBase = joinBase.intern();
        this.correlationAlias = CorrelationProviderHelper.getDefaultCorrelationAlias(attributePath);
        if (correlationResult.isEmpty()) {
            this.correlationResult = correlationAlias;
        } else {
            StringBuilder sb = new StringBuilder(correlationAlias.length() + correlationResult.length() + 1);
            Expression expr = ef.createSimpleExpression(correlationResult, false);
            SimpleQueryGenerator generator = new PrefixingQueryGenerator(Collections.singletonList(correlationAlias));
            generator.setQueryBuffer(sb);
            expr.accept(generator);
            this.correlationResult = sb.toString().intern();
        }
    }

}
