/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.filter.inheritance.model;

import com.blazebit.persistence.testsuite.entity.PrimitiveDocument;
import com.blazebit.persistence.view.AttributeFilter;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewInheritanceMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.filter.EqualFilter;

/**
 * @author Christian Beikov
 * @since 1.3.0
 */
@EntityView(PrimitiveDocument.class)
@EntityViewInheritanceMapping("name = 'doc2'")
public interface AttributeFilterInheritancePrimitiveDocumentViewSub2 extends AttributeFilterInheritancePrimitiveDocumentView {

    @Mapping("UPPER(owner.name)")
    @AttributeFilter(EqualFilter.class)
    public String getName();
}
