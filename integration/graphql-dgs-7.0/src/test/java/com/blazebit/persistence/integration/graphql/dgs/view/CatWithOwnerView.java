/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.graphql.dgs.view;

import com.blazebit.persistence.integration.graphql.dgs.model.Cat;
import com.blazebit.persistence.integration.graphql.GraphQLName;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.MappingSingular;

import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.6.2
 */
@EntityView(Cat.class)
public interface CatWithOwnerView extends CatSimpleView {

    PersonSimpleView getOwner();

    @MappingSingular
    List<String> getNicknames();

    @GraphQLName("theData")
    default String abc() {
        return "def";
    }
    default String getSampleData() {
        return "abc";
    }
}
