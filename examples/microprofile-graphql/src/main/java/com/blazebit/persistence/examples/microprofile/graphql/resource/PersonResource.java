/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.microprofile.graphql.resource;

import java.net.URI;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.examples.microprofile.graphql.model.Person;
import com.blazebit.persistence.examples.microprofile.graphql.view.PersonCreateView;
import com.blazebit.persistence.examples.microprofile.graphql.view.PersonSimpleView;
import com.blazebit.persistence.examples.microprofile.graphql.view.PersonUpdateView;
import com.blazebit.persistence.integration.jaxrs.EntityViewId;
import com.blazebit.persistence.view.EntityViewManager;

/**
 * @author Christian Beikov
 * @since 1.6.2
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
    public PersonSimpleView updatePerson(@EntityViewId("id") PersonUpdateView personUpdateView) {
        evm.save(em, personUpdateView);
        return evm.find(em, PersonSimpleView.class, personUpdateView.getId());
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
