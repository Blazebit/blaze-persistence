/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.entity;

import com.blazebit.persistence.testsuite.entity.IntIdEntity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class EmbeddableTestEntityId2 implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private IntIdEntity intIdEntity;
    private String key;

    public EmbeddableTestEntityId2() {
    }

    public EmbeddableTestEntityId2(IntIdEntity intIdEntity, String key) {
        this.intIdEntity = intIdEntity;
        this.key = key;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "int_id_ent")
    public IntIdEntity getIntIdEntity() {
        return intIdEntity;
    }

    public void setIntIdEntity(IntIdEntity intIdEntity) {
        this.intIdEntity = intIdEntity;
    }

    // Rename because mysql can't handle "key"
    // Fixed size because mysql has size limitations
    @Column(name = "test_key", length = 100)
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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
        EmbeddableTestEntityId2 other = (EmbeddableTestEntityId2) obj;
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
