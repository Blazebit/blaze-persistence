/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.jaxrs.jsonb.testsuite.resource;

import com.blazebit.persistence.integration.jaxrs.EntityViewId;
import com.blazebit.persistence.integration.jaxrs.jsonb.testsuite.config.EntityManagerHolder;
import com.blazebit.persistence.integration.jaxrs.jsonb.testsuite.view.DocumentUpdateView;
import com.blazebit.persistence.integration.jaxrs.jsonb.testsuite.view.DocumentView;
import com.blazebit.persistence.view.EntityViewManager;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Objects;

/**
 * @author Moritz Becker
 * @since 1.6.4
 */
@Path("documents")
public class DocumentResource {

    @Inject
    private EntityManagerHolder emHolder;
    @Inject
    private EntityViewManager evm;

    @Transactional
    @PUT
    @Path("{id1}")
    @Consumes("application/vnd.blazebit.update1+json")
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentView updateDocument1(@EntityViewId("id1") DocumentUpdateView documentUpdateView) {
        return updateDocument0(documentUpdateView);
    }

    @Transactional
    @PUT
    @Path("{id2}")
    @Consumes("application/vnd.blazebit.update2+json")
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentView updateDocument2(@PathParam(value = "id2") Long documentId, @EntityViewId("id2") DocumentUpdateView documentUpdate) {
        Objects.requireNonNull(documentId);
        return updateDocument0(documentUpdate);
    }

    @Transactional
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentView updateDocument3(DocumentUpdateView documentUpdate) {
        return updateDocument0(documentUpdate);
    }

    private DocumentView updateDocument0(DocumentUpdateView documentUpdateView) {
        evm.save(emHolder.getEntityManager(), documentUpdateView);
        return evm.find(emHolder.getEntityManager(), DocumentView.class, documentUpdateView.getId());
    }
}
