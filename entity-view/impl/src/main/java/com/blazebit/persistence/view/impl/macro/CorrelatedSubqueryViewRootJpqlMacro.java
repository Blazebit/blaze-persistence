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
import com.blazebit.persistence.view.spi.ViewRootJpqlMacro;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CorrelatedSubqueryViewRootJpqlMacro implements ViewRootJpqlMacro {

    private static final String CORRELATION_VIEW_ROOT_PARAM_PREFIX = "correlationViewRootParam_";
    private static final String CORRELATION_VIEW_ROOT_ID_PARAM_PREFIX = "correlationViewRootIdParam_";
    private static final String CORRELATION_VIEW_ROOT_ALIAS = "correlationViewRootAlias_";

    private final FullQueryBuilder<?, ?> criteriaBuilder;
    private final Map<String, Object> optionalParameters;
    private final boolean batchedViewRoot;
    private final Class<?> viewRootEntityType;
    private final String viewRootIdPath;

    private String viewRootExpression;
    private String viewRootParamName;
    private String viewRootIdParamName;

    public CorrelatedSubqueryViewRootJpqlMacro(FullQueryBuilder<?, ?> criteriaBuilder, Map<String, Object> optionalParameters, boolean batchedViewRoot, Class<?> viewRootEntityType, String viewRootIdPath, String viewRootExpression) {
        this.criteriaBuilder = criteriaBuilder;
        this.optionalParameters = optionalParameters;
        this.batchedViewRoot = batchedViewRoot;
        this.viewRootEntityType = viewRootEntityType;
        this.viewRootIdPath = viewRootIdPath;
        this.viewRootExpression = viewRootExpression;
    }

    public void setParameters(Query query, Object viewRootId) {
        if (batchedViewRoot) {
            query.setParameter(viewRootExpression, viewRootId);
            return;
        }

        if (viewRootParamName != null) {
            EntityManager em = criteriaBuilder.getEntityManager();
            if (viewRootId instanceof Collection) {
                Collection<Object> paramCollection = (Collection<Object>) viewRootId;
                List<Object> viewRootEntities = new ArrayList<Object>(paramCollection.size());
                for (Object paramValue : paramCollection) {
                    if (paramValue != null) {
                        viewRootEntities.add(em.getReference(viewRootEntityType, paramValue));
                    }
                }

                query.setParameter(viewRootParamName, viewRootEntities);
            } else {
                Object viewRootEntity = em.getReference(viewRootEntityType, viewRootId);
                query.setParameter(viewRootParamName, viewRootEntity);
            }
        }
        if (viewRootIdParamName != null) {
            query.setParameter(viewRootIdParamName, viewRootId);
        }
    }

    @Override
    public String getViewRoot() {
        // Might be null when the viewRootParamName should be used
        // If this is a parameter, we return null
        if (viewRootExpression == null || viewRootExpression.charAt(0) == ':') {
            return null;
        }
        return viewRootExpression;
    }

    public boolean usesViewRootEntityParameter() {
        return viewRootExpression != null;
    }

    public boolean usesViewRoot() {
        return viewRootParamName != null || viewRootIdParamName != null || viewRootExpression != null;
    }

    private String getViewRootParamName() {
        if (viewRootParamName == null) {
            viewRootParamName = generateParamName(CORRELATION_VIEW_ROOT_PARAM_PREFIX);
        }

        return viewRootParamName;
    }

    private String getViewRootIdParamName() {
        if (viewRootIdParamName == null) {
            viewRootIdParamName = generateParamName(CORRELATION_VIEW_ROOT_ID_PARAM_PREFIX);
        }

        return viewRootIdParamName;
    }

    private String generateParamName(String prefix) {
        int paramNumber = 0;
        String paramName;
        while (true) {
            paramName = prefix + paramNumber;
            if (criteriaBuilder.getParameter(paramName) != null) {
                paramNumber++;
            } else if (optionalParameters.containsKey(paramName)) {
                paramNumber++;
            } else {
                return paramName;
            }
        }
    }

    @Override
    public void render(FunctionRenderContext context) {
        if (context.getArgumentsSize() > 1) {
            throw new IllegalArgumentException("The VIEW_ROOT macro allows maximally one argument: <expression>!");
        }

        if (context.getArgumentsSize() > 0) {
            if (viewRootExpression != null) {
                String firstArgument = context.getArgument(0);
                if (viewRootIdPath.startsWith(firstArgument) && (viewRootIdPath.length() == firstArgument.length() || firstArgument.charAt(viewRootIdPath.length()) == '.')) {
                    // Using the plain id or accessing a sub-component of the id is allowed
                    context.addChunk(viewRootExpression);
                    context.addChunk(".");
                    context.addArgument(0);
                } else {
                    if (batchedViewRoot) {
                        if (criteriaBuilder.getFrom(CORRELATION_VIEW_ROOT_ALIAS) == null) {
                            viewRootExpression = CORRELATION_VIEW_ROOT_ALIAS;
                            criteriaBuilder.from(viewRootEntityType, CORRELATION_VIEW_ROOT_ALIAS);
                        }
                    }
                    context.addChunk(viewRootExpression);
                    context.addChunk(".");
                    context.addArgument(0);
                }
            } else {
                if (viewRootIdPath.equals(context.getArgument(0))) {
                    context.addChunk(":");
                    context.addChunk(getViewRootIdParamName());
                } else {
                    if (criteriaBuilder.getFrom(CORRELATION_VIEW_ROOT_ALIAS) == null) {
                        viewRootExpression = CORRELATION_VIEW_ROOT_ALIAS;
                        criteriaBuilder.from(viewRootEntityType, CORRELATION_VIEW_ROOT_ALIAS);
                    }
                    context.addChunk(CORRELATION_VIEW_ROOT_ALIAS);
                    context.addChunk(".");
                    context.addArgument(0);
                }
            }
        } else {
            if (viewRootExpression != null) {
                context.addChunk(viewRootExpression);
            } else {
                context.addChunk(":");
                context.addChunk(getViewRootParamName());
            }
        }
    }
}
