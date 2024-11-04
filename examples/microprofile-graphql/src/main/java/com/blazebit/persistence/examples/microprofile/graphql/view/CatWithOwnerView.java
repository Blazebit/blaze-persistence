/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.microprofile.graphql.view;

import com.blazebit.persistence.examples.microprofile.graphql.model.Cat;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.MappingSingular;
import org.eclipse.microprofile.graphql.Query;

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

    @Query("theData")
    default String abc() {
        return "def";
    }
    default String getSampleData() {
        return "abc";
    }
}
