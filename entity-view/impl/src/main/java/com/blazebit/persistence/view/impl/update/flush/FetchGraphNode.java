/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.flush;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface FetchGraphNode<T extends FetchGraphNode<T>> {

    public String getAttributeName();

    public String getMapping();

    public void appendFetchJoinQueryFragment(String base, StringBuilder sb);
    
    public FetchGraphNode<?> mergeWith(List<T> fetchGraphNodes);
}
