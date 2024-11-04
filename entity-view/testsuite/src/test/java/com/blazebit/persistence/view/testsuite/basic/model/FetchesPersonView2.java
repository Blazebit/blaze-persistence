/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.basic.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;

import java.util.Set;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@EntityView(Person.class)
public interface FetchesPersonView2 extends IdHolderView<Long> {

    @Mapping(fetches = "ownedDocuments")
    Person getFriend();

    @Mapping(fetches = "contacts")
    Set<Document> getOwnedDocuments();

    FetchesDocumentView2 getPartnerDocument();
}
