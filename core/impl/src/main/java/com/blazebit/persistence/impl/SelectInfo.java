/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.parser.expression.Expression;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class SelectInfo extends NodeInfo implements AliasInfo {

    final String alias;
    private final AliasManager aliasOwner;

    public SelectInfo(Expression expression) {
        super(expression);
        this.alias = null;
        this.aliasOwner = null;
    }

    public SelectInfo(Expression expression, String alias, AliasManager aliasOwner) {
        super(expression);
        this.alias = alias;
        this.aliasOwner = aliasOwner;
    }

    @Override
    public SelectInfo clone() {
        return new SelectInfo(getExpression(), alias, aliasOwner);
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public AliasManager getAliasOwner() {
        return aliasOwner;
    }

    public void accept(SelectInfoVisitor visitor) {
        visitor.visit(this);
    }

}
