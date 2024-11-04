/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.hibernate;

import com.blazebit.persistence.testsuite.entity.EmbeddableTestEntityIdEmbeddable;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;

import javax.persistence.*;
import java.io.Serializable;

@Embeddable
public class EmbeddableIdTestEntityId implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private IntIdEntity intIdEntity;
    private String key;
    private EmbeddableTestEntityIdEmbeddable localizedEntity;

    public EmbeddableIdTestEntityId() {
    }

    public EmbeddableIdTestEntityId(IntIdEntity intIdEntity, String key) {
        this.intIdEntity = intIdEntity;
        this.key = key;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "int_id_entity_id", nullable = false)
    public IntIdEntity getIntIdEntity() {
        return intIdEntity;
    }

    public void setIntIdEntity(IntIdEntity intIdEntity) {
        this.intIdEntity = intIdEntity;
    }

    // Rename because mysql can't handle "key"
    // Fixed size because mysql has size limitations
    @Column(name = "test_key", nullable = false, length = 100)
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Embedded
    public EmbeddableTestEntityIdEmbeddable getLocalizedEntity() {
        return localizedEntity;
    }

    public void setLocalizedEntity(EmbeddableTestEntityIdEmbeddable localizedEntity) {
        this.localizedEntity = localizedEntity;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((intIdEntity == null) ? 0 : intIdEntity.hashCode());
        result = prime * result + ((key == null) ? 0 : key.hashCode());
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
        EmbeddableIdTestEntityId other = (EmbeddableIdTestEntityId) obj;
        if (intIdEntity == null) {
            if (other.intIdEntity != null)
                return false;
        } else if (!intIdEntity.equals(other.intIdEntity))
            return false;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        return true;
    }

}
