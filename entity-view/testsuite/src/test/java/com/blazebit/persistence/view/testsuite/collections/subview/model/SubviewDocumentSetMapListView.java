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
public abstract class SubviewDocumentSetMapListView implements SubviewDocumentCollectionsView {

    @Mapping("partners")
    public abstract Set<SubviewPersonForCollectionsView> getA();

    @Mapping("contacts")
    public abstract Map<Integer, SubviewPersonForCollectionsView> getB();

    @Mapping("personList")
    public abstract List<SubviewPersonForCollectionsView> getC();

    @Override
    public Map<Integer, SubviewPersonForCollectionsView> getContacts() {
        return getB();
    }

    @Override
    public Set<SubviewPersonForCollectionsView> getPartners() {
        return getA();
    }

    @Override
    public List<SubviewPersonForCollectionsView> getPersonList() {
        return getC();
    }
}
