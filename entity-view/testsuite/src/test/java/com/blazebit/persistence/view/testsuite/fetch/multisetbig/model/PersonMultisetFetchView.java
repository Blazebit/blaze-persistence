/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.fetch.multisetbig.model;

import java.util.Set;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.timeentity.PersonForMultisetFetch;

/**
 *
 * @author Christian Beikov
 * @since 1.6.11
 */
@EntityView(PersonForMultisetFetch.class)
public interface PersonMultisetFetchView extends SimplePersonMultisetFetchView {

    @Mapping(fetch = FetchStrategy.MULTISET)
    public Set<DocumentMultisetFetchView> getOwnedDocuments();

}
