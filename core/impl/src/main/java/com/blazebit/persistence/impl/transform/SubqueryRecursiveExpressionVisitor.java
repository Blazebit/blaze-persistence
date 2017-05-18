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

package com.blazebit.persistence.impl.transform;

import com.blazebit.persistence.impl.AbstractCommonQueryBuilder;
import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.parser.expression.SubqueryExpression;
import com.blazebit.persistence.parser.expression.VisitorAdapter;
import com.blazebit.persistence.parser.expression.modifier.ExpressionModifier;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class SubqueryRecursiveExpressionVisitor extends VisitorAdapter implements ExpressionModifierVisitor<ExpressionModifier> {

    @Override
    public void visit(ExpressionModifier expressionModifier, ClauseType clauseType) {
        expressionModifier.get().accept(this);
    }

    @Override
    public void visit(SubqueryExpression expression) {
        // TODO: this is ugly
        ((AbstractCommonQueryBuilder<?, ?, ?, ?, ?>) expression.getSubquery()).applyExpressionTransformersAndBuildGroupByClauses(false);
    }

}
