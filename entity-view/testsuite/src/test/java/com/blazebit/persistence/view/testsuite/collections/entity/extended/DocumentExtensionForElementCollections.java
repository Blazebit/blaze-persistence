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

package com.blazebit.persistence.view.testsuite.collections.entity.extended;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 *
 * @author Christian Beikov
 * @since 1.0.6
 */
@Embeddable
public class DocumentExtensionForElementCollections implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private ExtendedDocumentForElementCollections parent;
    private Set<ExtendedDocumentForElementCollections> childDocuments = new HashSet<ExtendedDocumentForElementCollections>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ext_doc_elem_coll_parent")
    public ExtendedDocumentForElementCollections getParent() {
        return parent;
    }

    public void setParent(ExtendedDocumentForElementCollections parent) {
        this.parent = parent;
    }

    @OneToMany(mappedBy = "extension.parent")
    public Set<ExtendedDocumentForElementCollections> getChildDocuments() {
        return childDocuments;
    }

    public void setChildDocuments(Set<ExtendedDocumentForElementCollections> childDocuments) {
        this.childDocuments = childDocuments;
    }
    
}