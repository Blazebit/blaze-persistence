/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.microprofile.graphql.resource;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
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

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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
        EntityViewSetting<CatWithOwnerView, ?> setting = graphQLEntityViewSupport.createPaginatedSetting(
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
