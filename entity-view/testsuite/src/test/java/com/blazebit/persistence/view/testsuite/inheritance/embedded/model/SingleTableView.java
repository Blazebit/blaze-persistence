/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.inheritance.embedded.model;

import com.blazebit.persistence.testsuite.treat.entity.SingleTableBase;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingInheritance;
import com.blazebit.persistence.view.MappingInheritanceSubtype;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(SingleTableBase.class)
public interface SingleTableView {

    @IdMapping
    public Long getId();

    @Mapping("this")
    public SingleTableDetailsView getDetails();
}
