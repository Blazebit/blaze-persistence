/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
@Embeddable
public class EmbeddedDocumentTupleEntityId implements Serializable {

    private Long element1;
    private Long element2;

    public EmbeddedDocumentTupleEntityId() { }

    public EmbeddedDocumentTupleEntityId(Long element1, Long element2) {
        this.element1 = element1;
        this.element2 = element2;
    }

    @Column(nullable = false)
    public Long getElement1() {
        return element1;
    }

    public void setElement1(Long element1) {
        this.element1 = element1;
    }

    @Column(nullable = false)
    public Long getElement2() {
        return element2;
    }

    public void setElement2(Long element2) {
        this.element2 = element2;
    }
}
