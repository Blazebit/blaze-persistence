/*
 * Copyright 2014 Blazebit.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.*;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
@Entity
public class Document extends Ownable implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
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
    private Calendar creationDate;
    private Calendar creationDate2;
    private Date lastModified;
    private Date lastModified2;
    private DocumentType documentType;
    private Boolean archived = false;
    private Document parent;

    public Document() {
    }

    public Document(String name) {
        this.name = name;
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

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
    @MapKeyColumn(table = "contacts", nullable = false)
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
		Document other = (Document) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
