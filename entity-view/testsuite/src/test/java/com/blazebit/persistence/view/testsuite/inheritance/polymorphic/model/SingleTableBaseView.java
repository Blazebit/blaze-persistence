/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.inheritance.polymorphic.model;

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

    public String getName();

    @Mapping("TREAT(this AS SingleTableSub1).sub1Value")
    public Integer getSub1ValueOrNull();

    public SingleTableSimpleBaseView getParent();

    @Mapping("TREAT(parent AS SingleTableSub1)")
    public SingleTableSimpleSub1View getParentAsSingleTableSub1();
}
