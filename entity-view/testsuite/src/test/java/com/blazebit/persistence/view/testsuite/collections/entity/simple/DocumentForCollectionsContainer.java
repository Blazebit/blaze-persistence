/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.entity.simple;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@Entity
@Table(name = "doc_coll_cont")
public class DocumentForCollectionsContainer implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String containerName;
    private Set<DocumentForCollections> documents = new HashSet<>();

    public DocumentForCollectionsContainer() {
    }

    public DocumentForCollectionsContainer(String containerName) {
        this.containerName = containerName;
    }

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    @ManyToMany
    public Set<DocumentForCollections> getDocuments() {
        return documents;
    }

    public void setDocuments(Set<DocumentForCollections> partners) {
        this.documents = partners;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        DocumentForCollectionsContainer other = (DocumentForCollectionsContainer) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
