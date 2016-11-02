/*
 * Copyright 2014 - 2016 Blazebit.
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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlMacro;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CorrelatedSubqueryViewRootJpqlMacro implements JpqlMacro {

    private static final String CORRELATION_VIEW_ROOT_PARAM_PREFIX = "correlationViewRootParam_";
    private static final String CORRELATION_VIEW_ROOT_ID_PARAM_PREFIX = "correlationViewRootIdParam_";
    private static final String CORRELATION_VIEW_ROOT_ALIAS = "correlationViewRootAlias_";

    private final CriteriaBuilder<?> criteriaBuilder;
    private final Map<String, Object> optionalParameters;
    private final Class<?> viewRootEntityType;
    private final String viewRootIdPath;

    private boolean viewRootEntityRequired;
    private String viewRootParamName;
    private String viewRootIdParamName;

    public CorrelatedSubqueryViewRootJpqlMacro(CriteriaBuilder<?> criteriaBuilder, Map<String, Object> optionalParameters, Class<?> viewRootEntityType, String viewRootIdPath) {
        this.criteriaBuilder = criteriaBuilder;
        this.optionalParameters = optionalParameters;
        this.viewRootEntityType = viewRootEntityType;
        this.viewRootIdPath = viewRootIdPath;
    }

    public void setParameters(Object viewRootId) {
        if (viewRootEntityRequired) {
            criteriaBuilder.from(viewRootEntityType, CORRELATION_VIEW_ROOT_ALIAS);
        }
        if (viewRootParamName != null) {
            if (viewRootId instanceof Collection) {
                Collection<Object> paramCollection = (Collection<Object>) viewRootId;
                List<Object> viewRootEntities = new ArrayList<Object>(paramCollection.size());
                for (Object paramValue : paramCollection) {
                    if (paramValue != null) {
                        viewRootEntities.add(criteriaBuilder.getEntityManager().getReference(viewRootEntityType, paramValue));
                    }
                }

                criteriaBuilder.setParameter(viewRootParamName, viewRootEntities);
            } else {
                Object viewRootEntity = criteriaBuilder.getEntityManager().getReference(viewRootEntityType, viewRootId);
                criteriaBuilder.setParameter(viewRootParamName, viewRootEntity);
            }
        }
        if (viewRootIdParamName != null) {
            criteriaBuilder.setParameter(viewRootIdParamName, viewRootId);
        }
    }

    public boolean usesViewRoot() {
        return viewRootParamName != null || viewRootIdParamName != null;
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
            if (viewRootIdPath.equals(context.getArgument(0))) {
                context.addChunk(":");
                context.addChunk(getViewRootIdParamName());
            } else {
                viewRootEntityRequired = true;
                context.addChunk(CORRELATION_VIEW_ROOT_ALIAS);
                context.addChunk(".");
                context.addArgument(0);
            }
        } else {
            context.addChunk(":");
            context.addChunk(getViewRootParamName());
        }
    }
}
