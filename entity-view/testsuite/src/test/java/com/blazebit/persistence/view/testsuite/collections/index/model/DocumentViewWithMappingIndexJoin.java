/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.view.testsuite.collections.index.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingIndex;
import com.blazebit.persistence.view.MultiCollectionMapping;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
@EntityView(Document.class)
public interface DocumentViewWithMappingIndexJoin extends DocumentViewWithMappingIndex {
    
    @Mapping(value = "versions", fetch = FetchStrategy.JOIN)
    @MappingIndex("idx - 1")
    public List<VersionViewWithMappingIndex> getVersions();

    @Mapping(value = "versions[0 = 0]", fetch = FetchStrategy.JOIN)
    @MappingIndex("idx")
    public Map<Integer, VersionViewWithMappingIndex> getVersionMap();

    @Mapping(value = "versions[1 = 1]", fetch = FetchStrategy.JOIN)
    @MappingIndex("this")
    public Map<VersionKeyView, VersionViewWithMappingIndex> getVersionMap2();

    @Mapping(value = "versions[2 = 2]", fetch = FetchStrategy.JOIN)
    // Good job Datanucleus.. https://github.com/datanucleus/datanucleus-core/issues/356
    @MappingIndex("idx - idx")
    @MultiCollectionMapping(comparator = VersionViewWithMappingIndex.DefaultComparator.class)
    public List<SortedSet<VersionViewWithMappingIndex>> getMultiVersions();

    @Mapping(value = "versions[3 = 3]", fetch = FetchStrategy.JOIN)
    // Good job Datanucleus.. https://github.com/datanucleus/datanucleus-core/issues/356
    @MappingIndex("idx - idx")
    @MultiCollectionMapping(comparator = VersionViewWithMappingIndex.DefaultComparator.class)
    public Map<Integer, SortedSet<VersionViewWithMappingIndex>> getMultiVersionMap();

    @Mapping(value = "versions[4 = 4]", fetch = FetchStrategy.JOIN)
    @MappingIndex("this")
    @MultiCollectionMapping(comparator = VersionViewWithMappingIndex.DefaultComparator.class)
    public Map<VersionStaticKeyView, SortedSet<VersionViewWithMappingIndex>> getMultiVersionMap2();
}
