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

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Embeddable
public class NameObject implements Serializable {

    private String primaryName;
    private String secondaryName;
    private IntIdEntity intIdEntity;

    public NameObject() {
    }

    public NameObject(String primaryName, String secondaryName) {
        this.primaryName = primaryName;
        this.secondaryName = secondaryName;
    }

    public NameObject(String primaryName, String secondaryName, IntIdEntity intIdEntity) {
        this.primaryName = primaryName;
        this.secondaryName = secondaryName;
        this.intIdEntity = intIdEntity;
    }

    @Column(length = 30)
    public String getPrimaryName() {
        return primaryName;
    }

    public void setPrimaryName(String primaryName) {
        this.primaryName = primaryName;
    }

    @Column(length = 30)
    public String getSecondaryName() {
        return secondaryName;
    }

    public void setSecondaryName(String secondaryName) {
        this.secondaryName = secondaryName;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "name_object_int_id_entity")
    public IntIdEntity getIntIdEntity() {
        return intIdEntity;
    }

    public void setIntIdEntity(IntIdEntity intIdEntity) {
        this.intIdEntity = intIdEntity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NameObject)) {
            return false;
        }

        NameObject that = (NameObject) o;

        if (getPrimaryName() != null ? !getPrimaryName().equals(that.getPrimaryName()) : that.getPrimaryName() != null) {
            return false;
        }
        if (getSecondaryName() != null ? !getSecondaryName().equals(that.getSecondaryName()) : that.getSecondaryName() != null) {
            return false;
        }
        return getIntIdEntity() != null ? getIntIdEntity().equals(that.getIntIdEntity()) : that.getIntIdEntity() == null;
    }

    @Override
    public int hashCode() {
        int result = getPrimaryName() != null ? getPrimaryName().hashCode() : 0;
        result = 31 * result + (getSecondaryName() != null ? getSecondaryName().hashCode() : 0);
        result = 31 * result + (getIntIdEntity() != null ? getIntIdEntity().hashCode() : 0);
        return result;
    }
}
