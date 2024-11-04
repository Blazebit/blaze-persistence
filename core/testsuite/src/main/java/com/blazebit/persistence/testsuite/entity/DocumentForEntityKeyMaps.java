/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@Entity
@Table(name = "doc_map")
public class DocumentForEntityKeyMaps {

    private Long id;
    private String name;
    private Map<PersonForEntityKeyMaps, DocumentForEntityKeyMaps> contactDocuments = new HashMap<>();

    public DocumentForEntityKeyMaps() {
    }

    public DocumentForEntityKeyMaps(String name) {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToMany
    @MapKeyJoinColumn(name = "person_id", nullable = false)
    @JoinTable(name = "doc_coll_contact_docs", joinColumns = @JoinColumn(name = "parent_doc_id"), inverseJoinColumns = @JoinColumn(name = "doc_id"))
    public Map<PersonForEntityKeyMaps, DocumentForEntityKeyMaps> getContactDocuments() {
        return contactDocuments;
    }

    public void setContactDocuments(Map<PersonForEntityKeyMaps, DocumentForEntityKeyMaps> contactDocuments) {
        this.contactDocuments = contactDocuments;
    }
}
