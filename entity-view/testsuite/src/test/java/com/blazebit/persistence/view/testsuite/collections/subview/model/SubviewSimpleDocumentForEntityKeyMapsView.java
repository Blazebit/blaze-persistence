/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.subview.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.testsuite.entity.DocumentForEntityKeyMaps;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@EntityView(DocumentForEntityKeyMaps.class)
public interface SubviewSimpleDocumentForEntityKeyMapsView {
    @IdMapping
    Long getId();

    String getName();
}
