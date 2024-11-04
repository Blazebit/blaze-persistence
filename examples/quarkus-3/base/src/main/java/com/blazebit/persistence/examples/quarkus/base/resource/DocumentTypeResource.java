/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.examples.quarkus.base.resource;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.examples.quarkus.base.entity.DocumentType;
import com.blazebit.persistence.examples.quarkus.base.view.DocumentTypeCreateView;
import com.blazebit.persistence.examples.quarkus.base.view.DocumentTypeUpdateView;
import com.blazebit.persistence.examples.quarkus.base.view.DocumentTypeView;
import com.blazebit.persistence.integration.jaxrs.EntityViewId;
import com.blazebit.persistence.view.EntityViewManager;

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
import java.net.URI;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@Path("document-types")
public class DocumentTypeResource {

    @Inject
    private EntityManager em;
    @Inject
    private EntityViewManager evm;
    @Inject
    private CriteriaBuilderFactory cbf;

    @Transactional
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createDocumentType(DocumentTypeCreateView documentTypeCreateView) {
        evm.save(em, documentTypeCreateView);
        return Response.created(URI.create("/document-types/" + documentTypeCreateView.getId())).build();
    }

    @Transactional
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentTypeView updateDocumentType(@EntityViewId("id") DocumentTypeUpdateView documentTypeUpdateView) {
        evm.save(em, documentTypeUpdateView);
        return evm.find(em, DocumentTypeView.class, documentTypeUpdateView.getId());
    }

    @DELETE
    @Transactional
    public Response clearDocumentTypes() {
        cbf.delete(em, DocumentType.class).executeUpdate();
        return Response.ok().build();
    }
}
