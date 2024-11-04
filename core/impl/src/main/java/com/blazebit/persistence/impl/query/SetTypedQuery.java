/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.query;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class SetTypedQuery<X> extends TypedQueryWrapper<X> {

    private final List<Query> queryParts;

    @SuppressWarnings("unchecked")
    public SetTypedQuery(Query delegate, List<Query> queryParts) {
        super((TypedQuery<X>) delegate, null);
        this.queryParts = queryParts;
    }

    public Query getQueryPart(int index) {
        if (index == 0) {
            return delegate;
        }
        return queryParts.get(index - 1);
    }
}
