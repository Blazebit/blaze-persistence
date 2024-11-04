/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.timeentity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author Christian Beikov
 * @since 1.6.11
 */
@Entity
@Table(name = "pers_multiset")
public class PersonForMultisetFetch implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private DocumentForMultisetFetch partnerDocument;
    private Set<DocumentForMultisetFetch> ownedDocuments = new HashSet<>();
    private Set<PersonForMultisetFetch> someCollection = new HashSet<>();

    public PersonForMultisetFetch() {
    }

    public PersonForMultisetFetch(String name) {
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
    public DocumentForMultisetFetch getPartnerDocument() {
        return partnerDocument;
    }

    public void setPartnerDocument(DocumentForMultisetFetch partnerDocument) {
        this.partnerDocument = partnerDocument;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(mappedBy = "owner")
    public Set<DocumentForMultisetFetch> getOwnedDocuments() {
        return ownedDocuments;
    }

    public void setOwnedDocuments(Set<DocumentForMultisetFetch> ownedDocuments) {
        this.ownedDocuments = ownedDocuments;
    }

    @OneToMany
    @JoinTable(name = "pers_coll_some_coll")
    public Set<PersonForMultisetFetch> getSomeCollection() {
        return someCollection;
    }

    public void setSomeCollection(Set<PersonForMultisetFetch> someCollection) {
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
        if (!(obj instanceof PersonForMultisetFetch )) {
            return false;
        }
        PersonForMultisetFetch other = (PersonForMultisetFetch) obj;
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
