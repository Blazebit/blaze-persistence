/*
 * Copyright 2014 - 2019 Blazebit.
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

package com.blazebit.persistence.spring.data.testsuite.controller;

import com.blazebit.persistence.spring.data.repository.KeysetPageable;
import com.blazebit.persistence.spring.data.rest.KeysetConfig;
import com.blazebit.persistence.spring.data.testsuite.entity.Document;
import com.blazebit.persistence.spring.data.testsuite.repository.DocumentViewRepository;
import com.blazebit.persistence.spring.data.testsuite.view.DocumentView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Moritz Becker
 * @since 1.4.0
 */
@RestController
public class DocumentController {

    private final DocumentViewRepository documentViewRepository;

    @Autowired
    public DocumentController(DocumentViewRepository documentViewRepository) {
        this.documentViewRepository = documentViewRepository;
    }

    @GetMapping("/document-views")
    public Page<DocumentView> getDocuments(@KeysetConfig(Document.class) @PageableDefault(sort = "id") KeysetPageable keysetPageable) {
        return documentViewRepository.findAll(null, keysetPageable);
    }
}
