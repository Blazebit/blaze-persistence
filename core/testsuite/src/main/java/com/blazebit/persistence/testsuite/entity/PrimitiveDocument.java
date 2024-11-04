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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Entity
@Table(name = "prim_doc")
public class PrimitiveDocument implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String name;
    private boolean deleted;
    private PrimitivePerson owner;
    private Set<PrimitivePerson> partners = new HashSet<PrimitivePerson>();
    private Map<Integer, PrimitivePerson> contacts = new HashMap<Integer, PrimitivePerson>();
    private List<PrimitivePerson> people = new ArrayList<PrimitivePerson>();
    private List<PrimitivePerson> peopleListBag = new ArrayList<PrimitivePerson>();
    private Collection<PrimitivePerson> peopleCollectionBag = new ArrayList<PrimitivePerson>();
    private PrimitiveDocument parent;
    private PrimitiveVersion version;

    public PrimitiveDocument() {
    }

    public PrimitiveDocument(String name) {
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

    @Basic(optional = false)
    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    public PrimitivePerson getOwner() {
        return owner;
    }

    public void setOwner(PrimitivePerson owner) {
        this.owner = owner;
    }

    @OneToMany(mappedBy = "partnerDocument")
    public Set<PrimitivePerson> getPartners() {
        return partners;
    }

    public void setPartners(Set<PrimitivePerson> partners) {
        this.partners = partners;
    }

    @OneToMany
    @JoinTable(name = "prim_contacts")
    @MapKeyColumn(nullable = false)
    public Map<Integer, PrimitivePerson> getContacts() {
        return contacts;
    }

    public void setContacts(Map<Integer, PrimitivePerson> localized) {
        this.contacts = localized;
    }

    @OneToMany
    @OrderColumn(name = "people_idx", nullable = false)
    @JoinTable(name = "prim_document_people")
    public List<PrimitivePerson> getPeople() {
        return people;
    }

    public void setPeople(List<PrimitivePerson> people) {
        this.people = people;
    }

    @OneToMany
    @JoinTable(name = "prim_document_people_list_bag")
    public List<PrimitivePerson> getPeopleListBag() {
        return peopleListBag;
    }

    public void setPeopleListBag(List<PrimitivePerson> peopleListBag) {
        this.peopleListBag = peopleListBag;
    }

    @OneToMany
    @JoinTable(name = "prim_document_people_coll_bag")
    public Collection<PrimitivePerson> getPeopleCollectionBag() {
        return peopleCollectionBag;
    }

    public void setPeopleCollectionBag(Collection<PrimitivePerson> peopleCollectionBag) {
        this.peopleCollectionBag = peopleCollectionBag;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    public PrimitiveDocument getParent() {
        return parent;
    }

    public void setParent(PrimitiveDocument parent) {
        this.parent = parent;
    }

    @OneToOne(mappedBy = "document", cascade = CascadeType.ALL)
    public PrimitiveVersion getVersion() {
        return version;
    }

    public void setVersion(PrimitiveVersion version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PrimitiveDocument)) {
            return false;
        }

        PrimitiveDocument that = (PrimitiveDocument) o;

        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return (int) (getId() ^ (getId() >>> 32));
    }
}
