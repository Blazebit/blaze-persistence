/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.basic.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(Document.class)
public interface DocumentViewInterface extends IdHolderView<Long> {

    String getName();

    @MappingSubquery(CountSubqueryProvider.class)
    Long getContactCount();

    @Mapping("contacts2[:contactPersonNumber]")
    Person getMyContactPerson();

    @Mapping("contacts[1]")
    Person getFirstContactPerson();

    @MappingParameter("contactPersonNumber")
    Integer getContactPersonNumber2();
}
