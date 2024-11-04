/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.jaxrs.jsonb.testsuite.view;

import com.blazebit.persistence.integration.jaxrs.jsonb.testsuite.entity.Document;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingParameter;

/**
 * @author Moritz Becker
 * @since 1.6.4
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
