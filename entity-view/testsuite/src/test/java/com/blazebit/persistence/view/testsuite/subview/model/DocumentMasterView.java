/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.subview.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.testsuite.entity.Document;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(Document.class)
public interface DocumentMasterView {
    
    @IdMapping
    public Long getId();

    public String getName();

    public PersonSubView getOwner();

    @MappingParameter("contactPersonNumber")
    public Integer getContactPersonNumber();

    @Mapping("contacts[:contactPersonNumber]")
    public PersonSubViewFiltered getMyContactPerson();

    @MappingParameter("contactPersonNumber")
    public Integer getTheContactPersonNumber();

    @Mapping("contacts2")
    public Map<Integer, PersonSubView> getContacts();

    @Mapping("partners")
    public Set<PersonSubView> getPartners();

    public List<PersonSubView> getPeople();
}
