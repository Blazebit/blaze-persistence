/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.entity.extended;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

/**
 *
 * @author Christian Beikov
 * @since 1.0.6
 */
@Embeddable
public class DocumentExtensionForElementCollections implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private ExtendedDocumentForElementCollections parent;
    private Set<ExtendedDocumentForElementCollections> childDocuments = new HashSet<ExtendedDocumentForElementCollections>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ext_doc_elem_coll_parent")
    public ExtendedDocumentForElementCollections getParent() {
        return parent;
    }

    public void setParent(ExtendedDocumentForElementCollections parent) {
        this.parent = parent;
    }

    @OneToMany(mappedBy = "extension.parent")
    public Set<ExtendedDocumentForElementCollections> getChildDocuments() {
        return childDocuments;
    }

    public void setChildDocuments(Set<ExtendedDocumentForElementCollections> childDocuments) {
        this.childDocuments = childDocuments;
    }
    
}