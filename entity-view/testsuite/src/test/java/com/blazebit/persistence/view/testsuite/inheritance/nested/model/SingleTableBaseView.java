/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.inheritance.nested.model;

import com.blazebit.persistence.testsuite.treat.entity.SingleTableBase;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewInheritance;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(SingleTableBase.class)
@EntityViewInheritance
public interface SingleTableBaseView {
    
    @IdMapping
    public Long getId();

    @Mapping("name")
    String getName();

    // The key to reproduce issue 456 is to have a method in the base class
    // that is lexically ordered first
    @Mapping("name")
    String getA();
}
