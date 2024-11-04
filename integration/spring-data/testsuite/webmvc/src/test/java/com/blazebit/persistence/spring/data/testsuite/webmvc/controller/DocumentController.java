/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.testsuite.webmvc.controller;

import com.blazebit.persistence.spring.data.repository.KeysetPageable;
import com.blazebit.persistence.spring.data.testsuite.webmvc.entity.Document;
import com.blazebit.persistence.spring.data.testsuite.webmvc.repository.DocumentRepository;
import com.blazebit.persistence.spring.data.testsuite.webmvc.repository.ReadOnlyDocumentViewRepository;
import com.blazebit.persistence.spring.data.testsuite.webmvc.view.DocumentCreateOrUpdateView;
import com.blazebit.persistence.spring.data.testsuite.webmvc.view.DocumentUpdateView;
import com.blazebit.persistence.spring.data.testsuite.webmvc.view.DocumentView;
import com.blazebit.persistence.spring.data.webmvc.EntityViewId;
import com.blazebit.persistence.spring.data.webmvc.KeysetConfig;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@RestController
public class DocumentController {

    public static final String APPLICATION_VND_BLAZEBIT_UPDATE_1_JSON = "application/vnd.blazebit.update1+json";
    public static final String APPLICATION_VND_BLAZEBIT_UPDATE_2_JSON = "application/vnd.blazebit.update2+json";
    public static final String APPLICATION_VND_BLAZEBIT_UPDATE_3_JSON = "application/vnd.blazebit.update3+json";
    private final ReadOnlyDocumentViewRepository readOnlyDocumentViewRepository;
    private final DocumentRepository documentRepository;

    @Autowired
    public DocumentController(ReadOnlyDocumentViewRepository readOnlyDocumentViewRepository, DocumentRepository documentRepository) {
        this.readOnlyDocumentViewRepository = readOnlyDocumentViewRepository;
        this.documentRepository = documentRepository;
    }

    @GetMapping("/documents")
    public Page<DocumentView> getDocuments(@KeysetConfig(Document.class) @PageableDefault(sort = "id") KeysetPageable keysetPageable) {
        return readOnlyDocumentViewRepository.findAll(null, keysetPageable);
    }

    @PutMapping(
            value = "/documents/{id1}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = APPLICATION_VND_BLAZEBIT_UPDATE_1_JSON
    )
    public ResponseEntity<DocumentView> updateDocument1(@EntityViewId("id1") @RequestBody DocumentUpdateView documentUpdate) {
        return updateDocument0(documentUpdate);
    }

    @PutMapping(
            value = "/documents/{id2}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = APPLICATION_VND_BLAZEBIT_UPDATE_2_JSON
    )
    public ResponseEntity<DocumentView> updateDocument2(@PathVariable(value = "id2") Long idParam, @EntityViewId("id2") @RequestBody DocumentUpdateView documentUpdate) {
        Objects.requireNonNull(idParam);
        return updateDocument0(documentUpdate);
    }

    @PutMapping(
        value = "/documents/{id1}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = APPLICATION_VND_BLAZEBIT_UPDATE_3_JSON
    )
    public ResponseEntity<DocumentView> updateDocument3(@EntityViewId("id1") @RequestBody DocumentCreateOrUpdateView documentUpdate) {
        return updateDocument0(documentUpdate);
    }

    @PutMapping(
            value = "/documents",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<DocumentView> updateDocument3(@RequestBody DocumentUpdateView documentUpdate) {
        return updateDocument0(documentUpdate);
    }

    @PostMapping(
            value = "/documents",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<DocumentView> createDocument(@RequestBody DocumentCreateOrUpdateView document) {
        return createDocument0(document);
    }

    private ResponseEntity<DocumentView> createDocument0(DocumentCreateOrUpdateView documentUpdate) {
        documentRepository.createDocument(documentUpdate);
        DocumentView documentView = readOnlyDocumentViewRepository.findOne(documentUpdate.getId());
        return ResponseEntity.ok(documentView);
    }

    private ResponseEntity<DocumentView> updateDocument0(DocumentCreateOrUpdateView documentUpdate) {
        documentRepository.updateDocument(documentUpdate);
        DocumentView documentView = readOnlyDocumentViewRepository.findOne(documentUpdate.getId());
        return ResponseEntity.ok(documentView);
    }

    private ResponseEntity<DocumentView> updateDocument0(DocumentUpdateView documentUpdate) {
        documentRepository.updateDocument(documentUpdate);
        DocumentView documentView = readOnlyDocumentViewRepository.findOne(documentUpdate.getId());
        return ResponseEntity.ok(documentView);
    }
}
