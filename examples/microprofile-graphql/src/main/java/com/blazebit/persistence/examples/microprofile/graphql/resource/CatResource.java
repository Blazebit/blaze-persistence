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

package com.blazebit.persistence.examples.microprofile.graphql.resource;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.examples.microprofile.graphql.model.Cat;
import com.blazebit.persistence.examples.microprofile.graphql.view.CatCreateView;
import com.blazebit.persistence.examples.microprofile.graphql.view.CatSimpleView;
import com.blazebit.persistence.examples.microprofile.graphql.view.CatUpdateView;
import com.blazebit.persistence.examples.microprofile.graphql.view.CatWithOwnerView;
import com.blazebit.persistence.integration.graphql.GraphQLEntityViewSupport;
import com.blazebit.persistence.integration.graphql.GraphQLRelayConnection;
import com.blazebit.persistence.integration.jaxrs.EntityViewId;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.Sorters;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.api.Context;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.6.2
 */
@GraphQLApi
@Path("cats")
public class CatResource {

    @Inject
    EntityManager em;
    @Inject
    EntityViewManager evm;
    @Inject
    CriteriaBuilderFactory cbf;
    @Inject
    Context context;
    @Inject
    GraphQLEntityViewSupport graphQLEntityViewSupport;

    @Transactional
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public CatSimpleView updateCat(@EntityViewId("id") CatUpdateView catUpdateView) {
        evm.save(em, catUpdateView);
        return evm.find(em, CatSimpleView.class, catUpdateView.getId());
    }

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    public Response addCat(CatUpdateView view) {
        evm.save(em, view);
        return Response.created(URI.create("/cats/" + view.getId())).build();
    }

    @GET
    @Query("cats")
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    public List<CatSimpleView> getCats() {
        CriteriaBuilder<Cat> cb = cbf.create(em, Cat.class);
        return evm.applySetting(graphQLEntityViewSupport.<CatSimpleView>createSetting(context.unwrap(DataFetchingEnvironment.class)), cb).getResultList();
    }

    @Query
    public CatWithOwnerView catById(@Name("id") Long id) {
        return evm.find(em, graphQLEntityViewSupport.createSetting(context.unwrap(DataFetchingEnvironment.class)), id);
    }

    @Query
    public GraphQLRelayConnection<CatWithOwnerView> findAll(
            @Name("first") Integer first,
            @Name("last") Integer last,
            @Name("offset") Integer offset,
            @Name("before") String before,
            @Name("after") String after) {
        CriteriaBuilder<Cat> cb = cbf.create(em, Cat.class);
        EntityViewSetting<CatWithOwnerView, PaginatedCriteriaBuilder<CatWithOwnerView>> setting = graphQLEntityViewSupport.createPaginatedSetting(
                context.unwrap(DataFetchingEnvironment.class)
        );
        setting.addAttributeSorter("id", Sorters.ascending());
        if (setting.getMaxResults() == 0) {
            return new GraphQLRelayConnection<>();
        }
        return new GraphQLRelayConnection<>(evm.applySetting(setting, cb).getResultList());
    }

    @Mutation
    @Transactional
    public Long createCat(@Source(name = "cat") CatCreateView cat) {
        evm.save(em, cat);
        return cat.getId();
    }

    @DELETE
    @Transactional
    public Response clearCats() {
        cbf.delete(em, Cat.class).executeUpdate();
        return Response.ok().build();
    }
}
