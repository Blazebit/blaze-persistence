/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
    @MappingIndex("versionIdx - 1")
    public List<VersionViewWithMappingIndex> getVersions();

    @Mapping(value = "versions[0 = 0]", fetch = FetchStrategy.JOIN)
    @MappingIndex("versionIdx")
    public Map<Integer, VersionViewWithMappingIndex> getVersionMap();

    @Mapping(value = "versions[1 = 1]", fetch = FetchStrategy.JOIN)
    @MappingIndex("this")
    public Map<VersionKeyView, VersionViewWithMappingIndex> getVersionMap2();

    @Mapping(value = "versions[2 = 2]", fetch = FetchStrategy.JOIN)
    // Good job Datanucleus.. https://github.com/datanucleus/datanucleus-core/issues/356
    @MappingIndex("versionIdx - versionIdx")
    @MultiCollectionMapping(comparator = VersionViewWithMappingIndex.DefaultComparator.class)
    public List<SortedSet<VersionViewWithMappingIndex>> getMultiVersions();

    @Mapping(value = "versions[3 = 3]", fetch = FetchStrategy.JOIN)
    // Good job Datanucleus.. https://github.com/datanucleus/datanucleus-core/issues/356
    @MappingIndex("versionIdx - versionIdx")
    @MultiCollectionMapping(comparator = VersionViewWithMappingIndex.DefaultComparator.class)
    public Map<Integer, SortedSet<VersionViewWithMappingIndex>> getMultiVersionMap();

    @Mapping(value = "versions[4 = 4]", fetch = FetchStrategy.JOIN)
    @MappingIndex("this")
    @MultiCollectionMapping(comparator = VersionViewWithMappingIndex.DefaultComparator.class)
    public Map<VersionStaticKeyView, SortedSet<VersionViewWithMappingIndex>> getMultiVersionMap2();
}
