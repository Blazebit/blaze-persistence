/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.basic.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;

import java.util.List;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Document.class)
public interface FetchesDocumentView3 extends IdHolderView<Long> {

    @Mapping(fetches = "localized")
    Map<Integer, Person> getContacts();

    @Mapping(fetches = "localized")
    List<Person> getPeople();
}
