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

import javax.inject.Inject;
import javax.ws.rs.Consumes;
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
