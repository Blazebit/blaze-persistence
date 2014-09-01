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
import com.blazebit.persistence.impl.expression.FunctionExpression;
import com.blazebit.persistence.impl.expression.OuterExpression;
import com.blazebit.persistence.impl.expression.ParameterExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import com.blazebit.persistence.impl.predicate.VisitorAdapter;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class ExpressionUtils {

    public static boolean isUnique(Metamodel metamodel, Expression expr) {
        if (expr instanceof CompositeExpression) {
            return isUnique(metamodel, (CompositeExpression) expr);
        } else if (expr instanceof FunctionExpression) {
            return isUnique(metamodel, (FunctionExpression) expr);
        } else if (expr instanceof PathExpression) {
            return isUnique(metamodel, (PathExpression) expr);
        } else if (expr instanceof SubqueryExpression) {
            return isUnique(metamodel, (SubqueryExpression) expr);
        } else if (expr instanceof OuterExpression) {
            return isUnique(metamodel, ((OuterExpression) expr).getPath());
        } else if (expr instanceof ParameterExpression) {
            return false;
        } else if (expr instanceof FooExpression) {
            // TODO: Not actually sure how we could do that better
            return false;
        } else {
            throw new IllegalArgumentException("The expression of type '" + expr.getClass().getName() + "' can not be analyzed for uniqueness!");
        }
    }
    
    private static boolean isUnique(Metamodel metamodel, CompositeExpression expr) {
        if (expr.getExpressions().size() > 1) {
            // Maybe the analysis can be done but we actually don't need so accurate results right now
            return false;
        }
        
        return isUnique(metamodel, expr.getExpressions().get(0));
    }
    
    private static boolean isUnique(Metamodel metamodel, FunctionExpression expr) {
        // The existing JPA functions don't return unique results regardless of their arguments
        return false;
    }
    
    private static boolean isUnique(Metamodel metamodel, SubqueryExpression expr) {
        List<Expression> expressions = expr.getSubquery().getSelectExpressions();
        
        if (expressions.size() != 1) {
            throw new IllegalArgumentException("Can't perform nullability analysis on a subquery with more than one result column!");
        }
        
        return isUnique(metamodel, expressions.get(0));
    }
    
    private static boolean isUnique(Metamodel metamodel, PathExpression expr) {
        JoinNode baseNode = ((JoinNode) expr.getBaseNode());
        ManagedType<?> t;
        Attribute<?, ?> attr;
        
        if (expr.getField() != null) {
            t = metamodel.managedType(baseNode.getPropertyClass());
            attr = t.getAttribute(expr.getField());
            if (!isUnique(attr)) {
                return false;
            }
        }
        
        while (baseNode.getParent() != null) {
            t = metamodel.managedType(baseNode.getParent().getPropertyClass());
            attr = t.getAttribute(baseNode.getParentTreeNode().getRelationName());
            if (!isUnique(attr)) {
                return false;
            }
            baseNode = baseNode.getParent();
        }
        
        return true;
    }
    
    private static boolean isUnique(Attribute<?, ?> attr) {
        if (attr.isCollection()) {
            return false;
        }
        
        // Right now we only support ids, but we actually should check for unique constraints
        return ((SingularAttribute<?, ?>) attr).isId();
    }
    
    public static boolean isNullable(Metamodel metamodel, Expression expr) {
        if (expr instanceof CompositeExpression) {
            return isNullable(metamodel, (CompositeExpression) expr);
        } else if (expr instanceof FunctionExpression) {
            return isNullable(metamodel, (FunctionExpression) expr);
        } else if (expr instanceof PathExpression) {
            return isNullable(metamodel, (PathExpression) expr);
        } else if (expr instanceof SubqueryExpression) {
            return isNullable(metamodel, (SubqueryExpression) expr);
        } else if (expr instanceof OuterExpression) {
            return isNullable(metamodel, ((OuterExpression) expr).getPath());
        } else if (expr instanceof ParameterExpression) {
            return true;
        } else if (expr instanceof FooExpression) {
            return false;
        } else {
            throw new IllegalArgumentException("The expression of type '" + expr.getClass().getName() + "' can not be analyzed for nullability!");
        }
    }
    
    private static boolean isNullable(Metamodel metamodel, CompositeExpression expr) {
        boolean nullable;
        for (Expression subExpr : expr.getExpressions()) {
            nullable = isNullable(metamodel, subExpr);

            if (nullable) {
                return true;
            }
        }

        return false;
    }
    
    private static boolean isNullable(Metamodel metamodel, FunctionExpression expr) {
        if ("NULLIF".equals(expr.getFunctionName())) {
            return true;
        } else if ("COALESCE".equals(expr.getFunctionName())) {
            boolean nullable;
            for (Expression subExpr : expr.getExpressions()) {
                nullable = isNullable(metamodel, subExpr);

                if (!nullable) {
                    return false;
                }
            }

            return true;
        } else {
            boolean nullable;
            for (Expression subExpr : expr.getExpressions()) {
                nullable = isNullable(metamodel, subExpr);

                if (nullable) {
                    return true;
                }
            }

            return false;
        }
    }
    
    private static boolean isNullable(Metamodel metamodel, SubqueryExpression expr) {
        List<Expression> expressions = expr.getSubquery().getSelectExpressions();
        
        if (expressions.size() != 1) {
            throw new IllegalArgumentException("Can't perform nullability analysis on a subquery with more than one result column!");
        }
        
        return isNullable(metamodel, expressions.get(0));
    }
    
    private static boolean isNullable(Metamodel metamodel, PathExpression expr) {
        JoinNode baseNode = ((JoinNode) expr.getBaseNode());
        ManagedType<?> t;
        Attribute<?, ?> attr;
        
        if (expr.getField() != null) {
            t = metamodel.managedType(baseNode.getPropertyClass());
            attr = t.getAttribute(expr.getField());
            if (isNullable(attr)) {
                return true;
            }
        }
        
        while (baseNode.getParent() != null) {
            t = metamodel.managedType(baseNode.getParent().getPropertyClass());
            attr = t.getAttribute(baseNode.getParentTreeNode().getRelationName());
            if (isNullable(attr)) {
                return true;
            }
            baseNode = baseNode.getParent();
        }
        
        return false;
    }
    
    private static boolean isNullable(Attribute<?, ?> attr) {
        if (attr.isCollection()) {
            return true;
        }
        
        return ((SingularAttribute<?, ?>) attr).isOptional();
    }

    public static boolean containsSubqueryExpression(Expression e) {
        SubqueryExpressionDetector detector = new SubqueryExpressionDetector();
        e.accept(detector);
        return detector.hasSubquery;
    }

    public static void replaceSubexpression(Expression superExpression, String placeholder, Expression substitute) {
        final AliasReplacementTransformer replacementTransformer = new AliasReplacementTransformer(substitute, placeholder);
        VisitorAdapter transformationVisitor = new VisitorAdapter() {

            @Override
            public void visit(CompositeExpression expression) {
                List<Expression> transformed = new ArrayList<Expression>();
                for (Expression expr : expression.getExpressions()) {
                    transformed.add(replacementTransformer.transform(expr, null));
                }
                expression.getExpressions().clear();
                expression.getExpressions().addAll(transformed);
            }

        };

        superExpression.accept(transformationVisitor);
    }
    
    public static boolean isSizeExpression(Expression expression){
        if(expression instanceof CompositeExpression){
            return isSizeExpression((CompositeExpression) expression);
        }
        return false;
    }
    
    public static boolean isSizeExpression(CompositeExpression expression){
        return "SIZE(".equals(expression.getExpressions().get(0).toString());
    }

    private static class SubqueryExpressionDetector extends VisitorAdapter {

        private boolean hasSubquery = false;

        @Override
        public void visit(SubqueryExpression expression) {
            hasSubquery = true;
        }

        public boolean hasSubquery() {
            return hasSubquery;
        }
    }
}
