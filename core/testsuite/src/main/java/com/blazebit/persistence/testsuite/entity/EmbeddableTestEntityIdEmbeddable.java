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

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class EmbeddableTestEntityIdEmbeddable implements Serializable {

    private static final long serialVersionUID = 1L;

    private String someValue;

    public EmbeddableTestEntityIdEmbeddable() {
    }

    public EmbeddableTestEntityIdEmbeddable(String someValue) {
        this.someValue = someValue;
    }

    @Column(name = "some_value", nullable = false, length = 10)
    public String getSomeValue() {
        return someValue;
    }

    public void setSomeValue(String someValue) {
        this.someValue = someValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((someValue == null) ? 0 : someValue.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EmbeddableTestEntityIdEmbeddable other = (EmbeddableTestEntityIdEmbeddable) obj;
        if (someValue == null) {
            if (other.someValue != null) {
                return false;
            }
        } else if (!someValue.equals(other.someValue)) {
            return false;
        }

        return true;
    }

}
