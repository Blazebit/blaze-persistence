/*
 * Copyright 2014 - 2022 Blazebit.
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

package com.blazebit.persistence.view.testsuite.multiset.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.testsuite.collections.entity.simple.DocumentForCollections;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
@UpdatableEntityView
@EntityView(DocumentForCollections.class)
public interface SubviewDocumentMultisetFetchView {

    @IdMapping
    public Long getId();

    public String getName();

    @MappingParameter("test")
    public Locale getTest();

    public Set<? extends SubviewPersonForCollectionsMultisetFetchView> getPartners();

    public List<? extends SubviewPersonForCollectionsMultisetFetchView> getPersonList();

    public Map<Integer, ? extends SubviewPersonForCollectionsMultisetFetchView> getContacts();
}
