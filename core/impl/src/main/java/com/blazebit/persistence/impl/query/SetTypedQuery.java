/*
 * Copyright 2014 - 2022 Blazebit.
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
