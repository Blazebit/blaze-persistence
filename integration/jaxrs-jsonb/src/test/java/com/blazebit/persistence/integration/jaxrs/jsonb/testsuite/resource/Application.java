/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.jaxrs.jsonb.testsuite.resource;

import com.blazebit.persistence.integration.jaxrs.jsonb.testsuite.JsonbJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;

import javax.enterprise.inject.Alternative;
import javax.ws.rs.ApplicationPath;

/**
 * @author Moritz Becker
 * @since 1.6.4
 */
@Alternative
@ApplicationPath("/")
public class Application extends ResourceConfig {

    public Application() {
        this.packages("com.blazebit.persistence.integration.jaxrs.jsonb");
        this.register(JsonbJsonProvider.class);
    }
}
