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
package com.blazebit.persistence.entity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
@Entity
public class Document implements Serializable {

    private Long id;
    private String name;
    private Object someTransientField;
    private Set<Version> versions = new HashSet<Version>();
    private Set<Person> partners = new HashSet<Person>();
    private Person owner;
    private long age;
    private int index;
    private String nonJoinable;
    private Map<Integer, Person> contacts = new HashMap<Integer, Person>();
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
        this.versions.addAll(Arrays.asList(versions));
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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @OneToMany(mappedBy = "partnerDocument")
    public Set<Person> getPartners() {
        return partners;
    }

    public void setPartners(Set<Person> partners) {
        this.partners = partners;
    }

    @ManyToOne(optional = false)
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

    @OneToMany
    @JoinTable(name = "contacts")
    @MapKeyColumn(table = "contacts")
    public Map<Integer, Person> getContacts() {
        return contacts;
    }

    public void setContacts(Map<Integer, Person> localized) {
        this.contacts = localized;
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

}
