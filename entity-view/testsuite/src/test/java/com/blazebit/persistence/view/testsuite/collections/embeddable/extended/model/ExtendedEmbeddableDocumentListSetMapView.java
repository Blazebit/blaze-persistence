/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.view.testsuite.collections.embeddable.extended.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.collections.entity.extended.ExtendedDocumentForElementCollections;
import com.blazebit.persistence.view.testsuite.collections.entity.extended.ExtendedPersonForElementCollections;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(ExtendedDocumentForElementCollections.class)
public abstract class ExtendedEmbeddableDocumentListSetMapView implements ExtendedEmbeddableDocumentCollectionsView {

    @Mapping("personList")
    public abstract List<ExtendedPersonForElementCollections> getA();

    @Mapping("partners")
    public abstract Set<ExtendedPersonForElementCollections> getB();

    @Mapping("contacts")
    public abstract Map<Integer, ExtendedPersonForElementCollections> getC();

    @Override
    public Map<Integer, ExtendedPersonForElementCollections> getContacts() {
        return getC();
    }

    @Override
    public Set<ExtendedPersonForElementCollections> getPartners() {
        return getB();
    }

    @Override
    public List<ExtendedPersonForElementCollections> getPersonList() {
        return getA();
    }
}
