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
public interface DocumentViewWithMappingIndexSubselect extends DocumentViewWithMappingIndex {
    
    @Mapping(value = "versions", fetch = FetchStrategy.SUBSELECT)
    @MappingIndex("versionIdx - 1")
    public List<VersionViewWithMappingIndex> getVersions();

    @Mapping(value = "versions", fetch = FetchStrategy.SUBSELECT)
    @MappingIndex("versionIdx")
    public Map<Integer, VersionViewWithMappingIndex> getVersionMap();

    @Mapping(value = "versions", fetch = FetchStrategy.SUBSELECT)
    @MappingIndex("this")
    public Map<VersionKeyView, VersionViewWithMappingIndex> getVersionMap2();

    @Mapping(value = "versions", fetch = FetchStrategy.SUBSELECT)
    // Good job Datanucleus.. https://github.com/datanucleus/datanucleus-core/issues/356
    @MappingIndex("versionIdx - versionIdx")
    @MultiCollectionMapping(comparator = VersionViewWithMappingIndex.DefaultComparator.class)
    public List<SortedSet<VersionViewWithMappingIndex>> getMultiVersions();

    @Mapping(value = "versions", fetch = FetchStrategy.SUBSELECT)
    // Good job Datanucleus.. https://github.com/datanucleus/datanucleus-core/issues/356
    @MappingIndex("versionIdx - versionIdx")
    @MultiCollectionMapping(comparator = VersionViewWithMappingIndex.DefaultComparator.class)
    public Map<Integer, SortedSet<VersionViewWithMappingIndex>> getMultiVersionMap();

    @Mapping(value = "versions", fetch = FetchStrategy.SUBSELECT)
    @MappingIndex("this")
    @MultiCollectionMapping(comparator = VersionViewWithMappingIndex.DefaultComparator.class)
    public Map<VersionStaticKeyView, SortedSet<VersionViewWithMappingIndex>> getMultiVersionMap2();
}
