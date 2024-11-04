/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.index.model;

import com.blazebit.persistence.view.IdMapping;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface DocumentViewWithMappingIndex {
    
    @IdMapping
    public Long getId();

    public String getName();

    public List<VersionViewWithMappingIndex> getVersions();

    public Map<Integer, VersionViewWithMappingIndex> getVersionMap();

    public Map<VersionKeyView, VersionViewWithMappingIndex> getVersionMap2();

    public List<SortedSet<VersionViewWithMappingIndex>> getMultiVersions();

    public Map<Integer, SortedSet<VersionViewWithMappingIndex>> getMultiVersionMap();

    public Map<VersionStaticKeyView, SortedSet<VersionViewWithMappingIndex>> getMultiVersionMap2();
}
