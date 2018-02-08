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

import com.blazebit.annotation.AnnotationUtils;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.impl.expression.AbortableVisitorAdapter;
import com.blazebit.persistence.impl.expression.ArithmeticExpression;
import com.blazebit.persistence.impl.expression.ArithmeticFactor;
import com.blazebit.persistence.impl.expression.EntityLiteral;
import com.blazebit.persistence.impl.expression.EnumLiteral;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.FunctionExpression;
import com.blazebit.persistence.impl.expression.GeneralCaseExpression;
import com.blazebit.persistence.impl.expression.ListIndexExpression;
import com.blazebit.persistence.impl.expression.MapEntryExpression;
import com.blazebit.persistence.impl.expression.MapKeyExpression;
import com.blazebit.persistence.impl.expression.MapValueExpression;
import com.blazebit.persistence.impl.expression.NullExpression;
import com.blazebit.persistence.impl.expression.NumericLiteral;
import com.blazebit.persistence.impl.expression.ParameterExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.StringLiteral;
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import com.blazebit.persistence.impl.expression.TemporalLiteral;
import com.blazebit.persistence.impl.expression.WhenClauseExpression;
import com.blazebit.persistence.impl.predicate.BooleanLiteral;

import javax.persistence.Basic;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SingularAttribute;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class ExpressionUtils {

    private static final AbortableVisitorAdapter SUBQUERY_EXPRESSION_DETECTOR = new AbortableVisitorAdapter() {

        @Override
        public Boolean visit(SubqueryExpression expression) {
            return true;
        }
    };

    private static final AbortableVisitorAdapter SIZE_EXPRESSION_DETECTOR = new AbortableVisitorAdapter() {

        @Override
        public Boolean visit(FunctionExpression expression) {
            return com.blazebit.persistence.impl.util.ExpressionUtils.isSizeFunction(expression);
        }
    };

    private ExpressionUtils() {
    }

    public static boolean isUnique(EntityMetamodel metamodel, Expression expr) {
        if (expr instanceof FunctionExpression) {
            return isUnique(metamodel, (FunctionExpression) expr);
        } else if (expr instanceof PathExpression) {
            return isUnique(metamodel, (PathExpression) expr);
        } else if (expr instanceof SubqueryExpression) {
            // Never assume uniqueness propagates
            return false;
        } else if (expr instanceof ParameterExpression) {
            return false;
        } else if (expr instanceof GeneralCaseExpression) {
            return isUnique(metamodel, (GeneralCaseExpression) expr);
        } else if (expr instanceof ListIndexExpression) {
            return false;
        } else if (expr instanceof MapKeyExpression) {
            return false;
        } else if (expr instanceof MapEntryExpression) {
            return false;
        } else if (expr instanceof MapValueExpression) {
            return false;
        } else if (expr instanceof EntityLiteral) {
            return false;
        } else if (expr instanceof EnumLiteral) {
            return false;
        } else if (expr instanceof NumericLiteral) {
            return false;
        } else if (expr instanceof BooleanLiteral) {
            return false;
        } else if (expr instanceof StringLiteral) {
            return false;
        } else if (expr instanceof TemporalLiteral) {
            return false;
        } else if (expr instanceof ArithmeticFactor) {
            return isUnique(metamodel, ((ArithmeticFactor) expr).getExpression());
        } else if (expr instanceof ArithmeticExpression) {
            return false;
        } else if (expr instanceof NullExpression) {
            // The actual semantics of NULL are, that NULL != NULL
            return true;
        } else {
            throw new IllegalArgumentException("The expression of type '" + expr.getClass().getName() + "' can not be analyzed for uniqueness!");
        }
    }

    private static boolean isUnique(EntityMetamodel metamodel, FunctionExpression expr) {
        // The existing JPA functions don't return unique results regardless of their arguments
        return false;
    }

    private static boolean isUnique(EntityMetamodel metamodel, GeneralCaseExpression expr) {
        if (expr.getDefaultExpr() != null && !isUnique(metamodel, expr.getDefaultExpr())) {
            return false;
        }

        List<WhenClauseExpression> expressions = expr.getWhenClauses();
        int size = expressions.size();
        for (int i = 0; i < size; i++) {
            if (!isUnique(metamodel, expressions.get(i).getResult())) {
                return false;
            }
        }

        return true;
    }

    private static boolean isUnique(EntityMetamodel metamodel, PathExpression expr) {
        JoinNode baseNode = ((JoinNode) expr.getBaseNode());
        Attribute<?, ?> attr;

        if (expr.getField() != null) {
            attr = JpaUtils.getAttributeForJoining(metamodel, expr).getAttribute();

            if (!isUnique(attr)) {
                return false;
            }
        }

        while (baseNode.getParent() != null) {
            if (baseNode.getParentTreeNode() == null) {
                // Don't assume uniqueness when encountering a cross or entity join
                return false;
            } else {
                attr = baseNode.getParentTreeNode().getAttribute();
                if (!isUnique(attr)) {
                    return false;
                }
                baseNode = baseNode.getParent();
            }
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

    /**
     * 
     * @param stringLiteral A possibly quoted string literal
     * @return The stringLiteral without quotes
     */
    public static String unwrapStringLiteral(String stringLiteral) {
        if (stringLiteral.length() >= 2 && stringLiteral.startsWith("'") && stringLiteral.endsWith("'")) {
            return stringLiteral.substring(1, stringLiteral.length() - 1);
        } else {
            return stringLiteral;
        }
    }

    public static boolean isFunctionFunctionExpression(FunctionExpression func) {
        return "FUNCTION".equalsIgnoreCase(func.getFunctionName());
    }

    public static boolean isNullable(EntityMetamodel metamodel, Expression expr) {
        if (expr instanceof FunctionExpression) {
            return isNullable(metamodel, (FunctionExpression) expr);
        } else if (expr instanceof PathExpression) {
            return isNullable(metamodel, (PathExpression) expr);
        } else if (expr instanceof SubqueryExpression) {
            // Subqueries are always nullable
            return true;
        } else if (expr instanceof ParameterExpression) {
            return true;
        } else if (expr instanceof GeneralCaseExpression) {
            return isNullable(metamodel, (GeneralCaseExpression) expr);
        } else if (expr instanceof ListIndexExpression) {
            return false;
        } else if (expr instanceof MapKeyExpression) {
            return false;
        } else if (expr instanceof MapEntryExpression) {
            return false;
        } else if (expr instanceof MapValueExpression) {
            return false;
        } else if (expr instanceof EntityLiteral) {
            return false;
        } else if (expr instanceof EnumLiteral) {
            return false;
        } else if (expr instanceof NullExpression) {
            return true;
        } else if (expr instanceof NumericLiteral) {
            return false;
        } else if (expr instanceof BooleanLiteral) {
            return false;
        } else if (expr instanceof StringLiteral) {
            return false;
        } else if (expr instanceof TemporalLiteral) {
            return false;
        } else if (expr instanceof ArithmeticFactor) {
            return isNullable(metamodel, ((ArithmeticFactor) expr).getExpression());
        } else if (expr instanceof ArithmeticExpression) {
            return isNullable(metamodel, (ArithmeticExpression) expr);
        } else {
            throw new IllegalArgumentException("The expression of type '" + expr.getClass().getName() + "' can not be analyzed for nullability!");
        }
    }

    private static boolean isNullable(EntityMetamodel metamodel, ArithmeticExpression arithmeticExpression) {
        return isNullable(metamodel, arithmeticExpression.getLeft()) || isNullable(metamodel, arithmeticExpression.getRight());
    }

    private static boolean isNullable(EntityMetamodel metamodel, GeneralCaseExpression expr) {
        if (expr.getDefaultExpr() != null && isNullable(metamodel, expr.getDefaultExpr())) {
            return true;
        }

        List<WhenClauseExpression> expressions = expr.getWhenClauses();
        int size = expressions.size();
        for (int i = 0; i < size; i++) {
            if (isNullable(metamodel, expressions.get(i).getResult())) {
                return true;
            }
        }

        return false;
    }

    private static boolean isNullable(EntityMetamodel metamodel, FunctionExpression expr) {
        if ("NULLIF".equalsIgnoreCase(expr.getFunctionName())) {
            return true;
        } else if ("COALESCE".equalsIgnoreCase(expr.getFunctionName())) {
            boolean nullable;
            List<Expression> expressions = expr.getExpressions();
            int size = expressions.size();
            for (int i = 0; i < size; i++) {
                nullable = isNullable(metamodel, expressions.get(i));

                if (!nullable) {
                    return false;
                }
            }

            return true;
        } else {
            boolean nullable;
            List<Expression> expressions = expr.getExpressions();
            int size = expressions.size();
            for (int i = 0; i < size; i++) {
                nullable = isNullable(metamodel, expressions.get(i));

                if (nullable) {
                    return true;
                }
            }

            return false;
        }
    }

    private static boolean isNullable(EntityMetamodel metamodel, PathExpression expr) {
        JoinNode baseNode = ((JoinNode) expr.getBaseNode());
        Attribute<?, ?> attr;

        if (expr.getField() != null) {
            attr = JpaUtils.getAttributeForJoining(metamodel, expr).getAttribute();

            if (isNullable(attr)) {
                return true;
            }
        }

        while (baseNode.getParent() != null) {
            if (baseNode.getParentTreeNode() == null) {
                // This is a cross or entity join
                if (baseNode.getParent().getJoinType() != JoinType.LEFT) {
                    attr = JpaUtils.getAttributeForJoining(metamodel, expr).getAttribute();
                    return isNullable(attr);
                }
                // Any attribute of a left joined relation could be null
                return false;
            } else {
                attr = baseNode.getParentTreeNode().getAttribute();
                if (isNullable(attr)) {
                    return true;
                }
                baseNode = baseNode.getParent();
            }
        }

        return false;
    }

    private static boolean isNullable(Attribute<?, ?> attr) {
        if (attr.isCollection()) {
            return true;
        }

        // !((SingularAttribute<?, ?>) attr).isId() is required as a workaround for Eclipselink
        return ((SingularAttribute<?, ?>) attr).isOptional() && !((SingularAttribute<?, ?>) attr).isId();
    }

    public static FetchType getFetchType(Attribute<?, ?> attr) {
        Member m = attr.getJavaMember();
        Set<Annotation> annotations;
        if (m instanceof Method) {
            annotations = AnnotationUtils.getAllAnnotations((Method) m);
        } else if (m instanceof Field) {
            annotations = new HashSet<Annotation>();
            Collections.addAll(annotations, ((Field) m).getAnnotations());
        } else {
            throw new IllegalStateException("Attribute member [" + attr.getName() + "] is neither field nor method");
        }
        Class<? extends Annotation> annotationType;
        switch (attr.getPersistentAttributeType()) {
            case BASIC:
                annotationType = Basic.class;
                break;
            case ELEMENT_COLLECTION:
                annotationType = ElementCollection.class;
                break;
            case EMBEDDED:
                return FetchType.EAGER;
            case MANY_TO_MANY:
                annotationType = ManyToMany.class;
                break;
            case MANY_TO_ONE:
                annotationType = ManyToOne.class;
                break;
            case ONE_TO_MANY:
                annotationType = OneToMany.class;
                break;
            case ONE_TO_ONE:
                annotationType = OneToOne.class;
                break;
            default:
                return FetchType.EAGER;
        }
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().isAssignableFrom(annotationType)) {
                try {
                    return (FetchType) annotation.annotationType().getMethod("fetch").invoke(annotation);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        return FetchType.EAGER;

    }

    public static boolean isAssociation(Attribute<?, ?> attr) {
        return attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE
            || attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_ONE;
    }

    public static boolean containsSubqueryExpression(Expression e) {
        return e.accept(SUBQUERY_EXPRESSION_DETECTOR);
    }

    public static boolean containsSizeExpression(Expression e) {
        return e.accept(SIZE_EXPRESSION_DETECTOR);
    }

    public static Expression replaceSubexpression(Expression superExpression, String placeholder, Expression substitute) {
        return superExpression.accept(new AliasReplacementVisitor(substitute, placeholder));
    }
}
