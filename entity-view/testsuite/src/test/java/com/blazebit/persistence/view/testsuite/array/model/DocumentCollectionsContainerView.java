/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.array.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Limit;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForCollectionsContainer;

/**
 *
 * @author Christian Beikov
 * @since 1.6.16
 */
@EntityView(DocumentForCollectionsContainer.class)
public interface DocumentCollectionsContainerView {
    
    @IdMapping
    public Long getId();

    public String getContainerName();

    @Mapping("documents")
    @Limit(limit = "1", order = "name ASC")
    public DocumentCollectionsLimitView getFirstDocument();
}
