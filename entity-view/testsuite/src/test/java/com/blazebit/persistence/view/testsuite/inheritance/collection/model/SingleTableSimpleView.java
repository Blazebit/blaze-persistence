/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.inheritance.collection.model;

import com.blazebit.persistence.testsuite.treat.entity.SingleTableBase;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(SingleTableBase.class)
public interface SingleTableSimpleView {
    
    @IdMapping
    public Long getId();
    public String getName();
}
