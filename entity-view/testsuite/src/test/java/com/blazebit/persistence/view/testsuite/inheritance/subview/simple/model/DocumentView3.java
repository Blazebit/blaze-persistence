/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.inheritance.subview.simple.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.MappingInheritance;
import com.blazebit.persistence.view.MappingInheritanceSubtype;
import com.blazebit.persistence.testsuite.entity.Document;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Document.class)
public interface DocumentView3 extends SimpleDocumentView {

    @MappingInheritance(onlySubtypes = true, value = {
            @MappingInheritanceSubtype(mapping = "age < 16", value = YoungPersonView3.class)
    })
    public PersonBaseView3 getOwner();
}
