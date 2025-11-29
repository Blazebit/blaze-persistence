/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.testsuite.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Entity
@Table(name = "prim_pers")
public class PrimitivePerson implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String name;
    private PrimitiveDocument partnerDocument;
    private Set<PrimitiveDocument> ownedDocuments = new HashSet<PrimitiveDocument>();

    public PrimitivePerson() {
    }

    public PrimitivePerson(String name) {
        this.name = name;
    }

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic(optional = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_doc_id")
    public PrimitiveDocument getPartnerDocument() {
        return partnerDocument;
    }

    public void setPartnerDocument(PrimitiveDocument partnerDocument) {
        this.partnerDocument = partnerDocument;
    }

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    public Set<PrimitiveDocument> getOwnedDocuments() {
        return ownedDocuments;
    }

    public void setOwnedDocuments(Set<PrimitiveDocument> ownedDocuments) {
        this.ownedDocuments = ownedDocuments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PrimitivePerson)) {
            return false;
        }

        PrimitivePerson that = (PrimitivePerson) o;

        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return (int) (getId() ^ (getId() >>> 32));
    }
}
