/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.spring.data.webmvc.impl.json;

import com.blazebit.persistence.spring.data.webmvc.EntityViewId;
import com.blazebit.persistence.view.EntityViewManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public class EntityViewIdHandlerInterceptor implements HandlerInterceptor, AsyncHandlerInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(EntityViewIdHandlerInterceptor.class);

    private final EntityViewManager evm;
    private final EntityViewIdValueHolder entityViewIdValueHolder;

    public EntityViewIdHandlerInterceptor(EntityViewManager evm, EntityViewIdValueHolder entityViewIdValueHolder) {
        this.evm = evm;
        this.entityViewIdValueHolder = entityViewIdValueHolder;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            Map<String, String> uriTemplateVars = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            String entityViewIdPathVariableName = resolveEntityViewIdPathVariableName(handlerMethod);
            if (entityViewIdPathVariableName != null) {
                if (entityViewIdPathVariableName.isEmpty()) {
                    throw new ServletRequestBindingException("Failed to resolve entity view path variable name for handler method " + handlerMethod);
                } else {
                    String pathVariableValue = uriTemplateVars.get(entityViewIdPathVariableName);
                    if (pathVariableValue == null) {
                        throw new ServletRequestBindingException("Missing URI template variable '" + pathVariableValue + "' for handler method " + handlerMethod);
                    } else {
                        this.entityViewIdValueHolder.value.set(pathVariableValue);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        this.entityViewIdValueHolder.value.remove();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        this.entityViewIdValueHolder.value.remove();
    }

    private String resolveEntityViewIdPathVariableName(HandlerMethod handlerMethod) {
        String pathVariableName;
        EntityViewId entityViewId = null;
        MethodParameter entityViewIdMethodParameter = null;
        for (int i = 0; i < handlerMethod.getMethodParameters().length; i++) {
            MethodParameter methodParameter = handlerMethod.getMethodParameters()[i];
            entityViewId = methodParameter.getParameterAnnotation(EntityViewId.class);
            if (entityViewId != null) {
                if (evm.getMetamodel().managedView(methodParameter.getParameterType()) == null) {
                    LOG.warn("Handler argument " + methodParameter + " is annotated with @" + EntityViewId.class.getName() +
                            " but its type [" + methodParameter.getNestedParameterType().getName() +
                            "] is not an entity view.");
                    // log warning
                } else {
                    entityViewIdMethodParameter = handlerMethod.getMethodParameters()[i];
                }
                break;
            }
        }
        if (entityViewIdMethodParameter != null) {
            pathVariableName = entityViewId.name();
            if (pathVariableName.isEmpty()) {
                pathVariableName = entityViewIdMethodParameter.getParameterName();
                if (pathVariableName == null) {
                    throw new IllegalArgumentException(
                            "Entity view id path variable name for argument type [" + entityViewIdMethodParameter.getNestedParameterType().getName() +
                                    "] not available, and parameter name information not found in class file either.");
                }
            }
        } else {
            pathVariableName = null;
        }
        return pathVariableName;
    }
}
