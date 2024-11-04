/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToOne;
import java.io.Serializable;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Entity
public class DocumentForOneToOneJoinTable extends Ownable implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private DocumentInfo documentInfoJoinTable;

    @Basic(optional = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToOne
    @JoinTable(
            name = "document_extra",
            joinColumns = @JoinColumn(name = "document_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "document_info_id", referencedColumnName = "id")
    )
    public DocumentInfo getDocumentInfoJoinTable() {
        return documentInfoJoinTable;
    }

    public void setDocumentInfoJoinTable(DocumentInfo documentInfoJoinTable) {
        this.documentInfoJoinTable = documentInfoJoinTable;
    }
}
