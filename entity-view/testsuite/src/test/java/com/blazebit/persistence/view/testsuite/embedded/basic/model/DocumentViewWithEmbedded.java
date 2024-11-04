/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.embedded.basic.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.basic.model.IdHolderView;
import com.blazebit.persistence.testsuite.entity.Document;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Document.class)
public interface DocumentViewWithEmbedded extends IdHolderView<Long> {

    @Mapping("this")
    DocumentDetailView getDetails();

    @Mapping("this")
    DocumentDetailEmbeddableView getEmbeddedDetails();

}
