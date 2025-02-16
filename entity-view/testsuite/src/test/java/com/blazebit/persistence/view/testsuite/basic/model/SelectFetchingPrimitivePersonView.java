/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.view.testsuite.basic.model;

import com.blazebit.persistence.testsuite.entity.PrimitivePerson;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.Mapping;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(PrimitivePerson.class)
public interface SelectFetchingPrimitivePersonView extends IdHolderView<Long> {

    public String getName();

    @Mapping(fetch = FetchStrategy.SELECT)
    public Set<PrimitiveSimpleDocumentView> getOwnedDocuments();
}
