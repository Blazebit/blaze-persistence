/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
