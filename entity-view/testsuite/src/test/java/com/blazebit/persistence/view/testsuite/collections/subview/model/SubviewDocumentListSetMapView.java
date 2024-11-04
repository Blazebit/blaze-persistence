/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.subview.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForCollections;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(DocumentForCollections.class)
public abstract class SubviewDocumentListSetMapView implements SubviewDocumentCollectionsView {

    @Mapping("personList")
    public abstract List<SubviewPersonForCollectionsView> getA();

    @Mapping("partners")
    public abstract Set<SubviewPersonForCollectionsView> getB();

    @Mapping("contacts")
    public abstract Map<Integer, SubviewPersonForCollectionsView> getC();

    @Override
    public Map<Integer, SubviewPersonForCollectionsView> getContacts() {
        return getC();
    }

    @Override
    public Set<SubviewPersonForCollectionsView> getPartners() {
        return getB();
    }

    @Override
    public List<SubviewPersonForCollectionsView> getPersonList() {
        return getA();
    }
}
