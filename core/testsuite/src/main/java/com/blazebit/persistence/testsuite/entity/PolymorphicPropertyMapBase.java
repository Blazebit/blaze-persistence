/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.entity;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

/**
 *
 * @param <T>
 * @author Christian Beikov
 * @since 1.0.0
 */
@MappedSuperclass
public abstract class PolymorphicPropertyMapBase<T extends PolymorphicBase> extends PolymorphicPropertyBase {
    
    private static final long serialVersionUID = 1L;

    private T base;

    public PolymorphicPropertyMapBase() {
    }

    @ManyToOne(fetch = FetchType.LAZY)
    public T getBase() {
        return base;
    }

    public void setBase(T base) {
        this.base = base;
    }
}
