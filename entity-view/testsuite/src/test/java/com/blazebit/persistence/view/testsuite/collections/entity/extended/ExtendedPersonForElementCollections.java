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

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@Embeddable
public class ExtendedPersonForElementCollections implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String fullname;
    private ExtendedDocumentForElementCollections partnerDocument;

    public ExtendedPersonForElementCollections() {
    }

    public ExtendedPersonForElementCollections(String fullname) {
        this.fullname = fullname;
    }

    @Column(name = "ext_pers_elem_coll_fullname")
    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ext_pers_elem_coll_partner_doc")
    public ExtendedDocumentForElementCollections getPartnerDocument() {
        return partnerDocument;
    }

    public void setPartnerDocument(ExtendedDocumentForElementCollections partnerDocument) {
        this.partnerDocument = partnerDocument;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fullname == null) ? 0 : fullname.hashCode());
        result = prime * result + ((partnerDocument == null) ? 0 : partnerDocument.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExtendedPersonForElementCollections other = (ExtendedPersonForElementCollections) obj;
        if (fullname == null) {
            if (other.fullname != null)
                return false;
        } else if (!fullname.equals(other.fullname))
            return false;
        if (partnerDocument == null) {
            if (other.partnerDocument != null)
                return false;
        } else if (!partnerDocument.equals(other.partnerDocument))
            return false;
        return true;
    }
}
