/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package ${package}.controller;

import ${package}.repository.CatSimpleViewRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Path;

@Path("/")
public class SampleController {

    @Inject
    CatSimpleViewRepository catRepo;

    @GET
    @Produces("text/plain")
    public String home() {
        return catRepo.findAll().toString();
    }

}