/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
@Entity
@Table(name = "person")
public class Person extends LongSequenceEntity {
    private static final long serialVersionUID = 1L;

    private String name;
    private NameObject nameObject = new NameObject();
    private long age;
    private Person friend;
    private Document partnerDocument;
    private Set<Document> ownedDocuments = new HashSet<Document>();
    private Set<Document> ownedDocuments2 = new HashSet<Document>();
    private Set<Document> favoriteDocuments = new HashSet<Document>();
    private Integer defaultLanguage;
    private Map<Integer, String> localized = new HashMap<Integer, String>();

    public Person() {
    }

    public Person(Long id) {
        super(id);
    }

    public Person(String name) {
        this.name = name;
    }

    public Person(String name, long age) {
        this.name = name;
        this.age = age;
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
    @JoinColumn(
            name = "responsible_person_id",
            referencedColumnName = "id"
    )
    public Set<Document> getOwnedDocuments2() {
        return ownedDocuments2;
    }

    public void setOwnedDocuments2(Set<Document> ownedDocuments2) {
        this.ownedDocuments2 = ownedDocuments2;
    }

    @OneToMany
    @JoinTable(name = "person_favorite_documents")
    public Set<Document> getFavoriteDocuments() {
        return favoriteDocuments;
    }

    public void setFavoriteDocuments(Set<Document> favoriteDocuments) {
        this.favoriteDocuments = favoriteDocuments;
    }

    @Basic(optional = false)
    @Column(length = 30)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Embedded
    public NameObject getNameObject() {
        return nameObject;
    }

    public void setNameObject(NameObject nameObject) {
        this.nameObject = nameObject;
    }

    public Integer getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(Integer defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }
    
    @ElementCollection
    @MapKeyColumn(nullable = false)
    @CollectionTable(name = "person_localized")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id")
    public Person getFriend() {
        return friend;
    }

    public void setFriend(Person friend) {
        this.friend = friend;
    }

}
