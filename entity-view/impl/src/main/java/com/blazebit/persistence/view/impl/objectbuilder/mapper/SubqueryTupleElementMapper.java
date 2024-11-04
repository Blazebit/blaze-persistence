/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.objectbuilder.mapper;

/**
 * Just a marker interface for element mappers that use subqueries
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface SubqueryTupleElementMapper extends TupleElementMapper {

    public String getViewPath();

    public String getEmbeddingViewPath();

    public String getSubqueryAlias();

    public String getSubqueryExpression();
}
