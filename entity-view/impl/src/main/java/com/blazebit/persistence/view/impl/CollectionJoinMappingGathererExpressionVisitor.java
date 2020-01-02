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

package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.expression.ArrayExpression;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.FunctionExpression;
import com.blazebit.persistence.parser.expression.PathElementExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.PropertyExpression;
import com.blazebit.persistence.parser.expression.VisitorAdapter;
import com.blazebit.persistence.parser.predicate.IsEmptyPredicate;
import com.blazebit.persistence.parser.predicate.MemberOfPredicate;
import com.blazebit.persistence.parser.util.ExpressionUtils;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class CollectionJoinMappingGathererExpressionVisitor extends VisitorAdapter {

    private final EntityMetamodel metamodel;
    private final ManagedType<?> managedType;
    private final List<String> paths;

    public CollectionJoinMappingGathererExpressionVisitor(ManagedType<?> managedType, EntityMetamodel metamodel) {
        this.metamodel = metamodel;
        this.managedType = managedType;
        this.paths = new ArrayList<>();
    }
    
    public List<String> getPaths() {
        return paths;
    }

    @Override
    public void visit(PropertyExpression expression) {
        throw new UnsupportedOperationException("This method should never be called!");
    }

    @Override
    public void visit(PathExpression expression) {
        List<PathElementExpression> expressions = expression.getExpressions();
        int size = expressions.size();
        StringBuilder sb = new StringBuilder(size * 10);
        ManagedType<?> t = managedType;
        Attribute<?, ?> jpaAttribute = null;
        
        for (int i = 0; i < size; i++) {
            String baseName;
            Expression e = expressions.get(i);
            if (e instanceof ArrayExpression) {
                ArrayExpression arrayExpression = (ArrayExpression) e;
                arrayExpression.getIndex().accept(this);
                continue;
            } else {
                baseName = e.toString();
            }
            
            if (i != 0) {
                sb.append('.');
            }
            
            sb.append(e.toString());

            try {
                jpaAttribute = t.getAttribute(baseName);
            } catch (IllegalArgumentException ex) {
                // Ignore non existing attributes
                jpaAttribute = null;
            }
            
            // NOTE: Attribute could be null because this model might contain errors
            if (jpaAttribute != null) {
                t = metamodel.getManagedType(JpaMetamodelUtils.resolveFieldClass(t.getJavaType(), jpaAttribute));
                if (jpaAttribute instanceof PluralAttribute<?, ?, ?>) {
                    paths.add(sb.toString());
                }
            }
        }

        if (jpaAttribute instanceof PluralAttribute<?, ?, ?>) {
            paths.add(sb.toString());
        }
    }

    @Override
    public void visit(IsEmptyPredicate predicate) {
    }

    @Override
    public void visit(MemberOfPredicate predicate) {
    }

    @Override
    public void visit(FunctionExpression expression) {
        if (!ExpressionUtils.isSizeFunction(expression)) {
            super.visit(expression);
        }
    }
}
