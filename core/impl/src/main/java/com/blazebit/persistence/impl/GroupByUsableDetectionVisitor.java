/*
 * Copyright 2014 - 2016 Blazebit.
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

import com.blazebit.persistence.impl.expression.AbortableVisitorAdapter;
import com.blazebit.persistence.impl.expression.AggregateExpression;
import com.blazebit.persistence.impl.expression.FunctionExpression;
import com.blazebit.persistence.impl.expression.SubqueryExpression;

/**
 * Returns false if expression is usable in groupBy, true otherwise
 */
class GroupByUsableDetectionVisitor extends AbortableVisitorAdapter {
    
    private final boolean treatSizeAsAggreagte;
    
    public GroupByUsableDetectionVisitor(boolean treatSizeAsAggreagte) {
        this.treatSizeAsAggreagte = treatSizeAsAggreagte;
    }

    @Override
    public Boolean visit(FunctionExpression expression) {
        if (expression instanceof AggregateExpression || (treatSizeAsAggreagte && com.blazebit.persistence.impl.util.ExpressionUtils.isSizeFunction(expression))) {
            return true;
        }
        return super.visit(expression);
    }

    @Override
    public Boolean visit(SubqueryExpression expression) {
        return true;
    }
    
}