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
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
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
 * @since 1.0.0
 */
@Entity
@Table(name = "document")
public class Document extends Ownable implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private Long version;
    private Object someTransientField;
    private NameObject nameObject = new NameObject();
    private NameObjectContainer nameContainer = new NameObjectContainer();
    private Set<Version> versions = new HashSet<>();
    private Set<Person> partners = new HashSet<>();
    private IntIdEntity intIdEntity;
    private long age;
    private int idx;
    private byte[] byteArray;
    private Byte[] wrappedByteArray;
    private Double someValue;
    private String nonJoinable;
    private Integer defaultContact;
    private Map<Integer, Person> contacts = new HashMap<>();
    private Map<Integer, Person> contacts2 = new HashMap<>();
    private List<Person> people = new ArrayList<>();
    private List<Person> peopleListBag = new ArrayList<>();
    private Collection<Person> peopleCollectionBag = new ArrayList<>();
    private List<String> strings = new ArrayList<>();
    private Map<String, String> stringMap = new HashMap<>();
    private List<NameObject> names = new ArrayList<>();
    private Map<String, NameObject> nameMap = new HashMap<>();
    private List<NameObjectContainer> nameContainers = new ArrayList<>();
    private Map<String, NameObjectContainer> nameContainerMap = new HashMap<>();
    private Calendar creationDate;
    private Calendar creationDate2;
    private Date lastModified;
    private Date lastModified2;
    private DocumentType documentType;
    private Boolean archived = false;
    private Document parent;
    private Person responsiblePerson;

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
    @Column(length = 30)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
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

    @Embedded
    public NameObjectContainer getNameContainer() {
        return nameContainer;
    }

    public void setNameContainer(NameObjectContainer nameContainer) {
        this.nameContainer = nameContainer;
    }

    @OneToMany(mappedBy = "document", cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
    public Set<Version> getVersions() {
        return versions;
    }

    public void setVersions(Set<Version> versions) {
        this.versions = versions;
    }

    @Basic(optional = false)
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

    @Column(length = 30)
    public byte[] getByteArray() {
        return byteArray;
    }

    public void setByteArray(byte[] byteArray) {
        this.byteArray = byteArray;
    }

    @Column(length = 30)
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

    @Column(length = 30)
    public String getNonJoinable() {
        return nonJoinable;
    }

    public void setNonJoinable(String nonJoinable) {
        this.nonJoinable = nonJoinable;
    }

    public Integer getDefaultContact() {
        return defaultContact;
    }

    public void setDefaultContact(Integer defaultContact) {
        this.defaultContact = defaultContact;
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
    @JoinTable(name = "contacts2")
    @MapKeyColumn(nullable = false)
    public Map<Integer, Person> getContacts2() {
        return contacts2;
    }

    public void setContacts2(Map<Integer, Person> localized) {
        this.contacts2 = localized;
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
    @JoinTable(name = "document_people_list_bag")
    public List<Person> getPeopleListBag() {
        return peopleListBag;
    }

    public void setPeopleListBag(List<Person> peopleListBag) {
        this.peopleListBag = peopleListBag;
    }

    @OneToMany
    @JoinTable(name = "document_people_coll_bag")
    public Collection<Person> getPeopleCollectionBag() {
        return peopleCollectionBag;
    }

    public void setPeopleCollectionBag(Collection<Person> peopleCollectionBag) {
        this.peopleCollectionBag = peopleCollectionBag;
    }

    @ElementCollection
    @OrderColumn(name = "strings_idx", nullable = false)
    @CollectionTable(name = "document_strings")
    public List<String> getStrings() {
        return strings;
    }

    public void setStrings(List<String> strings) {
        this.strings = strings;
    }

    @ElementCollection
    @CollectionTable(name = "document_string_map")
    @MapKeyColumn(nullable = false, length = 40)
    public Map<String, String> getStringMap() {
        return stringMap;
    }

    public void setStringMap(Map<String, String> stringMap) {
        this.stringMap = stringMap;
    }

    @ElementCollection
    @OrderColumn(name = "names_idx", nullable = false)
    @CollectionTable(name = "document_names")
    public List<NameObject> getNames() {
        return names;
    }

    public void setNames(List<NameObject> names) {
        this.names = names;
    }

    @ElementCollection
    @CollectionTable(name = "document_name_map")
    @MapKeyColumn(nullable = false, length = 40)
    public Map<String, NameObject> getNameMap() {
        return nameMap;
    }

    public void setNameMap(Map<String, NameObject> nameMap) {
        this.nameMap = nameMap;
    }

    @ElementCollection
    @OrderColumn(name = "name_containers_idx", nullable = false)
    @CollectionTable(name = "document_name_containers")
    public List<NameObjectContainer> getNameContainers() {
        return nameContainers;
    }

    public void setNameContainers(List<NameObjectContainer> nameContainers) {
        this.nameContainers = nameContainers;
    }

    @ElementCollection
    @CollectionTable(name = "document_name_container_map")
    @MapKeyColumn(nullable = false, length = 40)
    public Map<String, NameObjectContainer> getNameContainerMap() {
        return nameContainerMap;
    }

    public void setNameContainerMap(Map<String, NameObjectContainer> nameContainerMap) {
        this.nameContainerMap = nameContainerMap;
    }

    @Temporal(TemporalType.DATE)
    @Column // DataNucleus assumes this is not nullable when running com.blazebit.persistence.testsuite.JpqlFunctionTest.testGroupByFunction!?
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

    @ManyToOne(fetch = FetchType.LAZY)
    public Person getResponsiblePerson() {
        return responsiblePerson;
    }

    public void setResponsiblePerson(Person responsiblePerson) {
        this.responsiblePerson = responsiblePerson;
    }

}
