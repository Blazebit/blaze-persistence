/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.fetch.normal.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Document.class)
public interface SimpleDocumentFetchView {
    
    @IdMapping
    public Long getId();

    public String getName();

    public SimplePersonFetchSubView getOwner();

    public Set<SimplePersonFetchSubView> getPartners();

}
