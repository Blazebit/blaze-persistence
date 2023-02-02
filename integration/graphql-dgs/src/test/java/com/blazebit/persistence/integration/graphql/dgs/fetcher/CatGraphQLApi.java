/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.integration.graphql.dgs.fetcher;

import com.blazebit.persistence.integration.graphql.dgs.repository.CatViewRepository;
import com.blazebit.persistence.integration.graphql.dgs.view.CatCreateView;
import com.blazebit.persistence.integration.graphql.dgs.view.CatWithOwnerView;
import com.blazebit.persistence.integration.graphql.GraphQLEntityViewSupport;
import com.blazebit.persistence.integration.graphql.GraphQLRelayConnection;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.Sorters;
import com.netflix.graphql.dgs.*;
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

    @DgsData(parentType = "CatWithOwnerViewNode", field = "theData")
    public String getNodeData(DataFetchingEnvironment dataFetchingEnvironment) {
        Object source = dataFetchingEnvironment.getSource();
        if (source instanceof CatWithOwnerView) {
            return ((CatWithOwnerView) source).abc();
        }
        return null;
    }
}
