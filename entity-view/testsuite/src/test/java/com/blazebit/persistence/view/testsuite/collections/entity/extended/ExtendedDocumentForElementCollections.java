/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.entity.extended;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@Entity
@Table(name = "ext_doc_elem_coll")
public class ExtendedDocumentForElementCollections implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private ExtendedPersonForElementCollections owner = new ExtendedPersonForElementCollections();
    private Set<ExtendedPersonForElementCollections> partners = new HashSet<ExtendedPersonForElementCollections>();
    private Map<Integer, ExtendedPersonForElementCollections> contacts = new HashMap<Integer, ExtendedPersonForElementCollections>();
    private List<ExtendedPersonForElementCollections> personList = new ArrayList<ExtendedPersonForElementCollections>();

    private DocumentExtensionForElementCollections extension = new DocumentExtensionForElementCollections();

    public ExtendedDocumentForElementCollections() {
    }

    public ExtendedDocumentForElementCollections(String name) {
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

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Embedded
    public DocumentExtensionForElementCollections getExtension() {
        return extension;
    }

    public void setExtension(DocumentExtensionForElementCollections extension) {
        this.extension = extension;
    }

    @Embedded
    public ExtendedPersonForElementCollections getOwner() {
        return owner;
    }

    public void setOwner(ExtendedPersonForElementCollections owner) {
        this.owner = owner;
    }

    // NOTE: If we don't specify the join column, hibernate will generate a wrong column
    @ElementCollection
    @CollectionTable(name = "embeddable_partners", joinColumns = @JoinColumn(name = "id"))
    public Set<ExtendedPersonForElementCollections> getPartners() {
        return partners;
    }

    public void setPartners(Set<ExtendedPersonForElementCollections> partners) {
        this.partners = partners;
    }

    @ElementCollection
    @MapKeyColumn(name = "embeddable_contacts_key", nullable = false)
    @CollectionTable(name = "embeddable_contacts", joinColumns = @JoinColumn(name = "id"))
    public Map<Integer, ExtendedPersonForElementCollections> getContacts() {
        return contacts;
    }

    public void setContacts(Map<Integer, ExtendedPersonForElementCollections> contacts) {
        this.contacts = contacts;
    }

    // NOTE: If we don't specify the join column, hibernate will generate a wrong column
    @ElementCollection
    @OrderColumn(name = "position", nullable = false)
    @CollectionTable(name = "embeddable_personlist", joinColumns = @JoinColumn(name = "id"))
    public List<ExtendedPersonForElementCollections> getPersonList() {
        return personList;
    }

    public void setPersonList(List<ExtendedPersonForElementCollections> personList) {
        this.personList = personList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExtendedDocumentForElementCollections)) {
            return false;
        }

        ExtendedDocumentForElementCollections that = (ExtendedDocumentForElementCollections) o;

        return getId() != null ? getId().equals(that.getId()) : that.getId() == null;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }
}
