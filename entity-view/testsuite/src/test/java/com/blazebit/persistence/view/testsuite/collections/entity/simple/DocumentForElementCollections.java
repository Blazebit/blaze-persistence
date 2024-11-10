/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.entity.simple;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@Entity
@Table(name = "doc_elem_coll")
public class DocumentForElementCollections implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private PersonForElementCollections owner;
    private Set<PersonForElementCollections> partners = new HashSet<PersonForElementCollections>();
    private Map<Integer, PersonForElementCollections> contacts = new HashMap<Integer, PersonForElementCollections>();
    private List<PersonForElementCollections> personList = new ArrayList<PersonForElementCollections>();

    public DocumentForElementCollections() {
    }

    public DocumentForElementCollections(String name) {
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
    public PersonForElementCollections getOwner() {
        return owner;
    }

    public void setOwner(PersonForElementCollections owner) {
        this.owner = owner;
    }

    // NOTE: If we don't specify the join column, hibernate will generate a wrong column
    @ElementCollection
    @CollectionTable(name = "embeddable_partners", joinColumns = @JoinColumn(name = "id"))
    public Set<PersonForElementCollections> getPartners() {
        return partners;
    }

    public void setPartners(Set<PersonForElementCollections> partners) {
        this.partners = partners;
    }

    @ElementCollection
    @MapKeyColumn(name = "embeddable_contacts_key", nullable = false)
    @CollectionTable(name = "embeddable_contacts", joinColumns = @JoinColumn(name = "id"))
    public Map<Integer, PersonForElementCollections> getContacts() {
        return contacts;
    }

    public void setContacts(Map<Integer, PersonForElementCollections> contacts) {
        this.contacts = contacts;
    }

    // NOTE: If we don't specify the join column, hibernate will generate a wrong column
    @ElementCollection
    @OrderColumn(name = "position", nullable = false)
    @CollectionTable(name = "embeddable_personlist", joinColumns = @JoinColumn(name = "id"))
    public List<PersonForElementCollections> getPersonList() {
        return personList;
    }

    public void setPersonList(List<PersonForElementCollections> personList) {
        this.personList = personList;
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
        DocumentForElementCollections other = (DocumentForElementCollections) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
