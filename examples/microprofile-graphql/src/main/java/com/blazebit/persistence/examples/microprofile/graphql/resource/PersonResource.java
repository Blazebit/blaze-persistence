/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.microprofile.graphql.resource;

import java.net.URI;
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
