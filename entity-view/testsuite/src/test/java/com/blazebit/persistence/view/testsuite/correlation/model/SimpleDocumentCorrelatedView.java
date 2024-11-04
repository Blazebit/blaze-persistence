/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.correlation.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.testsuite.entity.Document;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Document.class)
public interface SimpleDocumentCorrelatedView {
    
    @IdMapping
    public Long getId();

    public String getName();

    public SimplePersonCorrelatedSubView getOwner();

    public Set<SimplePersonCorrelatedSubView> getPartners();

}
