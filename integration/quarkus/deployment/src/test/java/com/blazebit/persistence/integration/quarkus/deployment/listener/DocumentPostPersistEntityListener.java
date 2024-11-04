/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.quarkus.deployment.listener;

import com.blazebit.persistence.integration.quarkus.deployment.entity.Document;
import com.blazebit.persistence.integration.quarkus.deployment.view.DocumentCreateView;
import com.blazebit.persistence.view.EntityViewListener;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.PostPersistEntityListener;

import javax.persistence.EntityManager;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@EntityViewListener
public class DocumentPostPersistEntityListener implements PostPersistEntityListener<DocumentCreateView, Document> {

    public static volatile int PERSIST_COUNTER = 0;

    @Override
    public void postPersist(EntityViewManager entityViewManager, EntityManager entityManager, DocumentCreateView view, Document entity) {
        PERSIST_COUNTER++;
    }
}
