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

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
@Entity
public class Person implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private long age;
    private Document partnerDocument;
    private Set<Document> ownedDocuments = new HashSet<Document>();
    private Set<Document> favoriteDocuments = new HashSet<Document>();
    private Map<Integer, String> localized = new HashMap<Integer, String>();

    public Person() {
    }

    public Person(Long id) {
        this.id = id;
    }

    public Person(String name) {
        this.name = name;
    }

    public Person(String name, long age) {
        this.name = name;
        this.age = age;
    }

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    public Document getPartnerDocument() {
        return partnerDocument;
    }

    public void setPartnerDocument(Document partnerDocument) {
        this.partnerDocument = partnerDocument;
    }

    @OneToMany(mappedBy = "owner")
    public Set<Document> getOwnedDocuments() {
        return ownedDocuments;
    }

    public void setOwnedDocuments(Set<Document> ownedDocuments) {
        this.ownedDocuments = ownedDocuments;
    }

    @OneToMany
    @CollectionTable
    public Set<Document> getFavoriteDocuments() {
        return favoriteDocuments;
    }

    public void setFavoriteDocuments(Set<Document> favoriteDocuments) {
        this.favoriteDocuments = favoriteDocuments;
    }

    @Basic(optional = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    @ElementCollection
    @MapKeyColumn(nullable = false)
    public Map<Integer, String> getLocalized() {
        return localized;
    }

    public void setLocalized(Map<Integer, String> localized) {
        this.localized = localized;
    }

    public long getAge() {
        return age;
    }

    public void setAge(long age) {
        this.age = age;
    }
}
