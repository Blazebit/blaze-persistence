/*
 * Copyright 2014 Blazebit.
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

import com.blazebit.persistence.impl.expression.CompositeExpression;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.FooExpression;
import com.blazebit.persistence.impl.expression.OuterExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import java.util.ArrayList;

/**
 * This Transformer runs through the expressions of the query
 * For each OUTER(pp) expression it performs an implicitJoin for the join manager
 * of the surrounding query and repalces the OUTER(pp) expression with the base node alias '.' the field.
 * 
 * We need a join manager hierarchy to do this.
 * We have decided to limit the outer statement to the join manager of the directly surrounding query so that the
 * user can specify the absolute path in a normalized form.
 *
 * @author Moritz Becker
 * @since 1.0
 */
public class OuterFunctionTransformer implements ExpressionTransformer {
    private final JoinManager joinManager;

    public OuterFunctionTransformer(JoinManager joinManager) {
        this.joinManager = joinManager;
    }
    
    @Override
    public Expression transform(Expression original) {
        return transform(original, false);
    }

    @Override
    public Expression transform(Expression original, boolean selectClause) {
        if(original instanceof CompositeExpression){
            CompositeExpression compExpr = (CompositeExpression) original;
            CompositeExpression transformed = new CompositeExpression(new ArrayList<Expression>());
            for(Expression e : compExpr.getExpressions()){
                transformed.getExpressions().add(transform(e, selectClause));
            }
            return transformed;
        }
        
        if(!(original instanceof OuterExpression)){
            return original;
        }
        PathExpression path = ((OuterExpression) original).getPath();
        
        if(joinManager.getParent() != null){
            joinManager.getParent().implicitJoin(path, true, selectClause, true);
        }
        
        return original;
    }
    
}
