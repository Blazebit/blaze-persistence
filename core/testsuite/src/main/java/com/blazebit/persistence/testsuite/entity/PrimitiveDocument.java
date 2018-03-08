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
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
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

}
