/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.subview.model.variations;

import java.util.Set;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.PersonForCollections;
import com.blazebit.persistence.view.testsuite.collections.subview.model.SubviewDocumentSetListMapView;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(PersonForCollections.class)
public interface PersonForCollectionsSetListMapMasterView extends PersonForCollectionsMasterView {

    @Override
    public Set<SubviewDocumentSetListMapView> getOwnedDocuments();
}
