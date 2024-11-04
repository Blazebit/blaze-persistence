/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.hibernate.entity;

import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
