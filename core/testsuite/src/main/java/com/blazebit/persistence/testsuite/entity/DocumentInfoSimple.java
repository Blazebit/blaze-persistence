/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.io.Serializable;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@Entity
public class DocumentInfoSimple implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private DocumentForSimpleOneToOne document;
    private String someInfo;

    public DocumentInfoSimple() {
    }

    public DocumentInfoSimple(Long id, DocumentForSimpleOneToOne document, String someInfo) {
        this.id = id;
        this.document = document;
        this.someInfo = someInfo;
        document.setDocumentInfo(this);
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
    public DocumentForSimpleOneToOne getDocument() {
        return document;
    }

    public void setDocument(DocumentForSimpleOneToOne document) {
        this.document = document;
    }

    public String getSomeInfo() {
        return someInfo;
    }

    public void setSomeInfo(String someInfo) {
        this.someInfo = someInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DocumentInfoSimple)) {
            return false;
        }

        DocumentInfoSimple that = (DocumentInfoSimple) o;

        return getId() != null ? getId().equals(that.getId()) : that.getId() == null;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }
}
