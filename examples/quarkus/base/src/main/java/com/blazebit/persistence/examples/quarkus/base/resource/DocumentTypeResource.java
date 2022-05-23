/*
 * Copyright 2014 - 2022 Blazebit.
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
