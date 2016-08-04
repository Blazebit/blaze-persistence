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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.Basic;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import com.blazebit.annotation.AnnotationUtils;
import com.blazebit.persistence.impl.expression.*;
import com.blazebit.persistence.impl.predicate.*;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class ExpressionUtils {

    private final static Logger LOG = Logger.getLogger(ExpressionUtils.class.getName());

    public static boolean isUnique(Metamodel metamodel, Expression expr) {
        if (expr instanceof CompositeExpression) {
            return isUnique(metamodel, (CompositeExpression) expr);
        } else if (expr instanceof FunctionExpression) {
            return isUnique(metamodel, (FunctionExpression) expr);
        } else if (expr instanceof PathExpression) {
            return isUnique(metamodel, (PathExpression) expr);
        } else if (expr instanceof SubqueryExpression) {
            return isUnique(metamodel, (SubqueryExpression) expr);
        } else if (expr instanceof ParameterExpression) {
            return false;
        } else if (expr instanceof GeneralCaseExpression) {
            return isUnique(metamodel, (GeneralCaseExpression) expr);
        } else if (expr instanceof FooExpression) {
            // TODO: Not actually sure how we could do that better
            return false;
        } else if (expr instanceof LiteralExpression) {
            return false;
        } else if (expr instanceof NumericLiteral) {
            return false;
        } else if (expr instanceof BooleanLiteral) {
            return false;
        } else if (expr instanceof StringLiteral) {
            return false;
        } else if (expr instanceof ArithmeticFactor) {
            return isUnique(metamodel, ((ArithmeticFactor) expr).getExpression());
        } else if (expr instanceof ArithmeticExpression) {
            return false;
        } if (expr instanceof NullExpression) {
            // The actual semantics of NULL are, that NULL != NULL
            return true;
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
        List<Expression> expressions = ((SubqueryInternalBuilder<?>) expr.getSubquery()).getSelectExpressions();

        if (expressions.size() != 1) {
            throw new IllegalArgumentException("Can't perform nullability analysis on a subquery with more than one result column!");
        }

        return isUnique(metamodel, expressions.get(0));
    }

    private static boolean isUnique(Metamodel metamodel, GeneralCaseExpression expr) {
        if (!isUnique(metamodel, expr.getDefaultExpr())) {
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

    private static boolean isUnique(Metamodel metamodel, PathExpression expr) {
        JoinNode baseNode = ((JoinNode) expr.getBaseNode());
        ManagedType<?> t;
        Attribute<?, ?> attr;

        if (expr.getField() != null) {
            t = metamodel.managedType(baseNode.getPropertyClass());
            String attributeName = expr.getField();
            if (attributeName.indexOf('.') < 0) {
                attr = t.getAttribute(expr.getField());
            } else {
		        String[] attributeParts = attributeName.split("\\.");
		        attr = getPolymorphicSimpleAttribute(metamodel, t, attributeParts[0]);

		        for (int i = 1; i < attributeParts.length; i++) {
		            t = metamodel.managedType(JpaUtils.resolveFieldClass(t.getJavaType(), attr));
		            attr = getPolymorphicAttribute(metamodel, t, attributeParts[i]);
		        }
            }

            if (!isUnique(attr)) {
                return false;
            }
        }

        while (baseNode.getParent() != null) {
            attr = baseNode.getParentTreeNode().getAttribute();
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

    private static Attribute<?, ?> getPolymorphicSimpleAttribute(Metamodel metamodel, ManagedType<?> type, String attributeName) {
        Set<Attribute<?, ?>> resolvedAttributes = JpaUtils.getAttributesPolymorphic(metamodel, type, attributeName);
        Iterator<Attribute<?, ?>> iter = resolvedAttributes.iterator();

        if (resolvedAttributes.size() > 1) {
            // If there is more than one resolved attribute we can still save the user some trouble
            Attribute<?, ?> simpleAttribute = null;
            Set<Attribute<?, ?>> amiguousAttributes = new HashSet<Attribute<?, ?>>();

            for (Attribute<?, ?> attr : resolvedAttributes) {
                if (JpaUtils.isJoinable(attr)) {
                    amiguousAttributes.add(attr);
                } else {
                    simpleAttribute = attr;
                }
            }

            if (simpleAttribute == null) {
                return null;
            } else {
                for (Attribute<?, ?> a : amiguousAttributes) {
                    LOG.warning("The attribute [" + attributeName + "] of the class [" + a.getDeclaringType().getJavaType().getName()
                        + "] is ambiguous for polymorphic implicit joining on the type [" + type.getJavaType().getName() + "]");
                }

                return simpleAttribute;
            }
        } else if (iter.hasNext()) {
            return iter.next();
        } else {
            return null;
        }
    }

    private static Attribute<?, ?> getPolymorphicAttribute(Metamodel metamodel, ManagedType<?> type, String attributeName) {
        Set<Attribute<?, ?>> resolvedAttributes = JpaUtils.getAttributesPolymorphic(metamodel, type, attributeName);
        Iterator<Attribute<?, ?>> iter = resolvedAttributes.iterator();

        if (resolvedAttributes.size() > 1) {
            // If there is more than one resolved attribute we can still save the user some trouble
            Attribute<?, ?> joinableAttribute = null;
            Attribute<?, ?> attr = null;

            // Multiple non-joinable attributes would be fine since we only care for OUR join manager here
            // Multiple joinable attributes are only fine if they all have the same type
            while (iter.hasNext()) {
                attr = iter.next();
                if (JpaUtils.isJoinable(attr)) {
                    if (joinableAttribute != null && !joinableAttribute.getJavaType().equals(attr.getJavaType())) {
                        throw new IllegalArgumentException("Multiple joinable attributes with the name [" + attributeName
                            + "] but different java types in the types [" + joinableAttribute.getDeclaringType().getJavaType().getName()
                            + "] and [" + attr.getDeclaringType().getJavaType().getName() + "] found!");
                    } else {
                        joinableAttribute = attr;
                    }
                }
            }

            // We return the joinable attribute because OUR join manager needs it's type for further joining
            if (joinableAttribute != null) {
                return joinableAttribute;
            }

            return attr;
        } else if (iter.hasNext()) {
            return iter.next();
        } else {
            return null;
        }
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

    public static boolean isNullable(Metamodel metamodel, Expression expr) {
        if (expr instanceof CompositeExpression) {
            return isNullable(metamodel, (CompositeExpression) expr);
        } else if (expr instanceof FunctionExpression) {
            return isNullable(metamodel, (FunctionExpression) expr);
        } else if (expr instanceof PathExpression) {
            return isNullable(metamodel, (PathExpression) expr);
        } else if (expr instanceof SubqueryExpression) {
            return isNullable(metamodel, (SubqueryExpression) expr);
        } else if (expr instanceof ParameterExpression) {
            return true;
        } else if (expr instanceof GeneralCaseExpression) {
            return isNullable(metamodel, (GeneralCaseExpression) expr);
        } else if (expr instanceof FooExpression) {
            return false;
        } else if (expr instanceof LiteralExpression) {
            return false;
        } else if (expr instanceof NullExpression) {
            return true;
        } else if (expr instanceof NumericLiteral) {
            return false;
        } else if (expr instanceof BooleanLiteral) {
            return false;
        } else if (expr instanceof StringLiteral) {
            return false;
        } else if (expr instanceof ArithmeticFactor) {
            return isNullable(metamodel, ((ArithmeticFactor) expr).getExpression());
        } else if (expr instanceof ArithmeticExpression) {
            return isNullable(metamodel, (ArithmeticExpression) expr);
        } else {
            throw new IllegalArgumentException("The expression of type '" + expr.getClass().getName() + "' can not be analyzed for nullability!");
        }
    }

    private static boolean isNullable(Metamodel metamodel, ArithmeticExpression arithmeticExpression) {
        return isNullable(metamodel, arithmeticExpression.getLeft()) || isNullable(metamodel, arithmeticExpression.getRight());
    }

    private static boolean isNullable(Metamodel metamodel, CompositeExpression expr) {
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

    private static boolean isNullable(Metamodel metamodel, GeneralCaseExpression expr) {
        if (isNullable(metamodel, expr.getDefaultExpr())) {
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

    private static boolean isNullable(Metamodel metamodel, FunctionExpression expr) {
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

    private static boolean isNullable(Metamodel metamodel, SubqueryExpression expr) {
        List<Expression> expressions = ((SubqueryInternalBuilder<?>) expr.getSubquery()).getSelectExpressions();

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
            String attributeName = expr.getField();
            if (attributeName.indexOf('.') < 0) {
                attr = t.getAttribute(expr.getField());
            } else {
		        String[] attributeParts = attributeName.split("\\.");
		        attr = getPolymorphicSimpleAttribute(metamodel, t, attributeParts[0]);

		        for (int i = 1; i < attributeParts.length; i++) {
		            t = metamodel.managedType(JpaUtils.resolveFieldClass(t.getJavaType(), attr));
		            attr = getPolymorphicAttribute(metamodel, t, attributeParts[i]);
		        }
            }

            if (isNullable(attr)) {
                return true;
            }
        }

        while (baseNode.getParent() != null) {
            attr = baseNode.getParentTreeNode().getAttribute();
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
        return e.accept(subqueryExpressionDetector);
    }

    public static boolean containsSizeExpression(Expression e) {
        return e.accept(sizeExpressionDetector);
    }

    public static Expression replaceSubexpression(Expression superExpression, String placeholder, Expression substitute) {
        if (superExpression instanceof PathExpression) {
            PathExpression expression = (PathExpression) superExpression;
            List<PathElementExpression> expressions = expression.getExpressions();
            int size = expressions.size();

            if (size == 1) {
                PathElementExpression elementExpression = expressions.get(0);
                if (!(elementExpression instanceof PropertyExpression)) {
                    throw new IllegalArgumentException("Illegal array expression in subquery alias");
                }

                if (placeholder.equals(((PropertyExpression) elementExpression).getProperty())) {
                    return substitute;
                }
            }
        }
        final AliasReplacementTransformer replacementTransformer = new AliasReplacementTransformer(substitute, placeholder);
        VisitorAdapter transformationVisitor = new VisitorAdapter() {

            @Override
            public void visit(CompositeExpression expression) {
                super.visit(expression);
                List<Expression> transformed = new ArrayList<Expression>();
                List<Expression> expressions = expression.getExpressions();
                int size = expressions.size();
                for (int i = 0; i < size; i++) {
                    transformed.add(replacementTransformer.transform(expressions.get(i), null, false));
                }
                expression.getExpressions().clear();
                expression.getExpressions().addAll(transformed);
            }

            @Override
            public void visit(FunctionExpression expression) {
                super.visit(expression);
                List<Expression> transformed = new ArrayList<Expression>();
                List<Expression> expressions = expression.getExpressions();
                int size = expressions.size();
                for (int i = 0; i < size; i++) {
                    transformed.add(replacementTransformer.transform(expressions.get(i), null, false));
                }
                expression.setExpressions(transformed);
            }

            @Override
            public void visit(ArithmeticExpression expression) {
                super.visit(expression);
                expression.setLeft(replacementTransformer.transform(expression.getLeft(), null, false));
                expression.setRight(replacementTransformer.transform(expression.getRight(), null, false));
            }

            @Override
            public void visit(ArithmeticFactor expression) {
                super.visit(expression);
                expression.setExpression(replacementTransformer.transform(expression.getExpression(), null, false));
            }

            @Override
            public void visit(IsNullPredicate predicate) {
                super.visit(predicate);
                predicate.setExpression(replacementTransformer.transform(predicate.getExpression(), null, false));
            }

            @Override
            public void visit(BetweenPredicate predicate) {
                super.visit(predicate);
                predicate.setLeft(replacementTransformer.transform(predicate.getLeft(), null, false));
                predicate.setStart(replacementTransformer.transform(predicate.getStart(), null, false));
                predicate.setEnd(replacementTransformer.transform(predicate.getEnd(), null, false));
            }

            @Override
            public void visit(LikePredicate predicate) {
                super.visit(predicate);
            }

            @Override
            public void visit(InPredicate predicate) {
                super.visit(predicate);
                predicate.setLeft(replacementTransformer.transform(predicate.getLeft(), null, false));
                predicate.setRight(replacementTransformer.transform(predicate.getRight(), null, false));
            }

            @Override
            public void visit(ExistsPredicate predicate) {
                super.visit(predicate);
                predicate.setExpression(replacementTransformer.transform(predicate.getExpression(), null, false));
            }

            @Override
            public void visit(EqPredicate predicate) {
                super.visit(predicate);
                predicate.setLeft(replacementTransformer.transform(predicate.getLeft(), null, false));
                predicate.setRight(replacementTransformer.transform(predicate.getRight(), null, false));
            }

            @Override
            public void visit(GtPredicate predicate) {
                super.visit(predicate);
                predicate.setLeft(replacementTransformer.transform(predicate.getLeft(), null, false));
                predicate.setRight(replacementTransformer.transform(predicate.getRight(), null, false));
            }

            @Override
            public void visit(GePredicate predicate) {
                super.visit(predicate);
                predicate.setLeft(replacementTransformer.transform(predicate.getLeft(), null, false));
                predicate.setRight(replacementTransformer.transform(predicate.getRight(), null, false));
            }

            @Override
            public void visit(LtPredicate predicate) {
                super.visit(predicate);
                predicate.setLeft(replacementTransformer.transform(predicate.getLeft(), null, false));
                predicate.setRight(replacementTransformer.transform(predicate.getRight(), null, false));
            }

            @Override
            public void visit(LePredicate predicate) {
                super.visit(predicate);
                predicate.setLeft(replacementTransformer.transform(predicate.getLeft(), null, false));
                predicate.setRight(replacementTransformer.transform(predicate.getRight(), null, false));
            }

        };

        superExpression.accept(transformationVisitor);
        return superExpression;
    }

    public static boolean isSizeFunction(Expression expression) {
        if (expression instanceof FunctionExpression) {
            return isSizeFunction((FunctionExpression) expression);
        }
        return false;
    }

    public static boolean isSizeFunction(FunctionExpression expression) {
        return "SIZE".equalsIgnoreCase(expression.getFunctionName());
    }

    public static boolean isOuterFunction(FunctionExpression e) {
        return "OUTER".equalsIgnoreCase(e.getFunctionName());
    }

    private static final AbortableVisitorAdapter subqueryExpressionDetector = new AbortableVisitorAdapter() {

        @Override
        public Boolean visit(SubqueryExpression expression) {
            return true;
        }
    };

    private static final AbortableVisitorAdapter sizeExpressionDetector = new AbortableVisitorAdapter() {

        @Override
        public Boolean visit(FunctionExpression expression) {
            return ExpressionUtils.isSizeFunction(expression);
        }
    };
}
