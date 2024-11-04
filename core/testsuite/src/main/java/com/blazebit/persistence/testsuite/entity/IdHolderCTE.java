/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import com.blazebit.persistence.CTE;

/**
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
@Entity
@CTE
public class IdHolderCTE implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;

    public IdHolderCTE() {
    }

    public IdHolderCTE(Long id) {
        this.id = id;
    }

    @Id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
