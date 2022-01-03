/*
 * Copyright 2014 - 2022 Blazebit.
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

import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "emb_tst_ent")
@DiscriminatorValue("base")
public class EmbeddableTestEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private EmbeddableTestEntityId id;
    private Long version;
    private EmbeddableTestEntityEmbeddable embeddable = new EmbeddableTestEntityEmbeddable();
    private List<NameObject> elementCollection4 = new ArrayList<>();

    public EmbeddableTestEntity() {
        id = new EmbeddableTestEntityId();
    }

    @EmbeddedId
    public EmbeddableTestEntityId getId() {
        return id;
    }

    public void setId(EmbeddableTestEntityId id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Embedded
    public EmbeddableTestEntityEmbeddable getEmbeddable() {
        return embeddable;
    }
    
    public void setEmbeddable(EmbeddableTestEntityEmbeddable embeddable) {
        this.embeddable = embeddable;
    }

    @ElementCollection
    @OrderColumn(name = "emb_ts_ent_elem_coll4_idx", nullable = false)
    @CollectionTable(name = "emb_ts_ent_elem_coll4",
            joinColumns = {
                    @JoinColumn(name = "elem_coll4_parent_key", referencedColumnName = "test_key"),
                    @JoinColumn(name = "elem_coll4_parent_value", referencedColumnName = "test_value")
            }
    )
    public List<NameObject> getElementCollection4() {
        return elementCollection4;
    }

    public void setElementCollection4(List<NameObject> elementCollection4) {
        this.elementCollection4 = elementCollection4;
    }

}
