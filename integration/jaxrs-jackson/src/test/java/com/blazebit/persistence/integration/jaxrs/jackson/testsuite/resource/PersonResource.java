/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.jaxrs.jackson.testsuite.resource;

import com.blazebit.persistence.integration.jaxrs.EntityViewId;
import com.blazebit.persistence.integration.jaxrs.jackson.testsuite.config.EntityManagerHolder;
import com.blazebit.persistence.integration.jaxrs.jackson.testsuite.view.PersonCreateView;
import com.blazebit.persistence.integration.jaxrs.jackson.testsuite.view.PersonUpdateView;
import com.blazebit.persistence.integration.jaxrs.jackson.testsuite.view.PersonView;
import com.blazebit.persistence.view.EntityViewManager;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@Path("persons")
public class PersonResource {

    @Inject
    private EntityManagerHolder entityManagerHolder;
    @Inject
    private EntityViewManager evm;

    @Transactional
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createPerson(PersonCreateView personCreateView) {
        evm.save(entityManagerHolder.getEntityManager(), personCreateView);
        return Response.created(URI.create("/persons/" + personCreateView.getId())).build();
    }

    @Transactional
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PersonView updatePerson(@EntityViewId("id") PersonUpdateView personUpdate) {
        return updatePerson0(personUpdate);
    }

    private PersonView updatePerson0(PersonUpdateView personUpdate) {
        evm.save(entityManagerHolder.getEntityManager(), personUpdate);
        return evm.find(entityManagerHolder.getEntityManager(), PersonView.class, personUpdate.getId().toString());
    }
}
