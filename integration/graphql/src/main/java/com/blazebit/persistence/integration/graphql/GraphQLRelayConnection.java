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

package com.blazebit.persistence.integration.graphql;

import com.blazebit.persistence.Keyset;
import com.blazebit.persistence.PagedList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * A GraphQL relay connection.
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class GraphQLRelayConnection<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<GraphQLRelayEdge<T>> edges;
    private final GraphQLRelayPageInfo pageInfo;
    private final long totalCount;

    /**
     * Creates an empty GraphQL page.
     */
    public GraphQLRelayConnection() {
        this.edges = Collections.emptyList();
        this.pageInfo = GraphQLRelayPageInfo.EMPTY;
        this.totalCount = 0;
    }

    /**
     * Creates a new GraphQL page from the given list.
     *
     * @param list The list
     */
    public GraphQLRelayConnection(List<T> list) {
        if (list instanceof PagedList<?>) {
            PagedList<T> data = (PagedList<T>) list;
            this.pageInfo = new GraphQLRelayPageInfo(data);
            this.totalCount = data.getTotalSize();
        } else {
            this.pageInfo = GraphQLRelayPageInfo.EMPTY;
            this.totalCount = list.size();
        }
        this.edges = createEdges(list, pageInfo);
    }

    /**
     * Creates a GraphQL page from the given paged list.
     *
     * @param list The paged list
     */
    public GraphQLRelayConnection(PagedList<T> list) {
        this.pageInfo = new GraphQLRelayPageInfo(list);
        this.totalCount = list.getTotalSize();
        this.edges = createEdges(list, pageInfo);
    }

    private static <X> List<GraphQLRelayEdge<X>> createEdges(List<X> list, GraphQLRelayPageInfo pageInfo) {
        List<GraphQLRelayEdge<X>> edges;
        List<Keyset> keysets;
        if (list instanceof PagedList<?> && pageInfo != null && ((PagedList<?>) list).getKeysetPage() != null && (keysets = ((PagedList<?>) list).getKeysetPage().getKeysets()).size() == list.size()) {
            PagedList<?> pagedList = (PagedList<?>) list;
            int offset = pagedList.getFirstResult();
            int pageSize = pagedList.getMaxResults();
            edges = new ArrayList<>(list.size());
            for (int i = 0; i < list.size(); i++) {
                X node = list.get(i);
                edges.add(new GraphQLRelayEdge<>(Base64.getEncoder().encodeToString(pageInfo.serialize(offset, pageSize, keysets.get(i).getTuple())), node));
            }
        } else {
            edges = new ArrayList<>(list.size());
            for (int i = 0; i < list.size(); i++) {
                X node = list.get(i);
                edges.add(new GraphQLRelayEdge<>(null, node));
            }
        }
        return edges;
    }

    /**
     * Returns the elements on the page.
     *
     * @return the elements
     */
    public List<GraphQLRelayEdge<T>> getEdges() {
        return edges;
    }

    /**
     * Returns the page info for this page.
     *
     * @return the page info
     */
    public GraphQLRelayPageInfo getPageInfo() {
        return pageInfo;
    }

    /**
     * Returns the total count of elements available.
     *
     * @return the total count
     */
    public long getTotalCount() {
        return totalCount;
    }

}
