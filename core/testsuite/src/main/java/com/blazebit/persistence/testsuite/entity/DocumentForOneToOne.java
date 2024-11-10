/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import java.io.Serializable;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Entity
public class DocumentForOneToOne extends Ownable implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private DocumentInfo documentInfo;
    private DocumentInfo documentInfo2;

    public DocumentForOneToOne() {
    }

    public DocumentForOneToOne(String name) {
        this.name = name;
    }

    public DocumentForOneToOne(String name, Person owner) {
        this.name = name;
        setOwner(owner);
    }

    @Basic(optional = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToOne(mappedBy = "document")
    public DocumentInfo getDocumentInfo() {
        return documentInfo;
    }

    public void setDocumentInfo(DocumentInfo documentInfo) {
        this.documentInfo = documentInfo;
    }

    @OneToOne(mappedBy = "document2", optional = false, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    public DocumentInfo getDocumentInfo2() {
        return documentInfo2;
    }

    public void setDocumentInfo2(DocumentInfo documentInfo2) {
        this.documentInfo2 = documentInfo2;
    }
}
