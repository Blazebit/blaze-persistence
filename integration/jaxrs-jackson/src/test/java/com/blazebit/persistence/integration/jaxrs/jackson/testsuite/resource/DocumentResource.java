/*
 * Copyright 2014 - 2021 Blazebit.
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

package com.blazebit.persistence.integration.jaxrs.jackson.testsuite.resource;

import com.blazebit.persistence.integration.jaxrs.EntityViewId;
import com.blazebit.persistence.integration.jaxrs.jackson.testsuite.config.EntityManagerHolder;
import com.blazebit.persistence.integration.jaxrs.jackson.testsuite.view.DocumentUpdateView;
import com.blazebit.persistence.integration.jaxrs.jackson.testsuite.view.DocumentView;
import com.blazebit.persistence.view.EntityViewManager;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Objects;

/**
 * @author Moritz Becker
 * @since 1.5.0
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
