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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 *
 * @author ccbem
 */
@Entity
public class Person {
    private Long id;
    private String name;
    private Document partnerDocument;
    private Set<Document> ownedDocuments = new HashSet<Document>();
    private Map<Integer, String> localized = new HashMap<Integer, String>();

    public Person(){
    }
    
    public Person(String name) {
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

    @ManyToOne
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ElementCollection
    public Map<Integer, String> getLocalized() {
        return localized;
    }

    public void setLocalized(Map<Integer, String> localized) {
        this.localized = localized;
    }
}
