/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
public class Version extends LongSequenceEntity {

    private static final long serialVersionUID = 1L;

    private Document document;
    private Calendar date;
    private int versionIdx;
    private String url;
    private Map<Integer, String> localized = new HashMap<Integer, String>();

    public Version() {
    }

    public Version(int versionIdx) {
        this.versionIdx = versionIdx;
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
    public Integer getVersionIdx() {
        return versionIdx;
    }

    public void setVersionIdx(Integer index) {
        this.versionIdx = index;
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
