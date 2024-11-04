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
public abstract class BasicDocumentListMapSetView implements BasicDocumentCollectionsView {

    @Mapping("personList")
    public abstract List<PersonForCollections> getA();

    @Mapping("contacts")
    public abstract Map<Integer, PersonForCollections> getB();

    @Mapping("partners")
    public abstract Set<PersonForCollections> getC();

    @Override
    public Map<Integer, PersonForCollections> getContacts() {
        return getB();
    }

    @Override
    public Set<PersonForCollections> getPartners() {
        return getC();
    }

    @Override
    public List<PersonForCollections> getPersonList() {
        return getA();
    }
}
