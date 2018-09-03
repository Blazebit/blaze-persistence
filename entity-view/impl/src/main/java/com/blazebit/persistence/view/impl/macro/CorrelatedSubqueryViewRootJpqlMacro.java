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

    public static final String CORRELATION_VIEW_ROOT_ALIAS = "correlationViewRootAlias_";
    private static final String CORRELATION_VIEW_ROOT_PARAM_PREFIX = "correlationViewRootParam_";
    private static final String CORRELATION_VIEW_ROOT_ID_PARAM_PREFIX = "correlationViewRootIdParam_";

    protected final FullQueryBuilder<?, ?> criteriaBuilder;
    protected final Map<String, Object> optionalParameters;
    protected final boolean batchedViewRoot;
    protected final Class<?> viewRootEntityType;
    protected final String viewRootIdPath;

    protected String originalViewRootExpression;
    protected String viewRootExpression;
    protected String viewRootParamName;
    protected String viewRootIdParamName;
    protected boolean used;
    protected boolean nonIdUsed;

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
            if (originalViewRootExpression != null) {
                setEntityParam(query, originalViewRootExpression, viewRootId);
            } else {
                setEntityParam(query, viewRootExpression, viewRootId);
            }
            return;
        }

        if (viewRootParamName != null) {
            setEntityParam(query, viewRootParamName, viewRootId);
        }
        if (viewRootIdParamName != null) {
            criteriaBuilder.setParameter(viewRootIdParamName, viewRootId);
            query.setParameter(viewRootIdParamName, viewRootId);
        }
    }

    protected final void setEntityParam(Query query, String paramName, Object viewRootId) {
        EntityManager em = criteriaBuilder.getEntityManager();
        if (viewRootId instanceof Collection) {
            Collection<Object> paramCollection = (Collection<Object>) viewRootId;
            List<Object> viewRootEntities = new ArrayList<Object>(paramCollection.size());
            for (Object paramValue : paramCollection) {
                if (paramValue != null) {
                    viewRootEntities.add(em.getReference(viewRootEntityType, paramValue));
                }
            }

            criteriaBuilder.setParameter(paramName, viewRootEntities);
            query.setParameter(paramName, viewRootEntities);
        } else {
            Object viewRootEntity = em.getReference(viewRootEntityType, viewRootId);
            criteriaBuilder.setParameter(paramName, viewRootEntity);
            query.setParameter(paramName, viewRootEntity);
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
        return viewRootExpression != null;// && viewRootExpression.charAt(0) == ':';
    }

    public boolean usesViewMacro() {
        return used;
    }

    public boolean usesViewMacroNonId() {
        return nonIdUsed;
    }

    public void addIdParamPredicate(FullQueryBuilder<?, ?> criteriaBuilder) {
        if (viewRootExpression != null && viewRootExpression.charAt(0) != ':') {
            if (viewRootParamName != null) {
                criteriaBuilder.where(viewRootExpression).eqExpression(":" + viewRootParamName);
            } else {
                criteriaBuilder.where(viewRootExpression + '.' + viewRootIdPath).eqExpression(":" + getIdParamName());
            }
        }
    }

    public void addBatchPredicate(FullQueryBuilder<?, ?> criteriaBuilder) {
        if (viewRootExpression == CORRELATION_VIEW_ROOT_ALIAS) {
            criteriaBuilder.innerJoinOn(originalViewRootExpression, viewRootEntityType, CORRELATION_VIEW_ROOT_ALIAS)
                    .on(originalViewRootExpression + "." + viewRootIdPath).eqExpression(CORRELATION_VIEW_ROOT_ALIAS + "." + viewRootIdPath)
                    .end();
        }
    }

    protected String getParamName() {
        if (viewRootParamName == null) {
            viewRootParamName = generateParamName(CORRELATION_VIEW_ROOT_PARAM_PREFIX);
        }

        return viewRootParamName;
    }

    protected String getIdParamName() {
        if (viewRootIdParamName == null) {
            viewRootIdParamName = generateParamName(CORRELATION_VIEW_ROOT_ID_PARAM_PREFIX);
        }

        return viewRootIdParamName;
    }

    protected String addViewRootNode() {
        if (viewRootExpression != CORRELATION_VIEW_ROOT_ALIAS) {
            originalViewRootExpression = viewRootExpression;
            viewRootExpression = CORRELATION_VIEW_ROOT_ALIAS;
            // See addBatchPredicate for how the join node is added
            if (!batchedViewRoot) {
                criteriaBuilder.from(viewRootEntityType, CORRELATION_VIEW_ROOT_ALIAS);
            }
        }
        return viewRootExpression;
    }

    protected final String generateParamName(String prefix) {
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

    protected String getViewRootIdPath() {
        return viewRootIdPath;
    }

    @Override
    public void render(FunctionRenderContext context) {
        if (context.getArgumentsSize() > 1) {
            throw new IllegalArgumentException("The VIEW_ROOT macro allows maximally one argument: <expression>!");
        }

        this.used = true;
        if (context.getArgumentsSize() > 0) {
            if (viewRootExpression != null) {
                String firstArgument = context.getArgument(0);
                if (viewRootIdPath.startsWith(firstArgument) && (viewRootIdPath.length() == firstArgument.length() || firstArgument.charAt(viewRootIdPath.length()) == '.')) {
                    // Using the plain id or accessing a sub-component of the id is allowed
                    context.addChunk(viewRootExpression);
                    context.addChunk(".");
                    context.addChunk(getViewRootIdPath());
                } else {
                    String alias;
                    if (batchedViewRoot) {
                        alias = addViewRootNode();
                    } else {
                        alias = viewRootExpression;
                    }
                    this.nonIdUsed = true;
                    context.addChunk(alias);
                    context.addChunk(".");
                    context.addArgument(0);
                }
            } else {
                if (viewRootIdPath.equals(context.getArgument(0))) {
                    context.addChunk(":");
                    context.addChunk(getIdParamName());
                } else {
                    String alias = addViewRootNode();
                    this.nonIdUsed = true;
                    context.addChunk(alias);
                    context.addChunk(".");
                    context.addArgument(0);
                }
            }
        } else {
            this.used = true;
            if (viewRootExpression != null) {
                context.addChunk(viewRootExpression);
            } else {
                context.addChunk(":");
                context.addChunk(getParamName());
            }
        }
    }
}
