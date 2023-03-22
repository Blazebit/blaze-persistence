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
package com.blazebit.persistence.integration.quarkus.deployment.listener;

import com.blazebit.persistence.integration.quarkus.deployment.entity.Document;
import com.blazebit.persistence.integration.quarkus.deployment.view.DocumentCreateView;
import com.blazebit.persistence.view.EntityViewListener;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.PostPersistEntityListener;

import jakarta.persistence.EntityManager;

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
