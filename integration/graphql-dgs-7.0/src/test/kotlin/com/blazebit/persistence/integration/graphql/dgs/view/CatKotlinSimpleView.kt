/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.integration.graphql.dgs.view

import com.blazebit.persistence.view.EntityView
import com.blazebit.persistence.view.IdMapping

/**
 * @author Christian Beikov
 * @since 1.6.15
 */
@EntityView(com.blazebit.persistence.integration.graphql.dgs.model.Cat::class)
interface CatKotlinSimpleView {
    @get:IdMapping
    val id: Long?

    val name: String
}