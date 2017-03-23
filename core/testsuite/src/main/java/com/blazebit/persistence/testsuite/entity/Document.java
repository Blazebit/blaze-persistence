/*
 * Copyright 2014 - 2017 Blazebit.
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
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
@Entity
public class Document extends Ownable implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private Object someTransientField;
    private NameObject nameObject;
    private Set<Version> versions = new HashSet<Version>();
    private Set<Person> partners = new HashSet<Person>();
//    private Person owner;
    private IntIdEntity intIdEntity;
    private long age;
    private int idx;
    private byte[] byteArray;
    private Byte[] wrappedByteArray;
    private Double someValue;
    private String nonJoinable;
    private Map<Integer, Person> contacts = new HashMap<Integer, Person>();
    private List<Person> people = new ArrayList<Person>();
    private List<Person> peopleListBag = new ArrayList<Person>();
    private Collection<Person> peopleCollectionBag = new ArrayList<Person>();
    private Calendar creationDate;
    private Calendar creationDate2;
    private Date lastModified;
    private Date lastModified2;
    private DocumentType documentType;
    private Boolean archived = false;
    private Document parent;

    public Document() {
    }

    public Document(Long id) {
        super(id);
    }

    public Document(String name) {
        this.name = name;
    }

    public Document(String name, long age) {
        this.name = name;
        this.age = age;
    }
    
    public Document(String name, DocumentType documentType) {
        this(name);
        this.documentType = documentType;
    }

    public Document(String name, Person owner, Version... versions) {
        this(name);
        setOwner(owner);
        this.versions.addAll(Arrays.asList(versions));
        for (Version v : versions) {
            v.setDocument(this);
        }
    }

    @Basic(optional = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Transient
    public Object getSomeTransientField() {
        return someTransientField;
    }

    public void setSomeTransientField(Object someTransientField) {
        this.someTransientField = someTransientField;
    }

    @Embedded
    public NameObject getNameObject() {
        return nameObject;
    }

    public void setNameObject(NameObject nameObject) {
        this.nameObject = nameObject;
    }

    @OneToMany(mappedBy = "document")
    public Set<Version> getVersions() {
        return versions;
    }

    public void setVersions(Set<Version> versions) {
        this.versions = versions;
    }

    public long getAge() {
        return age;
    }

    public void setAge(long age) {
        this.age = age;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int index) {
        this.idx = index;
    }

    public byte[] getByteArray() {
        return byteArray;
    }

    public void setByteArray(byte[] byteArray) {
        this.byteArray = byteArray;
    }

    public Byte[] getWrappedByteArray() {
        return wrappedByteArray;
    }

    public void setWrappedByteArray(Byte[] wrappedByteArray) {
        this.wrappedByteArray = wrappedByteArray;
    }

    @OneToMany(mappedBy = "partnerDocument")
    public Set<Person> getPartners() {
        return partners;
    }

    public void setPartners(Set<Person> partners) {
        this.partners = partners;
    }

//    @Override
//    @ManyToOne(optional = false)
//    public Person getOwner() {
//        return owner;
//    }
//
//    @Override
//    public void setOwner(Person owner) {
//        this.owner = owner;
//    }

    @ManyToOne(fetch = FetchType.LAZY)
    public IntIdEntity getIntIdEntity() {
        return intIdEntity;
    }
    
    public void setIntIdEntity(IntIdEntity intIdEntity) {
        this.intIdEntity = intIdEntity;
    }

    public Double getSomeValue() {
        return someValue;
    }

    public void setSomeValue(Double someValue) {
        this.someValue = someValue;
    }

    public String getNonJoinable() {
        return nonJoinable;
    }

    public void setNonJoinable(String nonJoinable) {
        this.nonJoinable = nonJoinable;
    }

    @OneToMany
    @JoinTable(name = "contacts")
    @MapKeyColumn(nullable = false)
    public Map<Integer, Person> getContacts() {
        return contacts;
    }

    public void setContacts(Map<Integer, Person> localized) {
        this.contacts = localized;
    }

    @OneToMany
    @OrderColumn(name = "people_idx", nullable = false)
    @JoinTable(name = "document_people")
    public List<Person> getPeople() {
        return people;
    }

    public void setPeople(List<Person> people) {
        this.people = people;
    }

    @OneToMany
    public List<Person> getPeopleListBag() {
        return peopleListBag;
    }

    public void setPeopleListBag(List<Person> peopleListBag) {
        this.peopleListBag = peopleListBag;
    }

    @OneToMany
    public Collection<Person> getPeopleCollectionBag() {
        return peopleCollectionBag;
    }

    public void setPeopleCollectionBag(Collection<Person> peopleCollectionBag) {
        this.peopleCollectionBag = peopleCollectionBag;
    }

    @Temporal(TemporalType.DATE)
    public Calendar getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Calendar creationDate) {
        this.creationDate = creationDate;
    }

    @Temporal(TemporalType.DATE)
    public Calendar getCreationDate2() {
        return creationDate2;
    }

    public void setCreationDate2(Calendar creationDate2) {
        this.creationDate2 = creationDate2;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getLastModified2() {
        return lastModified2;
    }

    public void setLastModified2(Date lastModified2) {
        this.lastModified2 = lastModified2;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public Boolean isArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    public Document getParent() {
        return parent;
    }

    public void setParent(Document parent) {
        this.parent = parent;
    }

}
