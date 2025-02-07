/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.jaxrs.jackson.testsuite.resource;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;

import javax.enterprise.inject.Alternative;
import javax.ws.rs.ApplicationPath;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@Alternative
@ApplicationPath("/")
public class Application extends ResourceConfig {

    public Application() {
        this.packages("com.blazebit.persistence.integration.jaxrs.jackson");
        this.register(JacksonJsonProvider.class);
    }
}
