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

package com.blazebit.persistence.view.impl.update.flush;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AttributeFetchGraphNode<X extends AttributeFetchGraphNode<X>> implements FetchGraphNode<X> {

    protected final String attributeName;
    protected final String mapping;
    protected final boolean fetch;
    protected final FetchGraphNode<?> nestedGraphNode;

    public AttributeFetchGraphNode(String attributeName, String mapping, boolean fetch, FetchGraphNode nestedGraphNode) {
        this.fetch = fetch;
        this.attributeName = attributeName;
        this.mapping = mapping;
        this.nestedGraphNode = nestedGraphNode;
    }

    @Override
    public String getAttributeName() {
        return attributeName;
    }

    @Override
    public void appendFetchJoinQueryFragment(String base, StringBuilder sb) {
        if (fetch && mapping != null) {
            String newBase = base.replace('.', '_') + "_" + attributeName;
            sb.append(" LEFT JOIN FETCH ")
                    .append(base)
                    .append('.')
                    .append(mapping)
                    .append(" ")
                    .append(newBase);

            if (nestedGraphNode != null) {
                nestedGraphNode.appendFetchJoinQueryFragment(newBase, sb);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public FetchGraphNode<?> mergeWith(List<X> fetchGraphNodes) {
        boolean fetchChanged = false;
        List<FetchGraphNode<?>> nestedFlushers = new ArrayList<>(fetchGraphNodes.size());
        for (int i = 0; i < fetchGraphNodes.size(); i++) {
            X node = fetchGraphNodes.get(i);
            fetchChanged |= this.fetch != node.fetch;
            if (node.nestedGraphNode != null) {
                nestedFlushers.add(node.nestedGraphNode);
            }
        }

        final boolean newFetch = fetchChanged || this.fetch;

        if (nestedFlushers.isEmpty()) {
            if (fetchChanged && this.fetch != newFetch) {
                return new AttributeFetchGraphNode<>(attributeName, mapping, newFetch, fetchGraphNodes.get(0));
            } else {
                return this;
            }
        }
        FetchGraphNode<?> firstFlusher = nestedFlushers.get(0);
        FetchGraphNode<?> fetchGraphNode = firstFlusher.mergeWith((List) nestedFlushers);

        // All fetch graph nodes have the same structure, so no need for new objects
        if (!fetchChanged && fetchGraphNode == firstFlusher) {
            return this;
        }

        return new AttributeFetchGraphNode<>(attributeName, mapping, newFetch, fetchGraphNode);
    }

}
