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
 * @since 1.6.0
 */
@Entity
public class DocumentHolder implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    // CHECKSTYLE:OFF: MemberName
    private Document Document;
    // CHECKSTYLE:ON: MemberName

    public DocumentHolder() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Document getDocument() {
        return Document;
    }

    public void setDocument(Document document) {
        this.Document = document;
    }

}
