/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
