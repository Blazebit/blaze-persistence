/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import com.blazebit.persistence.CTE;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import java.io.Serializable;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
@Entity
@CTE
public class DocumentCTE implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;

    private Document document;

    private Document parent;

    private Document root;

    @Id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @OneToOne
    @PrimaryKeyJoinColumn
    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    @ManyToOne
    public Document getParent() {
        return parent;
    }

    public void setParent(Document parent) {
        this.parent = parent;
    }

    @ManyToOne
    public Document getRoot() {
        return root;
    }

    public void setRoot(Document root) {
        this.root = root;
    }
}
