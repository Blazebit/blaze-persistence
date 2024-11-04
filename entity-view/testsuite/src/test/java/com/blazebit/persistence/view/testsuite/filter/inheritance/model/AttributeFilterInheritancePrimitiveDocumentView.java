/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.filter.inheritance.model;

import com.blazebit.persistence.testsuite.entity.PrimitiveDocument;
import com.blazebit.persistence.view.AttributeFilter;
import com.blazebit.persistence.view.AttributeFilters;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewInheritance;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.filter.ContainsFilter;
import com.blazebit.persistence.view.filter.ContainsIgnoreCaseFilter;
import com.blazebit.persistence.view.filter.EqualFilter;

/**
 * @author Christian Beikov
 * @since 1.3.0
 */
@EntityView(PrimitiveDocument.class)
@EntityViewInheritance
public interface AttributeFilterInheritancePrimitiveDocumentView {

    @IdMapping
    Long getId();
}
