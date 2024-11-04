/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.basic.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForCollections;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.PersonForCollections;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(DocumentForCollections.class)
public abstract class BasicDocumentSetMapListView implements BasicDocumentCollectionsView {

    @Mapping("partners")
    public abstract Set<PersonForCollections> getA();

    @Mapping("contacts")
    public abstract Map<Integer, PersonForCollections> getB();

    @Mapping("personList")
    public abstract List<PersonForCollections> getC();

    @Mapping("CASE WHEN personList IS EMPTY THEN true ELSE false END")
    public abstract Boolean getIsPersonListEmpty();

    @Mapping("SIZE(personList)")
    public abstract Long getPersonListSize();

    @Mapping("personList[1]")
    public abstract PersonForCollections getFirstPerson();

    @Override
    public Map<Integer, PersonForCollections> getContacts() {
        return getB();
    }

    @Override
    public Set<PersonForCollections> getPartners() {
        return getA();
    }

    @Override
    public List<PersonForCollections> getPersonList() {
        return getC();
    }
}
