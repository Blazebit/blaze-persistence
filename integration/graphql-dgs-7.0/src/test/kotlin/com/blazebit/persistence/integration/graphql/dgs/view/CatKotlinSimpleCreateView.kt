/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.graphql.dgs.view

import com.blazebit.persistence.view.CreatableEntityView
import com.blazebit.persistence.view.EntityView

/**
 * @author Christian Beikov
 * @since 1.6.15
 */
@CreatableEntityView
@EntityView(com.blazebit.persistence.integration.graphql.dgs.model.Cat::class)
interface CatKotlinSimpleCreateView : CatKotlinUpdateView {
    var owner: PersonIdView?
}