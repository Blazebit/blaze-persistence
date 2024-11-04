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
public abstract class SubviewDocumentMapListSetView implements SubviewDocumentCollectionsView {

    @Mapping("contacts")
    public abstract Map<Integer, SubviewPersonForCollectionsView> getA();

    @Mapping("personList")
    public abstract List<SubviewPersonForCollectionsView> getB();

    @Mapping("partners")
    public abstract Set<SubviewPersonForCollectionsView> getC();

    @Override
    public Map<Integer, SubviewPersonForCollectionsView> getContacts() {
        return getA();
    }

    @Override
    public Set<SubviewPersonForCollectionsView> getPartners() {
        return getC();
    }

    @Override
    public List<SubviewPersonForCollectionsView> getPersonList() {
        return getB();
    }
}
