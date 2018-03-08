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

import com.blazebit.persistence.impl.ClauseType;
import com.blazebit.persistence.parser.expression.modifier.ExpressionModifier;


/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.2.0
 */
public class SizeExpressionTransformer implements ExpressionModifierVisitor<ExpressionModifier> {

    private final SizeTransformationVisitor sizeTransformationVisitor;

    public SizeExpressionTransformer(SizeTransformationVisitor sizeTransformationVisitor) {
        this.sizeTransformationVisitor = sizeTransformationVisitor;
    }

    @Override
    public void visit(ExpressionModifier expressionModifier, ClauseType clauseType) {
        sizeTransformationVisitor.setClause(clauseType);
        sizeTransformationVisitor.setOrderBySelectClause(false);
        sizeTransformationVisitor.visit(expressionModifier);
    }

}