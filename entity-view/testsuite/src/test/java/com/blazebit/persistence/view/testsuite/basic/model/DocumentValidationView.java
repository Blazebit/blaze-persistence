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
public interface DocumentValidationView extends IdHolderView<Long> {

    public String getName();

    @MappingSubquery(CountSubqueryProvider.class)
    public Long getContactCount();

    @Mapping("contacts2[:contactPersonNumber]")
    public Person getMyContactPerson();

    @Mapping("contacts[1]")
    public Person getFirstContactPerson();

    @MappingParameter("contactPersonNumber")
    public Integer getContactPersonNumber2();

    @Mapping("CASE WHEN partners IS NOT EMPTY THEN true ELSE false END")
    public Boolean getExistsPartners();
}
