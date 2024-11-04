/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.view.testsuite.basic.model;

import com.blazebit.persistence.testsuite.entity.PrimitiveDocument;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.Mapping;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.6.8
 */
@EntityView(PrimitiveDocument.class)
public interface PrimitiveDocumentMultisetView extends PrimitiveDocumentView {

    @Mapping(fetch = FetchStrategy.MULTISET)
    public List<PrimitivePersonView> getPartners();

}
