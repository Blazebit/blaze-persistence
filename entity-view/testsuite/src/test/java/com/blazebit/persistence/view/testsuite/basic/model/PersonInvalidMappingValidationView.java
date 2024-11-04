/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.basic.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.testsuite.entity.Person;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(Person.class)
public interface PersonInvalidMappingValidationView extends IdHolderView<Long> {

    // defaultContact is mapped on Document, but not on Person
    @Mapping("partnerDocument.contacts[defaultContact].name")
    public String getName();
}
