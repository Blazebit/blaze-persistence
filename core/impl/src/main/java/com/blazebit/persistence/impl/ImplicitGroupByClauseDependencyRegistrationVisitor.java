/*
 * Copyright 2014 - 2020 Blazebit.
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

import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.VisitorAdapter;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
class ImplicitGroupByClauseDependencyRegistrationVisitor extends VisitorAdapter {

    private final AliasManager aliasManager;

    public ImplicitGroupByClauseDependencyRegistrationVisitor(AliasManager aliasManager) {
        this.aliasManager = aliasManager;
    }

    @Override
    public void visit(PathExpression expr) {
        JoinNode node = (JoinNode) expr.getBaseNode();
        if (node == null) {
            // This can only be a select alias
            ((SelectInfo) aliasManager.getAliasInfo(expr.toString())).getExpression().accept(this);
        } else {
            node.updateClauseDependencies(ClauseType.GROUP_BY, null);
        }
    }

}