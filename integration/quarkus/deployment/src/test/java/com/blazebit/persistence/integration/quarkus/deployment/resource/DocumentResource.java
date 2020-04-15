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
