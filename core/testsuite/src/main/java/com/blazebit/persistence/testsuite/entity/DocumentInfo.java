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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.io.Serializable;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Entity
public class DocumentInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private DocumentForOneToOne document;
    private DocumentForOneToOne document2;
    private String someInfo;

    public DocumentInfo() {
    }

    public DocumentInfo(Long id, DocumentForOneToOne document, String someInfo) {
        this.id = id;
        this.document = document;
        this.document2 = document;
        this.someInfo = someInfo;
        document.setDocumentInfo(this);
        document.setDocumentInfo2(this);
    }

    @Id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    public DocumentForOneToOne getDocument() {
        return document;
    }

    public void setDocument(DocumentForOneToOne document) {
        this.document = document;
    }

    @OneToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "document2_id", nullable = false)
    public DocumentForOneToOne getDocument2() {
        return document2;
    }

    public void setDocument2(DocumentForOneToOne document2) {
        this.document2 = document2;
    }

    public String getSomeInfo() {
        return someInfo;
    }

    public void setSomeInfo(String someInfo) {
        this.someInfo = someInfo;
    }
}
