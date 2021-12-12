/*
 * Copyright 2014 - 2021 Blazebit.
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

package com.blazebit.persistence.examples.spring.data.spqr.resource;

import com.blazebit.persistence.examples.spring.data.spqr.repository.CatViewRepository;
import com.blazebit.persistence.examples.spring.data.spqr.view.CatCreateView;
import com.blazebit.persistence.examples.spring.data.spqr.view.CatWithOwnerView;
import com.blazebit.persistence.integration.graphql.GraphQLEntityViewSupport;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.Sorters;
import graphql.relay.Connection;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.execution.relay.Page;
import io.leangen.graphql.execution.relay.generic.GenericPage;
import io.leangen.graphql.spqr.spring.annotations.GraphQLApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author Christian Beikov
 * @since 1.6.4
 */
@Component
@GraphQLApi
public class CatGraphQLApi {

    @Autowired
    EntityViewManager evm;
    @Autowired
    CatViewRepository repository;
    @Autowired
    @Lazy
    GraphQLEntityViewSupport graphQLEntityViewSupport;

    @GraphQLQuery
    public CatWithOwnerView catById(@GraphQLArgument(name = "id") Long id, @GraphQLEnvironment ResolutionEnvironment env) {
        return repository.findById(graphQLEntityViewSupport.createSetting(env.dataFetchingEnvironment), id);
    }

    @GraphQLQuery
    public Page<CatWithOwnerView> findAll(
            @GraphQLArgument(name = "first") Integer first,
            @GraphQLArgument(name = "last") Integer last,
            @GraphQLArgument(name = "offset") Integer offset,
            @GraphQLArgument(name = "before") String before,
            @GraphQLArgument(name = "after") String after,
            @GraphQLEnvironment ResolutionEnvironment env) {
        EntityViewSetting<CatWithOwnerView, ?> setting = graphQLEntityViewSupport.createPaginatedSetting(env.dataFetchingEnvironment, first, last, offset, before, after);
        setting.addAttributeSorter("id", Sorters.ascending());
        Connection<CatWithOwnerView> relayConnection = graphQLEntityViewSupport.createRelayConnection(repository.findAll(setting));
        return new GenericPage<>(relayConnection.getEdges(), relayConnection.getPageInfo());
    }

    @GraphQLMutation
    public Long createCat(@GraphQLArgument(name = "cat") CatCreateView cat) {
        repository.save(cat);
        return cat.getId();
    }
}
