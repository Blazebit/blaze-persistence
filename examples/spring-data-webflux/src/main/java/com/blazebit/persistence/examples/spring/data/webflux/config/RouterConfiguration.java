/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.webflux.config;

import com.blazebit.persistence.examples.spring.data.webflux.controller.CatRestController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
@Configuration
public class RouterConfiguration {

    @Bean
    public RouterFunction<ServerResponse> createRouterFunctions(CatRestController controller) {
        return RouterFunctions.route(RequestPredicates.POST("/test").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), controller::test);
    }
}