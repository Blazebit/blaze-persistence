/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.mapper;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public interface AliasedTupleElementMapper extends TupleElementMapper {

    public String getAlias();
}
