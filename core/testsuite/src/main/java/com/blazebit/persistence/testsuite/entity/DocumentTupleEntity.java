/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@Entity
public class DocumentTupleEntity implements Serializable {
    private Document element1;
    private Document element2;

    public DocumentTupleEntity() { }

    public DocumentTupleEntity(Document element1, Document element2) {
        this.element1 = element1;
        this.element2 = element2;
    }

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    public Document getElement1() {
        return element1;
    }

    public void setElement1(Document element1) {
        this.element1 = element1;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    public Document getElement2() {
        return element2;
    }

    public void setElement2(Document element2) {
        this.element2 = element2;
    }
}
