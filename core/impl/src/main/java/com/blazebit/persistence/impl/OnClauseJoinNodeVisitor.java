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
public class OnClauseJoinNodeVisitor implements JoinNodeVisitor {

    private final Expression.Visitor visitor;

    public OnClauseJoinNodeVisitor(Expression.Visitor visitor) {
        this.visitor = visitor;
    }

    @Override
    public void visit(JoinNode node) {
        if (node.getOnPredicate() != null) {
            node.getOnPredicate().accept(visitor);
        }
    }

}
