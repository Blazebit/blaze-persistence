/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.flush;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CollectionElementFetchGraphNode<X extends CollectionElementFetchGraphNode<X, T>, T extends FetchGraphNode<?>> implements FetchGraphNode<X> {

    protected final T nestedGraphNode;

    public CollectionElementFetchGraphNode(T nestedGraphNode) {
        this.nestedGraphNode = nestedGraphNode;
    }

    @Override
    public String getAttributeName() {
        return null;
    }

    @Override
    public String getMapping() {
        return null;
    }

    @Override
    public void appendFetchJoinQueryFragment(String base, StringBuilder sb) {
        nestedGraphNode.appendFetchJoinQueryFragment(base, sb);
    }

    @Override
    @SuppressWarnings("unchecked")
    public FetchGraphNode<?> mergeWith(List<X> fetchGraphNodes) {
        List<T> nestedNodes = new ArrayList<>(fetchGraphNodes.size());
        for (int i = 0; i < fetchGraphNodes.size(); i++) {
            X node = fetchGraphNodes.get(i);
            if (node.nestedGraphNode != null) {
                nestedNodes.add(node.nestedGraphNode);
            }
        }

        if (nestedNodes.isEmpty()) {
            return this;
        }
        T firstNode = nestedNodes.get(0);
        FetchGraphNode<?> fetchGraphNode = firstNode.mergeWith((List) nestedNodes);
        if (fetchGraphNode == firstNode) {
            return this;
        }

        return new CollectionElementFetchGraphNode<>(fetchGraphNode);
    }
}
