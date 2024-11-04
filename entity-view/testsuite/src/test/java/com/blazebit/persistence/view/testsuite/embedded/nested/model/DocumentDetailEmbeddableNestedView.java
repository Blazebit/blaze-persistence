/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.embedded.nested.model;

import com.blazebit.persistence.view.AttributeFilter;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.filter.EqualFilter;
import com.blazebit.persistence.testsuite.entity.Document;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Document.class)
public interface DocumentDetailEmbeddableNestedView {

    @AttributeFilter(EqualFilter.class)
    public String getName();
}
