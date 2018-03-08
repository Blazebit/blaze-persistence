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
public abstract class BasicDocumentMapListSetView implements BasicDocumentCollectionsView {

    @Mapping("contacts")
    public abstract Map<Integer, PersonForCollections> getA();

    @Mapping("personList")
    public abstract List<PersonForCollections> getB();

    @Mapping("partners")
    public abstract Set<PersonForCollections> getC();

    @Override
    public Map<Integer, PersonForCollections> getContacts() {
        return getA();
    }

    @Override
    public Set<PersonForCollections> getPartners() {
        return getC();
    }

    @Override
    public List<PersonForCollections> getPersonList() {
        return getB();
    }
}
