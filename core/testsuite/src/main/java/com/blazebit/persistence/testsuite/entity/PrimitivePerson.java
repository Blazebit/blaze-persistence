/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blazebit.persistence.testsuite.entity;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
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

    @OneToMany(mappedBy = "owner")
    public Set<PrimitiveDocument> getOwnedDocuments() {
        return ownedDocuments;
    }

    public void setOwnedDocuments(Set<PrimitiveDocument> ownedDocuments) {
        this.ownedDocuments = ownedDocuments;
    }
    
}
