/*
 * Copyright 2014 - 2020 Blazebit.
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
@Entity
@Table(name = "document_version")
public class Version implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Document document;
    private Calendar date;
    private int idx;
    private String url;
    private Map<Integer, String> localized = new HashMap<Integer, String>();

    public Version() {
    }

    public Version(int idx) {
        this.idx = idx;
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
    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    @Column(name = "version_date")
    @Temporal(TemporalType.DATE)
    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    // EclipseLink... https://github.com/eclipse-ee4j/eclipselink/issues/884
    // and DataNucleus... https://github.com/datanucleus/datanucleus-core/issues/355
    public Integer getIdx() {
        return idx;
    }

    public void setIdx(Integer index) {
        this.idx = index;
    }

    @Column(length = 30)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @ElementCollection
    @MapKeyColumn(nullable = false)
    @CollectionTable(name = "version_localized")
    public Map<Integer, String> getLocalized() {
        return localized;
    }

    public void setLocalized(Map<Integer, String> localized) {
        this.localized = localized;
    }
}
