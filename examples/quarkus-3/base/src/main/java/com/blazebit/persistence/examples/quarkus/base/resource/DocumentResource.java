/*
 * Copyright 2014 - 2024 Blazebit.
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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.examples.quarkus.base.entity.Document;
import com.blazebit.persistence.examples.quarkus.base.view.DocumentUpdateView;
import com.blazebit.persistence.examples.quarkus.base.view.DocumentView;
import com.blazebit.persistence.examples.quarkus.base.view.DocumentWithJsonIgnoredNameView;
import com.blazebit.persistence.integration.jaxrs.EntityViewId;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@Path("documents")
public class DocumentResource {

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
    public DocumentView updateDocument(@EntityViewId("id") DocumentUpdateView documentUpdateView) {
        evm.save(em, documentUpdateView);
        return evm.find(em, DocumentView.class, documentUpdateView.getId());
    }

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    public Response addDocument(DocumentUpdateView view) {
        evm.save(em, view);
        return Response.created(URI.create("/documents/" + view.getId())).build();
    }


    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    public List<DocumentView> getDocuments(@QueryParam("age") List<Long> ages) {
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class).where("age").in().fromValues(Long.class, "age", ages).end();
        return evm.applySetting(EntityViewSetting.create(DocumentView.class), cb).getResultList();
    }

    @GET
    @Transactional
    @Produces("application/vnd.blazebit.noname+json")
    public List<DocumentWithJsonIgnoredNameView> getDocumentsWithJsonIgnoredName(@QueryParam("age") List<Long> ages) {
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class).where("age").in().fromValues(Long.class, "age", ages).end();
        return evm.applySetting(EntityViewSetting.create(DocumentWithJsonIgnoredNameView.class), cb).getResultList();
    }

    @DELETE
    @Transactional
    public Response clearDocuments() {
        cbf.delete(em, Document.class).executeUpdate();
        return Response.ok().build();
    }
}
