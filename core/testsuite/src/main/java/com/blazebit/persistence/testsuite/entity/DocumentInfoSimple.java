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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.io.Serializable;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@Entity
public class DocumentInfoSimple implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private DocumentForSimpleOneToOne document;
    private String someInfo;

    public DocumentInfoSimple() {
    }

    public DocumentInfoSimple(Long id, DocumentForSimpleOneToOne document, String someInfo) {
        this.id = id;
        this.document = document;
        this.someInfo = someInfo;
        document.setDocumentInfo(this);
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
    public DocumentForSimpleOneToOne getDocument() {
        return document;
    }

    public void setDocument(DocumentForSimpleOneToOne document) {
        this.document = document;
    }

    public String getSomeInfo() {
        return someInfo;
    }

    public void setSomeInfo(String someInfo) {
        this.someInfo = someInfo;
    }
}
