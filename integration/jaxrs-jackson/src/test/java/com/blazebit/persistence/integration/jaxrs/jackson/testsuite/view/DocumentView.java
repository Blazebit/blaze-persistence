/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.jaxrs.jackson.testsuite.view;

import com.blazebit.persistence.integration.jaxrs.jackson.testsuite.entity.Document;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingParameter;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@EntityView(Document.class)
public interface DocumentView {

    @IdMapping
    Long getId();

    String getName();

    PersonView getOwner();

    @Mapping("size(owner.documents)")
    long getOwnerDocumentCount();

    @MappingParameter("optionalParameter")
    String getOptionalParameter();
}
