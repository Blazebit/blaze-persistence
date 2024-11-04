/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.elementcollection.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForElementCollections;

/**
 *
 * @author Christian Beikov
 * @since 1.6.0
 */
@EntityView(DocumentForElementCollections.class)
public interface DocumentForElementIdView {
    
    @IdMapping
    public Long getId();
}
