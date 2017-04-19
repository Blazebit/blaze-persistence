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

package com.blazebit.persistence.view.testsuite.entity;

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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
@Entity
public class Document implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private Object someTransientField;
    private Set<Version> versions = new HashSet<Version>();
    private Set<Person> partners = new HashSet<Person>();
    private Person owner;
    private long age;
    private String nonJoinable;
    private Integer defaultContact;
    private Map<Integer, Person> contacts = new HashMap<Integer, Person>();
    private Map<Integer, Person> contacts2 = new HashMap<Integer, Person>();
    private List<Person> personList = new ArrayList<Person>();
    private Calendar creationDate;
    private Date lastModified;

    public Document() {
    }

    public Document(String name) {
        this.name = name;
    }

    public Document(String name, Person owner, Version... versions) {
        this.name = name;
        this.owner = owner;
        for (Version v : versions) {
            v.setDocument(this);
            this.versions.add(v);
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

    @OneToMany(mappedBy = "document", cascade = {CascadeType.PERSIST})
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

    @OneToMany(mappedBy = "partnerDocument")
    public Set<Person> getPartners() {
        return partners;
    }

    public void setPartners(Set<Person> partners) {
        this.partners = partners;
    }

    @ManyToOne(optional = false, cascade = { CascadeType.PERSIST })
    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

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

    @OrderColumn(name = "position", nullable = false)
    @OneToMany
    @JoinTable(name = "personlist")
    public List<Person> getPersonList() {
        return personList;
    }

    public void setPersonList(List<Person> personList) {
        this.personList = personList;
    }

    @Temporal(TemporalType.DATE)
    public Calendar getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Calendar creationDate) {
        this.creationDate = creationDate;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Document)) {
            return false;
        }
        Document other = (Document) obj;
        if (getId() == null) {
            if (other.getId() != null) {
                return false;
            }
        } else if (!getId().equals(other.getId())) {
            return false;
        }
        return true;
    }

}
