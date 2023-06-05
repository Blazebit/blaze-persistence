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

package com.blazebit.persistence.spring.data.testsuite.webmvc.repository;

import com.blazebit.persistence.spring.data.testsuite.webmvc.view.DocumentCreateOrUpdateView;
import com.blazebit.persistence.spring.data.testsuite.webmvc.view.DocumentUpdateView;
import com.blazebit.persistence.view.EntityViewManager;
import javax.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@Repository
@Transactional
public class DocumentRepository {

    private final EntityManager em;
    private final EntityViewManager evm;

    public DocumentRepository(EntityManager em, EntityViewManager evm) {
        this.em = em;
        this.evm = evm;
    }

    public void updateDocument(DocumentUpdateView documentUpdateView) {
        evm.save(em, documentUpdateView);
    }

    public void createDocument(DocumentCreateOrUpdateView documentCreateView) {
        evm.save(em, documentCreateView);
    }
}
