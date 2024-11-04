/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.dgs.fetcher;

import com.blazebit.persistence.examples.spring.data.dgs.repository.CatViewRepository;
import com.blazebit.persistence.examples.spring.data.dgs.view.CatCreateView;
import com.blazebit.persistence.examples.spring.data.dgs.view.CatWithOwnerView;
import com.blazebit.persistence.integration.graphql.GraphQLEntityViewSupport;
import com.blazebit.persistence.integration.graphql.GraphQLRelayConnection;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.Sorters;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

/**
 * @author Christian Beikov
 * @since 1.6.2
 */
@DgsComponent
public class CatGraphQLApi {

    @Autowired
    CatViewRepository repository;
    @Autowired
    GraphQLEntityViewSupport graphQLEntityViewSupport;

    @DgsQuery
    public CatWithOwnerView catById(@InputArgument("id") Long id, DataFetchingEnvironment dataFetchingEnvironment) {
        return repository.findById(graphQLEntityViewSupport.createSetting(dataFetchingEnvironment), dataFetchingEnvironment.getArgument("id"));
    }

    @DgsQuery
    public GraphQLRelayConnection<CatWithOwnerView> findAll(DataFetchingEnvironment dataFetchingEnvironment) {
        EntityViewSetting<CatWithOwnerView, ?> setting = graphQLEntityViewSupport.createPaginatedSetting(dataFetchingEnvironment);
        setting.addAttributeSorter("id", Sorters.ascending());
        if (setting.getMaxResults() == 0) {
            return new GraphQLRelayConnection<>(Collections.emptyList());
        }
        return new GraphQLRelayConnection<>(repository.findAll(setting));
    }

    @DgsMutation
    public Long createCat(@InputArgument(name = "cat") CatCreateView cat) {
        repository.save(cat);
        return cat.getId();
    }

    // Even though the CatWithOwnerView type will have a field for the name "theData",
    // the DGS runtime can't access the field through the "abc" method,
    // so we need to add dedicated DataFetcher here

    @DgsData(parentType = "CatWithOwnerView", field = "theData")
    public String getData(DataFetchingEnvironment dataFetchingEnvironment) {
        Object source = dataFetchingEnvironment.getSource();
        if (source instanceof CatWithOwnerView) {
            return ((CatWithOwnerView) source).abc();
        }
        return null;
    }
}
