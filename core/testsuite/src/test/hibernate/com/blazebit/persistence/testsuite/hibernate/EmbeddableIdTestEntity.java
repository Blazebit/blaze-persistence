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

package com.blazebit.persistence.testsuite.hibernate;

import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "emb_id_tst_ent")
public class EmbeddableIdTestEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private EmbeddableIdTestEntityId id;
    private EmbeddableIdTestEntityEmbeddable embeddable = new EmbeddableIdTestEntityEmbeddable();

    public EmbeddableIdTestEntity() {
    }

    @EmbeddedId
    public EmbeddableIdTestEntityId getId() {
        return id;
    }

    public void setId(EmbeddableIdTestEntityId id) {
        this.id = id;
    }

    @Embedded
    public EmbeddableIdTestEntityEmbeddable getEmbeddable() {
        return embeddable;
    }

    public void setEmbeddable(EmbeddableIdTestEntityEmbeddable embeddable) {
        this.embeddable = embeddable;
    }

}
