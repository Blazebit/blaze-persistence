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

import java.io.Serializable;

/**
 * @author Christian Beikov
 * @since 1.3.0
 */
public class IdClassEntityId implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer key1;
    private String key2;

    public IdClassEntityId() {
    }

    public IdClassEntityId(Integer key1, String key2) {
        this.key1 = key1;
        this.key2 = key2;
    }

    public Integer getKey1() {
        return key1;
    }

    public void setKey1(Integer key1) {
        this.key1 = key1;
    }

    public String getKey2() {
        return key2;
    }

    public void setKey2(String key2) {
        this.key2 = key2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IdClassEntityId that = (IdClassEntityId) o;

        if (key1 != null ? !key1.equals(that.key1) : that.key1 != null) {
            return false;
        }
        return key2 != null ? key2.equals(that.key2) : that.key2 == null;
    }

    @Override
    public int hashCode() {
        int result = key1 != null ? key1.hashCode() : 0;
        result = 31 * result + (key2 != null ? key2.hashCode() : 0);
        return result;
    }
}
