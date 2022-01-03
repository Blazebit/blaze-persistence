/*
 * Copyright 2014 - 2022 Blazebit.
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

package com.blazebit.persistence.spring.data.webflux.impl.json;

import com.blazebit.persistence.spring.data.webflux.EntityViewId;
import com.blazebit.persistence.view.EntityViewManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public class EntityViewIdAwareWebFilter implements WebFilter {

    public static final String ENTITY_VIEW_ID_CONTEXT_PARAM = "entityViewId";

    private static final Logger LOG = LoggerFactory.getLogger(EntityViewIdAwareWebFilter.class);

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final EntityViewManager evm;

    public EntityViewIdAwareWebFilter(RequestMappingHandlerMapping requestMappingHandlerMapping, EntityViewManager evm) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.evm = evm;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        HandlerMethod handlerMethod = (HandlerMethod) requestMappingHandlerMapping.getHandler(exchange).toProcessor().peek();
        String entityViewId;
        if (handlerMethod != null) {
            Map<String, String> uriTemplateVars = (Map<String, String>) exchange.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            String entityViewIdPathVariableName = resolveEntityViewIdPathVariableName(handlerMethod);
            if (entityViewIdPathVariableName == null) {
                entityViewId = null;
            } else {
                if (entityViewIdPathVariableName.isEmpty()) {
                    LOG.error("Failed to resolve entity view path variable name for handler method " + handlerMethod);
                    exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                    return Mono.empty();
                } else {
                    entityViewId = uriTemplateVars.get(entityViewIdPathVariableName);
                    if (entityViewId == null) {
                        LOG.error("Missing URI template variable '" + entityViewIdPathVariableName + "' for handler method " + handlerMethod);
                        exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                        return Mono.empty();
                    }
                }
            }
        } else {
            entityViewId = null;
        }
        Mono<Void> chainResult = chain.filter(exchange);
        if (entityViewId == null) {
            return chainResult;
        } else {
            return chainResult.subscriberContext(ctx -> ctx.put(ENTITY_VIEW_ID_CONTEXT_PARAM, entityViewId));
        }
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
