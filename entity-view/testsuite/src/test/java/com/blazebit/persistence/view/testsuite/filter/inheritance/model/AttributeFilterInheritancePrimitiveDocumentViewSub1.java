/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.filter.inheritance.model;

import com.blazebit.persistence.testsuite.entity.PrimitiveDocument;
import com.blazebit.persistence.view.AttributeFilter;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewInheritance;
import com.blazebit.persistence.view.EntityViewInheritanceMapping;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.filter.EqualFilter;

/**
 * @author Christian Beikov
 * @since 1.3.0
 */
@EntityView(PrimitiveDocument.class)
@EntityViewInheritanceMapping("name = 'doc1'")
public interface AttributeFilterInheritancePrimitiveDocumentViewSub1 extends AttributeFilterInheritancePrimitiveDocumentView {

    @Mapping("owner.name")
    @AttributeFilter(EqualFilter.class)
    public String getName();
}
