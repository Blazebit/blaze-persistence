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
public abstract class BasicDocumentMapSetListView implements BasicDocumentCollectionsView {

    @Mapping("contacts")
    public abstract Map<Integer, PersonForCollections> getA();

    @Mapping("partners")
    public abstract Set<PersonForCollections> getB();

    @Mapping("personList")
    public abstract List<PersonForCollections> getC();

    @Override
    public Map<Integer, PersonForCollections> getContacts() {
        return getA();
    }

    @Override
    public Set<PersonForCollections> getPartners() {
        return getB();
    }

    @Override
    public List<PersonForCollections> getPersonList() {
        return getC();
    }
}
