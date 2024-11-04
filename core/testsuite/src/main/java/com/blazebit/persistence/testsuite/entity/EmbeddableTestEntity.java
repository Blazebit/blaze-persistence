/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
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
