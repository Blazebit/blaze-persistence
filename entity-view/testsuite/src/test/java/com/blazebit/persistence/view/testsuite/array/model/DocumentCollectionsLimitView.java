/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.array.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Limit;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForCollections;
import com.blazebit.persistence.view.testsuite.collections.subview.model.SubviewPersonForCollectionsView;

/**
 *
 * @author Christian Beikov
 * @since 1.6.16
 */
@EntityView(DocumentForCollections.class)
public interface DocumentCollectionsLimitView {
    
    @IdMapping
    public Long getId();

    public String getName();

    @Mapping("contacts[partnerDocument.id = VIEW(owner.id)]")
    public SubviewPersonForCollectionsView getContactTest1();

    @Mapping("contacts[LENGTH(name) > 0]")
    public Map<Integer, SubviewPersonForCollectionsView> getContacts();

    @Mapping("partners[LENGTH(name) > 0]")
    public Set<SubviewPersonForCollectionsView> getPartners();

    @Mapping("personList[LENGTH(name) > 0]")
    public List<SubviewPersonForCollectionsView> getPersonList();

    @Limit(limit = "100", order = "id ASC")
    @Mapping("PersonForCollections[name = VIEW(name)]")
    public Set<SubviewPersonForCollectionsView> getCorrelatedJoin();

    @Limit(limit = "100", order = "id ASC")
    @Mapping(value = "PersonForCollections[name = VIEW(name)]", fetch = FetchStrategy.SELECT)
    public Set<SubviewPersonForCollectionsView> getCorrelatedSelect();

    @Limit(limit = "100", order = "id ASC")
    @Mapping(value = "PersonForCollections[name = VIEW(name)]", fetch = FetchStrategy.SUBSELECT)
    public Set<SubviewPersonForCollectionsView> getCorrelatedSubselect();

    @Limit(limit = "100", order = "id ASC")
    @Mapping(value = "PersonForCollections[name = VIEW(name)]", fetch = FetchStrategy.MULTISET)
    public Set<SubviewPersonForCollectionsView> getCorrelatedMultiset();
}
