/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.quarkus.deployment.resource;

import com.blazebit.persistence.integration.quarkus.deployment.view.DocumentCreateView;
import com.blazebit.persistence.integration.quarkus.deployment.view.DocumentView;
import com.blazebit.persistence.view.EntityViewManager;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@Path("documents")
public class DocumentResource {

    @Inject
    EntityManager entityManager;

    @Inject
    EntityViewManager entityViewManager;

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentView add(DocumentCreateView view) {
        entityViewManager.save(entityManager, view);
        return entityViewManager.find(entityManager, DocumentView.class, view.getId());
    }
}
