/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package ${package}.controller;

import ${package}.repository.CatSimpleViewRepository;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;

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