/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.array.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForCollections;
import com.blazebit.persistence.view.testsuite.collections.subview.model.SubviewPersonForCollectionsView;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
@EntityView(DocumentForCollections.class)
public interface SubviewDocumentCollectionsView {
    
    @IdMapping
    public Long getId();

    public String getName();

    // This is for #1139
    @Mapping("contacts[partnerDocument.id = VIEW(owner.id)]")
    public SubviewPersonForCollectionsView getContactTest1();

    @Mapping("contacts[LENGTH(name) > 0]")
    public Map<Integer, SubviewPersonForCollectionsView> getContacts();

    @Mapping("partners[LENGTH(name) > 0]")
    public Set<SubviewPersonForCollectionsView> getPartners();

    @Mapping("personList[LENGTH(name) > 0]")
    public List<SubviewPersonForCollectionsView> getPersonList();

    @Mapping("PersonForCollections[name = VIEW(name)]")
    public Set<SubviewPersonForCollectionsView> getCorrelatedJoin();

    @Mapping(value = "PersonForCollections[name = VIEW(name)]", fetch = FetchStrategy.SELECT)
    public Set<SubviewPersonForCollectionsView> getCorrelatedSelect();

    @Mapping(value = "PersonForCollections[name = VIEW(name)]", fetch = FetchStrategy.SUBSELECT)
    public Set<SubviewPersonForCollectionsView> getCorrelatedSubselect();

    @Mapping(value = "PersonForCollections[name = VIEW(name)]", fetch = FetchStrategy.MULTISET)
    public Set<SubviewPersonForCollectionsView> getCorrelatedMultiset();
}
