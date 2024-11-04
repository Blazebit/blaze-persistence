/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
