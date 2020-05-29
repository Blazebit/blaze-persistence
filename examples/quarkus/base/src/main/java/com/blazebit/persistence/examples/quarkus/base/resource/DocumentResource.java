/*
 * Copyright 2014 - 2020 Blazebit.
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
import com.blazebit.persistence.integration.jaxrs.EntityViewId;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@Path("documents")
public class DocumentResource {

    @Inject
    EntityManager em;
    @Inject
    EntityViewManager evm;
    @Inject
    CriteriaBuilderFactory cbf;

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
}
