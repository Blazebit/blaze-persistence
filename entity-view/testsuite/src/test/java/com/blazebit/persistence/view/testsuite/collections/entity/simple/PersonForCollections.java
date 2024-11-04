/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.entity.simple;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@Entity
@Table(name = "pers_coll")
public class PersonForCollections implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private DocumentForCollections partnerDocument;
    private Set<DocumentForCollections> ownedDocuments = new HashSet<>();
    private Set<PersonForCollections> someCollection = new HashSet<>();

    public PersonForCollections() {
    }

    public PersonForCollections(String name) {
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
    @JoinColumn(name = "partner_doc_id")
    public DocumentForCollections getPartnerDocument() {
        return partnerDocument;
    }

    public void setPartnerDocument(DocumentForCollections partnerDocument) {
        this.partnerDocument = partnerDocument;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(mappedBy = "owner")
    public Set<DocumentForCollections> getOwnedDocuments() {
        return ownedDocuments;
    }

    public void setOwnedDocuments(Set<DocumentForCollections> ownedDocuments) {
        this.ownedDocuments = ownedDocuments;
    }

    @OneToMany
    @JoinTable(name = "pers_coll_some_coll")
    public Set<PersonForCollections> getSomeCollection() {
        return someCollection;
    }

    public void setSomeCollection(Set<PersonForCollections> someCollection) {
        this.someCollection = someCollection;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PersonForCollections)) {
            return false;
        }
        PersonForCollections other = (PersonForCollections) obj;
        if (getId() == null) {
            if (other.getId() != null) {
                return false;
            }
        } else if (!getId().equals(other.getId())) {
            return false;
        }
        return true;
    }
}
