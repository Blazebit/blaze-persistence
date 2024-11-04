/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.elementcollection.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.UpdatableMapping;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForElementCollections;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.PersonForElementCollections;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.6.0
 */
@UpdatableEntityView
@EntityView(DocumentForElementCollections.class)
public interface DocumentForElementCollectionsEmbeddableElementsView {
    
    @IdMapping
    public Long getId();

    public String getName();

    @UpdatableMapping
    public Set<PersonForElementCollections> getPartners();
}
