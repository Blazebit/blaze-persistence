/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.spqr.view;

import com.blazebit.persistence.examples.spring.data.spqr.model.Cat;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.MappingSingular;
import io.leangen.graphql.annotations.GraphQLQuery;

import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.6.4
 */
@EntityView(Cat.class)
public interface CatWithOwnerView extends CatSimpleView {

    PersonSimpleView getOwner();

    @MappingSingular
    List<String> getNicknames();

    @GraphQLQuery(name = "theData")
    default String abc() {
        return "def";
    }
    default String getSampleData() {
        return "abc";
    }

}
