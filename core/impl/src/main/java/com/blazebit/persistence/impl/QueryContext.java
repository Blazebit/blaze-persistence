/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class QueryContext {

    private final AbstractCommonQueryBuilder<?, ?, ?, ?, ?> parent;
    private final ClauseType parentClause;

    public QueryContext(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> parent, ClauseType parentClause) {
        this.parent = parent;
        this.parentClause = parentClause;
    }

    public AbstractCommonQueryBuilder<?, ?, ?, ?, ?> getParent() {
        return parent;
    }

    public ClauseType getParentClause() {
        return parentClause;
    }
}
