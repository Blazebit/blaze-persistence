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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CompositeAttributeFetchGraphNode<X extends CompositeAttributeFetchGraphNode<X, T>, T extends FetchGraphNode<?>> implements FetchGraphNode<X> {

    protected final Class<?> viewType;
    protected final Map<String, Integer> attributeIndexMapping;
    protected final T[] flushers;
    protected final Boolean persist;

    public CompositeAttributeFetchGraphNode(Class<?> viewType, T[] flushers, Boolean persist) {
        this.viewType = viewType;
        this.flushers = flushers;
        this.attributeIndexMapping = createAttributeToIndexMapping(flushers);
        this.persist = persist;
    }

    public CompositeAttributeFetchGraphNode(Class<?> viewType, Map<String, Integer> attributeIndexMapping, T[] flushers, Boolean persist) {
        this.viewType = viewType;
        this.attributeIndexMapping = attributeIndexMapping;
        this.flushers = flushers;
        this.persist = persist;
    }

    private static Map<String, Integer> createAttributeToIndexMapping(FetchGraphNode[] flushers) {
        Map<String, Integer> map = new HashMap<>(flushers.length);
        for (int i = 0; i < flushers.length; i++) {
            map.put(flushers[i].getAttributeName(), i);
        }
        return map;
    }

    @Override
    public String getAttributeName() {
        return null;
    }

    @Override
    public void appendFetchJoinQueryFragment(String base, StringBuilder sb) {
        for (int i = 0; i < flushers.length; i++) {
            if (flushers[i] != null) {
                flushers[i].appendFetchJoinQueryFragment(base, sb);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public FetchGraphNode<?> mergeWith(List<X> fetchGraphNodes) {
        X firstNode = fetchGraphNodes.get(0);
        T[] firstFlushers = firstNode.flushers;
        Map<String, Integer> newAttributeIndexMapping = null;
        List<List<T>> newFlushers = null;

        // We try to be as smart as we can here
        // We assume the firstNode i.e. "this" node is the one covering the most flushers
        // We check flushers of other nodes against the firstNode and try to collect "new" flushers
        // In the end we need to merge all the flushers with each other to get the maximum fetch graph

        for (int i = 0; i < fetchGraphNodes.size(); i++) {
            X node = fetchGraphNodes.get(i);

            // Skip the full flusher(the one without specific persist info) and persist nodes as we can't load values for them
            // Note that the FULL flush mode doesn't do merging, so we can safely skip the full flushers here
            if (node.persist == null || node.persist) {
                continue;
            }

            // This might happen for attributes that can have subtypes for updates
            if (firstNode.viewType != node.viewType) {
                if (newAttributeIndexMapping == null) {
                    newAttributeIndexMapping = new HashMap<>(firstNode.attributeIndexMapping);
                    if (newFlushers == null) {
                        newFlushers = newFlushersList(firstFlushers);
                    }
                }

                addAttributeIndexMappings(newAttributeIndexMapping, node.attributeIndexMapping, newFlushers);

                T[] nodeFlushers = node.flushers;
                for (int j = 0; j < nodeFlushers.length; j++) {
                    T nodeFlusher = nodeFlushers[j];
                    if (nodeFlusher != null) {
                        Integer newIndex = newAttributeIndexMapping.get(nodeFlusher.getAttributeName());
                        List<T> flushers = newFlushers.get(newIndex);
                        if (!flushers.contains(nodeFlusher)) {
                            flushers.add(nodeFlusher);
                        }
                    }
                }
            } else {
                T[] nodeFlushers = node.flushers;
                for (int j = 0; j < firstFlushers.length; j++) {
                    if (firstFlushers[j] != nodeFlushers[j] && nodeFlushers[j] != null) {
                        T nodeFlusher = nodeFlushers[j];
                        if (newFlushers == null) {
                            newFlushers = newFlushersList(firstFlushers);
                            newFlushers.get(j).add(nodeFlusher);
                        } else {
                            List<T> flushers = newFlushers.get(j);
                            if (!flushers.contains(nodeFlusher)) {
                                flushers.add(nodeFlusher);
                            }
                        }
                    }
                }
            }
        }

        // If no new flushers were created, we can simply return this
        if (newFlushers == null) {
            return this;
        }

        // If a list was created, we have to merge the candidates, but keep track of changes
        T[] newFlusherArray = (T[]) new FetchGraphNode[newFlushers.size()];
        for (int i = 0; i < newFlushers.size(); i++) {
            List<T> flushers = newFlushers.get(i);
            if (!flushers.isEmpty()) {
                T firstFlusher = flushers.get(0);
                if (newAttributeIndexMapping == null && flushers.size() == 1) {
                    newFlusherArray[i] = firstFlusher;
                } else {
                    T newFlusher = (T) firstFlusher.mergeWith((List) flushers);
                    newFlusherArray[i] = newFlusher;
                }
            }
        }

        if (newAttributeIndexMapping == null) {
            return new CompositeAttributeFetchGraphNode<>(viewType, attributeIndexMapping, newFlusherArray, null);
        } else {
            return new CompositeAttributeFetchGraphNode<>(null, newAttributeIndexMapping, newFlusherArray, null);
        }

    }

    private static <T> void addAttributeIndexMappings(Map<String, Integer> newAttributeIndexMapping, Map<String, Integer> attributeIndexMapping, List<List<T>> newFlushers) {
        for (String newAttribute : attributeIndexMapping.keySet()) {
            if (!newAttributeIndexMapping.containsKey(newAttribute)) {
                newAttributeIndexMapping.put(newAttribute, newAttributeIndexMapping.size());
                newFlushers.add(new ArrayList<T>());
            }
        }
    }

    private static <T> List<List<T>> newFlushersList(T[] firstFlushers) {
        List<List<T>> newFlushers = new ArrayList<>(firstFlushers.length);

        for (int i = 0; i < firstFlushers.length; i++) {
            List<T> flushers = new ArrayList<>();
            if (firstFlushers[i] != null) {
                flushers.add(firstFlushers[i]);
            }
            newFlushers.add(flushers);
        }

        return newFlushers;
    }

}
