/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.fetch.multisetbig.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.timeentity.PersonForMultisetFetch;

/**
 *
 * @author Christian Beikov
 * @since 1.6.11
 */
@EntityView(PersonForMultisetFetch.class)
public interface SimplePersonMultisetFetchView {

    @IdMapping
    public Long getId();

    public String getName();

    @Mapping("partnerDocument.id")
    public Long getPartnerDocumentId();

}
