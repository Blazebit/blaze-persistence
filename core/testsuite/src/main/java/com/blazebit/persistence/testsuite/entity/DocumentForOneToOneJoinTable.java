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

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToOne;
import java.io.Serializable;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Entity
public class DocumentForOneToOneJoinTable extends Ownable implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private DocumentInfo documentInfoJoinTable;

    @Basic(optional = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToOne
    @JoinTable(
            name = "document_extra",
            joinColumns = @JoinColumn(name = "document_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "document_info_id", referencedColumnName = "id")
    )
    public DocumentInfo getDocumentInfoJoinTable() {
        return documentInfoJoinTable;
    }

    public void setDocumentInfoJoinTable(DocumentInfo documentInfoJoinTable) {
        this.documentInfoJoinTable = documentInfoJoinTable;
    }
}
