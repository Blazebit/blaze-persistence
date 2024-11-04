/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.spring.data.spqr.view;

import com.blazebit.persistence.examples.spring.data.spqr.model.Boy;
import com.blazebit.persistence.view.EntityView;
import io.leangen.graphql.annotations.types.GraphQLType;

/**
 * @author Christian Beikov
 * @since 1.6.4
 */
@GraphQLType(name = "Boy")
@EntityView(Boy.class)
public interface BoyView extends ChildView {
}
