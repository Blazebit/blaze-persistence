/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.subview.model.variations;

import java.util.Set;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.PersonForCollections;
import com.blazebit.persistence.view.testsuite.collections.subview.model.SubviewDocumentMapSetListView;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(PersonForCollections.class)
public interface PersonForCollectionsMapSetListMasterView extends PersonForCollectionsMasterView {

    @Override
    public Set<SubviewDocumentMapSetListView> getOwnedDocuments();
}
