/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 *
 * @author Christian Beikov
 * @since 1.6.8
 */
@Entity
@Table(name = "prim_version")
public class PrimitiveVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    private long versionId;
    private PrimitiveDocument document;

    public PrimitiveVersion() {
    }

    @Id
    public long getVersionId() {
        return versionId;
    }

    public void setVersionId(long id) {
        this.versionId = id;
    }

    @OneToOne(fetch = FetchType.LAZY)
    public PrimitiveDocument getDocument() {
        return document;
    }

    public void setDocument(PrimitiveDocument document) {
        this.document = document;
    }

}
