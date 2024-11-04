/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.entity.simple;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@Entity
@Table(name = "doc_coll")
public class DocumentForCollections implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private PersonForCollections owner;
    private Set<PersonForCollections> partners = new HashSet<PersonForCollections>();
    private Map<Integer, PersonForCollections> contacts = new HashMap<Integer, PersonForCollections>();
    private List<PersonForCollections> personList = new ArrayList<PersonForCollections>();

    public DocumentForCollections() {
    }

    public DocumentForCollections(String name) {
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    public PersonForCollections getOwner() {
        return owner;
    }

    public void setOwner(PersonForCollections owner) {
        this.owner = owner;
    }

    @OneToMany(mappedBy = "partnerDocument")
    public Set<PersonForCollections> getPartners() {
        return partners;
    }

    public void setPartners(Set<PersonForCollections> partners) {
        this.partners = partners;
    }

    @OneToMany
    @MapKeyColumn(name = "doc_coll_contacts_key", nullable = false)
    @JoinTable(name = "doc_coll_contacts", joinColumns = @JoinColumn(name = "id"), inverseJoinColumns = @JoinColumn(name = "person_id"))
    public Map<Integer, PersonForCollections> getContacts() {
        return contacts;
    }

    public void setContacts(Map<Integer, PersonForCollections> contacts) {
        this.contacts = contacts;
    }

    @OneToMany
    @OrderColumn(name = "position", nullable = false)
    @JoinTable(name = "personlist", joinColumns = @JoinColumn(name = "id"), inverseJoinColumns = @JoinColumn(name = "person_id"))
    public List<PersonForCollections> getPersonList() {
        return personList;
    }

    public void setPersonList(List<PersonForCollections> personList) {
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
        DocumentForCollections other = (DocumentForCollections) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
