/*
 * Copyright 2014 - 2024 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
