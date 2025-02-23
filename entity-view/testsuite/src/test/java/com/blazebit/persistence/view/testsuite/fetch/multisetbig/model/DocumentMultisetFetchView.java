/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.fetch.multisetbig.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.timeentity.DocumentForMultisetFetch;

/**
 *
 * @author Christian Beikov
 * @since 1.6.11
 */
@EntityView(DocumentForMultisetFetch.class)
public interface DocumentMultisetFetchView {

    @IdMapping
    public Long getId();

    public String getName();

    // The goal here is to ensure we fetch 50+ attributes within MULTISET to trigger a PostgreSQL limitation described in #1979

    @Mapping("this")
    public DocumentTemporalsView getTemporals1();

    @Mapping("this")
    public DocumentTemporalsView getTemporals2();

    @Mapping("this")
    public DocumentTemporalsView getTemporals3();

    @Mapping("this")
    public DocumentTemporalsView getTemporals4();

    @Mapping("this")
    public DocumentTemporalsView getTemporals5();

}
