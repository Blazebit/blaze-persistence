/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.graphql.dgs.view

import com.blazebit.persistence.view.EntityView
import com.blazebit.persistence.view.UpdatableEntityView

/**
 * @author Christian Beikov
 * @since 1.6.15
 */
@UpdatableEntityView
@EntityView(com.blazebit.persistence.integration.graphql.dgs.model.Cat::class)
interface CatKotlinUpdateView : CatKotlinSimpleView {
    fun setName(name: String?)

    var age: Int?
}