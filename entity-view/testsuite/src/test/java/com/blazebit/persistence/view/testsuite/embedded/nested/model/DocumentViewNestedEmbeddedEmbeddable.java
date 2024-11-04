/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.embedded.nested.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.testsuite.entity.Document;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Document.class)
public interface DocumentViewNestedEmbeddedEmbeddable {

    @Mapping("this")
    DocumentDetailNestedView getDetails();

    @Mapping("this")
    DocumentDetailEmbeddableNestedView getEmbeddedDetails();

}
