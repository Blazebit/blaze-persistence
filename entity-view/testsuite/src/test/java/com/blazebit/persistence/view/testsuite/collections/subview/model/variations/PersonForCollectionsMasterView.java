/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.subview.model.variations;

import java.util.List;
import java.util.Set;

import com.blazebit.persistence.view.CollectionMapping;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.PersonForCollections;
import com.blazebit.persistence.view.testsuite.collections.subview.model.PersonForCollectionsListNestedView;
import com.blazebit.persistence.view.testsuite.collections.subview.model.SubviewDocumentCollectionsView;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(PersonForCollections.class)
public interface PersonForCollectionsMasterView {
    
    @IdMapping
    public Long getId();

    public String getName();

    @CollectionMapping(forceUnique = true, ignoreIndex = true)
    public Set<? extends SubviewDocumentCollectionsView> getOwnedDocuments();

    public List<PersonForCollectionsListNestedView> getSomeCollection();
}
