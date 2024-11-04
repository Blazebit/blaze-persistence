/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.spqr.view;

import com.blazebit.persistence.examples.spring.data.spqr.model.Child;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewInheritance;
import com.blazebit.persistence.view.IdMapping;
import io.leangen.graphql.annotations.types.GraphQLUnion;

/**
 * @author Christian Beikov
 * @since 1.6.4
 */
@GraphQLUnion(name = "Child", possibleTypeAutoDiscovery = true)
@EntityView(Child.class)
@EntityViewInheritance
public interface ChildView {

    @IdMapping
    Long getId();

    String getName();
}
