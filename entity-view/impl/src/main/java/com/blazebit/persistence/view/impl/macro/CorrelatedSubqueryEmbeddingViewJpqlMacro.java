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

package com.blazebit.persistence.view.impl.macro;

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.spi.FunctionRenderContext;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class CorrelatedSubqueryEmbeddingViewJpqlMacro extends CorrelatedSubqueryViewRootJpqlMacro implements EmbeddingViewJpqlMacro {

    public static final String CORRELATION_EMBEDDING_VIEW_ALIAS = "correlationEmbeddingViewAlias_";
    private static final String CORRELATION_EMBEDDING_VIEW_PARAM_PREFIX = "correlationEmbeddingViewParam_";
    private static final String CORRELATION_EMBEDDING_VIEW_ID_PARAM_PREFIX = "correlationEmbeddingViewIdParam_";

    private final CorrelatedSubqueryViewRootJpqlMacro viewRootJpqlMacro;
    private final String idPath;
    private String embeddingViewPath;
    private boolean embeddingViewPathSet;

    public CorrelatedSubqueryEmbeddingViewJpqlMacro(FullQueryBuilder<?, ?> criteriaBuilder, Map<String, Object> optionalParameters, boolean batchedViewRoot, Class<?> viewRootEntityType, String viewRootIdPath, String viewRootExpression, boolean batchedIdValues, CorrelatedSubqueryViewRootJpqlMacro viewRootJpqlMacro) {
        super(criteriaBuilder, optionalParameters, batchedViewRoot, viewRootEntityType, viewRootIdPath, viewRootExpression);
        this.viewRootJpqlMacro = viewRootJpqlMacro;
        // When id values rather than "this" is batched, we need to use "value" rather than the id path
        this.idPath = batchedIdValues ? "value" : viewRootIdPath;
    }

    @Override
    public boolean usesEmbeddingView() {
        return embeddingViewPath != null || viewRootExpression != null;
    }

    @Override
    protected String getViewRootIdPath() {
        return idPath;
    }

    @Override
    public String getEmbeddingViewPath() {
        if (embeddingViewPathSet) {
            return embeddingViewPath;
        }
        if (batchedViewRoot) {
            return viewRootExpression;
        }

        return CORRELATION_EMBEDDING_VIEW_ALIAS;
    }

    @Override
    public void setEmbeddingViewPath(String embeddingViewPath) {
        this.embeddingViewPathSet = embeddingViewPath != null;
        this.embeddingViewPath = embeddingViewPath;
    }

    @Override
    protected String getParamName() {
        if (viewRootParamName == null) {
            viewRootParamName = generateParamName(CORRELATION_EMBEDDING_VIEW_PARAM_PREFIX);
        }

        return viewRootParamName;
    }

    @Override
    protected String getIdParamName() {
        if (viewRootIdParamName == null) {
            viewRootIdParamName = generateParamName(CORRELATION_EMBEDDING_VIEW_ID_PARAM_PREFIX);
        }

        return viewRootIdParamName;
    }

    @Override
    public void addBatchPredicate(FullQueryBuilder<?, ?> criteriaBuilder) {
        if (viewRootExpression == CORRELATION_EMBEDDING_VIEW_ALIAS) {
            criteriaBuilder.innerJoinOn(originalViewRootExpression, viewRootEntityType, CORRELATION_EMBEDDING_VIEW_ALIAS)
                    .on(originalViewRootExpression + "." + viewRootIdPath).eqExpression(CORRELATION_EMBEDDING_VIEW_ALIAS + "." + viewRootIdPath)
                    .end();
        }
    }

    @Override
    protected String addViewRootNode() {
        if (embeddingViewPathSet) {
            return embeddingViewPath;
        }
        if (viewRootExpression != CORRELATION_EMBEDDING_VIEW_ALIAS) {
            originalViewRootExpression = viewRootExpression;
            viewRootExpression = CORRELATION_EMBEDDING_VIEW_ALIAS;
            // See addBatchPredicate for how the join node is added
            if (!batchedViewRoot) {
                criteriaBuilder.from(viewRootEntityType, CORRELATION_EMBEDDING_VIEW_ALIAS);
            }
        }
        return viewRootExpression;
    }

    @Override
    public void render(FunctionRenderContext context) {
        if (context.getArgumentsSize() > 1) {
            throw new IllegalArgumentException("The EMBEDDING_VIEW macro allows maximally one argument: <expression>!");
        }
        if (viewRootJpqlMacro.usesViewMacro()) {
            throw new UnsupportedOperationException("It's not yet support to use the EMBEDDING_VIEW macro along with the VIEW_ROOT macro in correlations!");
        }
        super.render(context);
    }
}
