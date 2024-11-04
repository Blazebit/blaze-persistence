/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.embedded.collection.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.basic.model.IdHolderView;
import com.blazebit.persistence.testsuite.entity.Document;

import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Document.class)
public interface DocumentViewWithEmbeddedCollection extends IdHolderView<Long> {

    @Mapping("this")
    List<DocumentDetailCollectionView> getDetails();

    @Mapping("this")
    List<DocumentDetailEmbeddableCollectionView> getEmbeddedDetails();

}
