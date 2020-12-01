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

package com.blazebit.persistence.spring.data.impl.repository;


import org.springframework.data.repository.core.support.QueryCreationListener;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.5.0
 */
public class QueryCollectingQueryCreationListener implements QueryCreationListener<RepositoryQuery> {

    private final List<QueryMethod> queryMethods = new ArrayList<>();

    @Override
    public void onCreation(RepositoryQuery query) {
        this.queryMethods.add(query.getQueryMethod());
    }

    public List<QueryMethod> getQueryMethods() {
        return queryMethods;
    }
}
