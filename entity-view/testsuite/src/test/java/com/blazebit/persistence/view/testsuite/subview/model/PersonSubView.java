/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.subview.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.testsuite.entity.Person;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(Person.class)
public interface PersonSubView extends SimplePersonSubView {

    // Although it might not be used, we add it to cover array expressions in subviews
    @Mapping("localized[1]")
    public String getFirstLocalized();

    @Mapping("VIEW_ROOT()")
    public SimpleDocumentView getRoot();

    @Mapping("EMBEDDING_VIEW()")
    public SimpleDocumentView getParent();
}
