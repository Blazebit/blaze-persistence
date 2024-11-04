/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.quarkus.base.resource;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.examples.quarkus.base.entity.Person;
import com.blazebit.persistence.examples.quarkus.base.view.PersonCreateView;
import com.blazebit.persistence.examples.quarkus.base.view.PersonUpdateView;
import com.blazebit.persistence.examples.quarkus.base.view.PersonView;
import com.blazebit.persistence.integration.jaxrs.EntityViewId;
import com.blazebit.persistence.view.EntityViewManager;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@Path("persons")
public class PersonResource {

    @Inject
    private EntityManager em;
    @Inject
    private EntityViewManager evm;
    @Inject
    private CriteriaBuilderFactory cbf;

    @Transactional
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PersonView updatePerson(@EntityViewId("id") PersonUpdateView personUpdateView) {
        evm.save(em, personUpdateView);
        return evm.find(em, PersonView.class, personUpdateView.getId());
    }

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    public Response addPerson(PersonCreateView view) {
        evm.save(em, view);
        return Response.created(URI.create("/persons/" + view.getId())).build();
    }

    @DELETE
    @Transactional
    public Response clearPersons() {
        cbf.delete(em, Person.class).executeUpdate();
        return Response.ok().build();
    }
}
