/*
 * Copyright 2014 - 2023 Blazebit.
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

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author Christian Beikov
 * @since 1.6.10
 */
@Entity
@Table(name = "SIMP_EMB_ID_ENT")
public class SimpleEmbeddedIdEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private SimpleEmbeddedIdEntityId id;
    private String name;

    @EmbeddedId
    public SimpleEmbeddedIdEntityId getId() {
        return id;
    }

    public void setId(SimpleEmbeddedIdEntityId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
