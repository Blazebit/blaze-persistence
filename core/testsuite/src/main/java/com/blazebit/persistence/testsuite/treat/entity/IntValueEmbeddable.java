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

package com.blazebit.persistence.testsuite.treat.entity;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class IntValueEmbeddable implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer someValue;

    public IntValueEmbeddable() {
    }

    public IntValueEmbeddable(Integer someValue) {
        this.someValue = someValue;
    }

    public Integer getSomeValue() {
        return someValue;
    }

    public void setSomeValue(Integer someValue) {
        this.someValue = someValue;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.someValue);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IntValueEmbeddable other = (IntValueEmbeddable) obj;
        if (!Objects.equals(this.someValue, other.someValue)) {
            return false;
        }
        return true;
    }
}
