/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.io.Serializable;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@Entity
public class DocumentForSimpleOneToOne extends LongSequenceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private DocumentInfoSimple documentInfo;

    public DocumentForSimpleOneToOne() {
    }

    public DocumentForSimpleOneToOne(String name) {
        this.name = name;
    }

    @Basic(optional = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToOne(mappedBy = "document")
    public DocumentInfoSimple getDocumentInfo() {
        return documentInfo;
    }

    public void setDocumentInfo(DocumentInfoSimple documentInfo) {
        this.documentInfo = documentInfo;
    }
}
