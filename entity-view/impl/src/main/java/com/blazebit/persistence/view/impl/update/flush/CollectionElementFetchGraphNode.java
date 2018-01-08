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
