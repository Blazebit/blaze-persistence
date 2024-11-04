/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.entity.extended;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@Entity
public class ExtendedPersonForCollections implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private ExtendedDocumentForCollections partnerDocument;
    private Set<ExtendedDocumentForCollections> ownedDocuments = new HashSet<ExtendedDocumentForCollections>();

    public ExtendedPersonForCollections() {
    }

    public ExtendedPersonForCollections(String name) {
        this.name = name;
    }

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    public ExtendedDocumentForCollections getPartnerDocument() {
        return partnerDocument;
    }

    public void setPartnerDocument(ExtendedDocumentForCollections partnerDocument) {
        this.partnerDocument = partnerDocument;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(mappedBy = "owner")
    public Set<ExtendedDocumentForCollections> getOwnedDocuments() {
        return ownedDocuments;
    }

    public void setOwnedDocuments(Set<ExtendedDocumentForCollections> ownedDocuments) {
        this.ownedDocuments = ownedDocuments;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExtendedPersonForCollections other = (ExtendedPersonForCollections) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
