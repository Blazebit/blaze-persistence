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

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@Entity
public class Workflow implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Locale defaultLanguage;
    private Set<Locale> supportedLocales = new HashSet<Locale>();
//    private Set<String> tags = new HashSet<String>();
    private Map<Locale, LocalizedEntity> localized = new HashMap<Locale, LocalizedEntity>();

    public Workflow() {
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
    public Locale getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(Locale defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    @ElementCollection
    @CollectionTable(
        joinColumns = {
            @JoinColumn(
                name = "id",
                referencedColumnName = "id",
                nullable = false,
                insertable = false,
                updatable = false) })
    // Careful, MySQL will fail if the value is too long since it will be part of a unique key
    @Column(length = 10)
    public Set<Locale> getSupportedLocales() {
        return supportedLocales;
    }

    public void setSupportedLocales(Set<Locale> supportedLocales) {
        this.supportedLocales = supportedLocales;
    }

//    @ElementCollection
//    // Careful, MySQL will fail if the value is too long since it will be part of a unique key
//    @Column(length = 20)
//    public Set<String> getTags() {
//      return tags;
//    }
//
//    public void setTags(Set<String> tags) {
//        this.tags = tags;
//    }

    @ElementCollection
    @CollectionTable(
        joinColumns = {
            @JoinColumn(
                name = "id",
                referencedColumnName = "id",
                nullable = false,
                insertable = false,
                updatable = false) })
    @MapKeyColumn(
        name = "LANGUAGE_CODE",
        length = 10,
        nullable = false,
        insertable = false,
        updatable = false)
    public Map<Locale, LocalizedEntity> getLocalized() {
        return localized;
    }

    public void setLocalized(Map<Locale, LocalizedEntity> localized) {
        this.localized = localized;
    }

}
