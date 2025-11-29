/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.io.Serializable;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Entity
public class DocumentInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private DocumentForOneToOne document;
    private DocumentForOneToOne document2;
    private String someInfo;

    public DocumentInfo() {
    }

    public DocumentInfo(Long id, DocumentForOneToOne document, String someInfo) {
        this.id = id;
        this.document = document;
        this.document2 = document;
        this.someInfo = someInfo;
        document.setDocumentInfo(this);
        document.setDocumentInfo2(this);
    }

    @Id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    public DocumentForOneToOne getDocument() {
        return document;
    }

    public void setDocument(DocumentForOneToOne document) {
        this.document = document;
    }

    @OneToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "document2_id", nullable = false)
    public DocumentForOneToOne getDocument2() {
        return document2;
    }

    public void setDocument2(DocumentForOneToOne document2) {
        this.document2 = document2;
    }

    public String getSomeInfo() {
        return someInfo;
    }

    public void setSomeInfo(String someInfo) {
        this.someInfo = someInfo;
    }
}
