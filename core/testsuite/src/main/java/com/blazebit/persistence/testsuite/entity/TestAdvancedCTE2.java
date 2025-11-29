/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import com.blazebit.persistence.CTE;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.io.Serializable;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Entity
@CTE
public class TestAdvancedCTE2 implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private TestCTEEmbeddable embeddable;

    @Id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Embedded
    public TestCTEEmbeddable getEmbeddable() {
        return embeddable;
    }

    public void setEmbeddable(TestCTEEmbeddable embeddable) {
        this.embeddable = embeddable;
    }
}
