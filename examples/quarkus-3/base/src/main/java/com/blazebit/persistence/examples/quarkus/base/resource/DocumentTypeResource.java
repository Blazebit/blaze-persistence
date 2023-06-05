/*
 * Copyright 2014 - 2023 Blazebit.
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
