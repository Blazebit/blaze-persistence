/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.testsuite.webmvc.view;

import com.blazebit.persistence.spring.data.testsuite.webmvc.entity.Document;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingParameter;

import java.time.Instant;

/**
 * @author Moritz Becker
 * @since 1.5.0
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

    default Instant getSomeInstant() {
        return Instant.now();
    }
}
