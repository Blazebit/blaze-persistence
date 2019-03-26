/*
 * Copyright 2014 - 2019 Blazebit.
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

package com.blazebit.persistence.view.testsuite.subview.multiplecollections.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author Moritz Becker
 * @since 1.4.0
 */
@Entity
@IdClass(C.CompositeId.class)
public class C {
    @Id
    private Long id1;
    @Id
    private Long id2;
    private String value;

    public C() { }

    public C(Long id1, Long id2) {
        this.id1 = id1;
        this.id2 = id2;
    }

    public Long getId1() {
        return id1;
    }

    public void setId1(Long id1) {
        this.id1 = id1;
    }

    public Long getId2() {
        return id2;
    }

    public void setId2(Long id2) {
        this.id2 = id2;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        C c = (C) o;
        return Objects.equals(id1, c.id1) &&
                Objects.equals(id2, c.id2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id1, id2);
    }

    public static class CompositeId implements Serializable {
        private Long id1;
        private Long id2;

        public Long getId1() {
            return id1;
        }

        public void setId1(Long id1) {
            this.id1 = id1;
        }

        public Long getId2() {
            return id2;
        }

        public void setId2(Long id2) {
            this.id2 = id2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CompositeId that = (CompositeId) o;
            return Objects.equals(id1, that.id1) &&
                    Objects.equals(id2, that.id2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id1, id2);
        }
    }
}
