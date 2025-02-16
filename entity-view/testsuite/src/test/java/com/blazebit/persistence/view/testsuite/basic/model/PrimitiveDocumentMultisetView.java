/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.view.testsuite.basic.model;

import com.blazebit.persistence.testsuite.entity.PrimitiveDocument;
import com.blazebit.persistence.testsuite.entity.PrimitivePerson;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.Mapping;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.6.8
 */
@EntityView(PrimitiveDocument.class)
public interface PrimitiveDocumentMultisetView extends PrimitiveSimpleDocumentView {

    @Mapping(fetch = FetchStrategy.MULTISET)
    public List<SelectFetchingPrimitivePersonView> getPartners();

    @Mapping(fetch = FetchStrategy.MULTISET)
    public List<SelectFetchingPrimitivePersonView> getPeople();

    public Map<Integer, PrimitivePersonView> getContacts();
}
