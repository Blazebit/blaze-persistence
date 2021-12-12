/*
 * Copyright 2014 - 2021 Blazebit.
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
