/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.quarkus.deployment.resource;

import com.blazebit.persistence.integration.quarkus.deployment.view.DocumentCreateView;
import com.blazebit.persistence.integration.quarkus.deployment.view.DocumentView;
import com.blazebit.persistence.view.EntityViewManager;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
