/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import java.io.Serializable;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
@Entity
public class EmbeddedDocumentTupleEntity implements Serializable {

    @EmbeddedId
    private EmbeddedDocumentTupleEntityId id;

    public EmbeddedDocumentTupleEntity() {
        this(new EmbeddedDocumentTupleEntityId());
    }

    public EmbeddedDocumentTupleEntity(Long element1, Long element2) {
        this(new EmbeddedDocumentTupleEntityId(element1, element2));
    }

    public EmbeddedDocumentTupleEntity(EmbeddedDocumentTupleEntityId id) {
        this.id = id;
    }
}
