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

package com.blazebit.persistence.view.testsuite.entity;

import com.blazebit.persistence.testsuite.entity.IntIdEntity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class EmbeddableTestEntityId implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private IntIdEntity intIdEntity;
    private String key;

    public EmbeddableTestEntityId() {
    }

    public EmbeddableTestEntityId(IntIdEntity intIdEntity, String key) {
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
        EmbeddableTestEntityId other = (EmbeddableTestEntityId) obj;
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
