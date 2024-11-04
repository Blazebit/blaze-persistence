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
public class EmbeddableAttributeFetchGraphNode<X extends EmbeddableAttributeFetchGraphNode<X, T>, T extends FetchGraphNode<?>> implements FetchGraphNode<X> {

    protected final String attributeName;
    protected final String mapping;
    protected final T nestedGraphNode;

    public EmbeddableAttributeFetchGraphNode(String attributeName, String mapping, T nestedGraphNode) {
        this.attributeName = attributeName;
        this.mapping = mapping;
        this.nestedGraphNode = nestedGraphNode;
    }

    @Override
    public String getAttributeName() {
        return attributeName;
    }

    @Override
    public String getMapping() {
        return mapping;
    }

    @Override
    public void appendFetchJoinQueryFragment(String base, StringBuilder sb) {
        if (nestedGraphNode != null) {
            String newBase = base + "." + mapping;
            nestedGraphNode.appendFetchJoinQueryFragment(newBase, sb);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public FetchGraphNode<?> mergeWith(List<X> fetchGraphNodes) {
        List<T> nestedFlushers = new ArrayList<>(fetchGraphNodes.size());
        for (int i = 0; i < fetchGraphNodes.size(); i++) {
            X node = fetchGraphNodes.get(i);
            if (node.nestedGraphNode != null) {
                nestedFlushers.add(node.nestedGraphNode);
            }
        }

        if (nestedFlushers.isEmpty()) {
            return this;
        }
        T firstFlusher = nestedFlushers.get(0);
        FetchGraphNode<?> fetchGraphNode = firstFlusher.mergeWith((List) nestedFlushers);

        // All fetch graph nodes have the same structure, so no need for new objects
        if (fetchGraphNode == firstFlusher) {
            return this;
        }

        return new EmbeddableAttributeFetchGraphNode(attributeName, mapping, fetchGraphNode);
    }

}
