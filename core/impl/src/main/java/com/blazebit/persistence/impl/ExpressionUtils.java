/*
 * Copyright 2014 - 2023 Blazebit.
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
import com.blazebit.persistence.impl.function.count.AbstractCountFunction;
import com.blazebit.persistence.parser.AliasReplacementVisitor;
import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.expression.AbortableVisitorAdapter;
import com.blazebit.persistence.parser.expression.ArithmeticExpression;
import com.blazebit.persistence.parser.expression.ArithmeticFactor;
import com.blazebit.persistence.parser.expression.EntityLiteral;
import com.blazebit.persistence.parser.expression.EnumLiteral;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.FunctionExpression;
import com.blazebit.persistence.parser.expression.GeneralCaseExpression;
import com.blazebit.persistence.parser.expression.ListIndexExpression;
import com.blazebit.persistence.parser.expression.MapEntryExpression;
import com.blazebit.persistence.parser.expression.MapKeyExpression;
import com.blazebit.persistence.parser.expression.MapValueExpression;
import com.blazebit.persistence.parser.expression.NullExpression;
import com.blazebit.persistence.parser.expression.NumericLiteral;
import com.blazebit.persistence.parser.expression.ParameterExpression;
import com.blazebit.persistence.parser.expression.PathElementExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.PropertyExpression;
import com.blazebit.persistence.parser.expression.StringLiteral;
import com.blazebit.persistence.parser.expression.SubqueryExpression;
import com.blazebit.persistence.parser.expression.TemporalLiteral;
import com.blazebit.persistence.parser.expression.WhenClauseExpression;
import com.blazebit.persistence.parser.predicate.BooleanLiteral;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.spi.ExtendedAttribute;
import com.blazebit.persistence.spi.ExtendedManagedType;

import javax.persistence.Basic;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
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
            if (com.blazebit.persistence.parser.util.ExpressionUtils.isSizeFunction(expression)) {
                return true;
            } else {
                return super.visit(expression);
            }
        }
    };

    private ExpressionUtils() {
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

    public static boolean isNullable(EntityMetamodel metamodel, ConstantifiedJoinNodeAttributeCollector constantifiedJoinNodeAttributeCollector, Expression expr) {
        return isNullable(metamodel, constantifiedJoinNodeAttributeCollector, null, expr) ;
    }

    public static boolean isNullable(EntityMetamodel metamodel, Map<String, Type<?>> rootTypes, Expression expr) {
        return isNullable(metamodel, null, rootTypes, expr) ;

    }

    private static boolean isNullable(EntityMetamodel metamodel, ConstantifiedJoinNodeAttributeCollector constantifiedJoinNodeAttributeCollector, Map<String, Type<?>> rootTypes, Expression expr) {
        if (expr instanceof FunctionExpression) {
            return isNullable(metamodel, constantifiedJoinNodeAttributeCollector, rootTypes, (FunctionExpression) expr);
        } else if (expr instanceof PathExpression) {
            return isNullable(metamodel, constantifiedJoinNodeAttributeCollector, rootTypes, (PathExpression) expr);
        } else if (expr instanceof SubqueryExpression) {
            // Subqueries are always nullable, unless they use a count query
            AbstractCommonQueryBuilder<?, ?, ?, ?, ?> subquery = (AbstractCommonQueryBuilder<?, ?, ?, ?, ?>) ((SubqueryExpression) expr).getSubquery();
            // TODO: Ideally, we would query nullability of aggregate functions instead of relying on this
            for (SelectInfo selectInfo : subquery.selectManager.getSelectInfos()) {
                if (!com.blazebit.persistence.parser.util.ExpressionUtils.isCountFunction(selectInfo.get())) {
                    return true;
                }
            }
            return false;
        } else if (expr instanceof ParameterExpression) {
            return true;
        } else if (expr instanceof GeneralCaseExpression) {
            return isNullable(metamodel, constantifiedJoinNodeAttributeCollector, rootTypes, (GeneralCaseExpression) expr);
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
            return isNullable(metamodel, constantifiedJoinNodeAttributeCollector, rootTypes, ((ArithmeticFactor) expr).getExpression());
        } else if (expr instanceof ArithmeticExpression) {
            return isNullable(metamodel, constantifiedJoinNodeAttributeCollector, rootTypes, (ArithmeticExpression) expr);
        } else {
            throw new IllegalArgumentException("The expression of type '" + expr.getClass().getName() + "' can not be analyzed for nullability!");
        }
    }

    private static boolean isNullable(EntityMetamodel metamodel, ConstantifiedJoinNodeAttributeCollector constantifiedJoinNodeAttributeCollector, Map<String, Type<?>> rootTypes, ArithmeticExpression arithmeticExpression) {
        return isNullable(metamodel, constantifiedJoinNodeAttributeCollector, rootTypes, arithmeticExpression.getLeft()) || isNullable(metamodel, constantifiedJoinNodeAttributeCollector, rootTypes, arithmeticExpression.getRight());
    }

    private static boolean isNullable(EntityMetamodel metamodel, ConstantifiedJoinNodeAttributeCollector constantifiedJoinNodeAttributeCollector, Map<String, Type<?>> rootTypes, GeneralCaseExpression expr) {
        if (expr.getDefaultExpr() != null && isNullable(metamodel, constantifiedJoinNodeAttributeCollector, rootTypes, expr.getDefaultExpr())) {
            return true;
        }

        List<WhenClauseExpression> expressions = expr.getWhenClauses();
        int size = expressions.size();
        for (int i = 0; i < size; i++) {
            if (isNullable(metamodel, constantifiedJoinNodeAttributeCollector, rootTypes, expressions.get(i).getResult())) {
                return true;
            }
        }

        return false;
    }

    private static boolean isNullable(EntityMetamodel metamodel, ConstantifiedJoinNodeAttributeCollector constantifiedJoinNodeAttributeCollector, Map<String, Type<?>> rootTypes, FunctionExpression expr) {
        String functionName = expr.getFunctionName();
        int argumentStartIndex = 0;
        if ("FUNCTION".equalsIgnoreCase(functionName)) {
            functionName = ((StringLiteral) expr.getExpressions().get(0)).getValue();
            argumentStartIndex++;
        }
        if ("NULLIF".equalsIgnoreCase(functionName)) {
            return true;
        } else if ("COUNT".equalsIgnoreCase(expr.getFunctionName()) || AbstractCountFunction.FUNCTION_NAME.equalsIgnoreCase(functionName)) {
            return false;
        } else if ("COALESCE".equalsIgnoreCase(functionName)) {
            boolean nullable;
            List<Expression> expressions = expr.getExpressions();
            int size = expressions.size();
            for (int i = argumentStartIndex; i < size; i++) {
                nullable = isNullable(metamodel, constantifiedJoinNodeAttributeCollector, rootTypes, expressions.get(i));

                if (!nullable) {
                    return false;
                }
            }

            return true;
        } else {
            // TODO: Ideally, we would query nullability of functions instead of relying on this
            boolean nullable;
            List<Expression> expressions = expr.getExpressions();
            int size = expressions.size();
            for (int i = argumentStartIndex; i < size; i++) {
                nullable = isNullable(metamodel, constantifiedJoinNodeAttributeCollector, rootTypes, expressions.get(i));

                if (nullable) {
                    return true;
                }
            }

            return false;
        }
    }

    private static boolean isNullable(EntityMetamodel metamodel, ConstantifiedJoinNodeAttributeCollector constantifiedJoinNodeAttributeCollector, Map<String, Type<?>> rootTypes, PathExpression expr) {
        JoinNode baseNode = ((JoinNode) expr.getBaseNode());
        if (baseNode == null) {
            List<PathElementExpression> expressions = expr.getExpressions();
            PathElementExpression expression = expressions.get(0);
            if (!(expression instanceof PropertyExpression)) {
                // List or Map access, as well as Treat and Array access are always nullable
                return true;
            }
            int size = expressions.size();
            if (rootTypes == null) {
                if (size == 1 && constantifiedJoinNodeAttributeCollector != null) {
                    // In this case, the expression could be an alias referring to a non-path select item
                    AliasInfo aliasInfo = constantifiedJoinNodeAttributeCollector.getAliasManager().getAliasInfo(((PropertyExpression) expression).getProperty());
                    if (aliasInfo instanceof SelectInfo) {
                        return isNullable(metamodel, constantifiedJoinNodeAttributeCollector, ((SelectInfo) aliasInfo).getExpression());
                    }
                }
                // Not sure if this case is possible, but let's play defensive and return that this is nullable
                return true;
            }
            Type<?> baseType = rootTypes.get(((PropertyExpression) expression).getProperty());
            int i = 0;
            if (baseType != null) {
                if (size == 1) {
                    // We have to assume that any base alias reference is nullable
                    return true;
                }
                i = 1;
            } else {
                baseType = rootTypes.get("this");
            }
            for (; i < size; i++) {
                expression = expressions.get(i);
                if (!(expression instanceof PropertyExpression)) {
                    // List or Map access, as well as Treat and Array access are always nullable
                    return true;
                }
                ManagedType<?> managedType = (ManagedType<?>) baseType;
                Attribute<?, ?> attribute = managedType.getAttribute(((PropertyExpression) expression).getProperty());
                if (JpaMetamodelUtils.isNullable(attribute)) {
                    return true;
                }
                baseType = ((SingularAttribute<?, ?>) attribute).getType();
            }
            return false;
        }
        // First we check if the target attribute is optional/nullable, because then we don't need to check the join structure
        if (expr.getField() != null) {
            // If the attribute is constantified i.e. appears in a top-level EQ predicate, we can be sure it is non-nullable as well
            if (constantifiedJoinNodeAttributeCollector != null && constantifiedJoinNodeAttributeCollector.isConstantifiedNonOptional(baseNode, expr.getField())) {
                return false;
            }
            ManagedType<?> managedType = baseNode.getManagedType();
            ExtendedManagedType<?> extendedManagedType = metamodel.getManagedType(ExtendedManagedType.class, JpaMetamodelUtils.getTypeName(managedType));
            ExtendedAttribute<?, ?> attribute = extendedManagedType.getAttribute(expr.getField());
            List<Attribute<?, ?>> attributePath = attribute.getAttributePath();
            if (attributePath.size() == 1 && JpaMetamodelUtils.isNullable(attribute.getAttribute())) {
                return true;
            }
            Attribute<?, ?> firstAttribute = attributePath.get(0);
            // If the first attribute is the id, we don't have to check further, as that is not nullable
            if (!(firstAttribute instanceof SingularAttribute<?, ?>) || !((SingularAttribute<?, ?>) firstAttribute).isId()) {
                Attribute<?, ?> firstNonEmbeddableAttribute = null;
                int dotIndex = 0;
                for (Attribute<?, ?> attr : attributePath) {
                    dotIndex += attr.getName().length() + 1;
                    if (attr.getPersistentAttributeType() != Attribute.PersistentAttributeType.EMBEDDED) {
                        firstNonEmbeddableAttribute = attr;
                        break;
                    }
                }
                if (firstNonEmbeddableAttribute == null || firstNonEmbeddableAttribute.isCollection()) {
                    return true;
                }
                // Check if we have a single valued id access
                if ((!JpaMetamodelUtils.isAssociation(firstNonEmbeddableAttribute) || dotIndex > expr.getField().length()) && JpaMetamodelUtils.isNullable(attribute.getAttribute())) {
                    return true;
                }
                if (JpaMetamodelUtils.isNullable(firstNonEmbeddableAttribute)) {
                    String associationName = expr.getField().substring(0, dotIndex - 1);
                    // Finally check if the association might have been inner joined
                    JoinTreeNode associationNode = baseNode.getNodes().get(associationName);
                    if (associationNode == null || associationNode.getDefaultNode().getJoinType() != JoinType.INNER) {
                        return true;
                    }
                }
            }
        }

        // If the parent join is an INNER or RIGHT join, this can never produce null
        // We also consider CROSS joins or simple root references, which have a joinType of null, to be non-optional
        // For simplicity, we simply say that a LEFT join will always produce null
        // Since implicit joining would produce inner joins, using LEFT can only be a deliberate decision of the user
        // If the user wants to avoid implications of this path being considered nullable, the join should be changed
        // Note that a VALUES clause does not adhere to the nullability guarantees
        return baseNode.getValueCount() > 0 && baseNode.getValuesCastedParameter() == null || baseNode.getJoinType() == JoinType.LEFT || baseNode.getJoinType() == JoinType.FULL;
    }

    public static FetchType getFetchType(Attribute<?, ?> attr) {
        Member m = attr.getJavaMember();
        Set<Annotation> annotations;
        if (m instanceof Method) {
            annotations = AnnotationUtils.getAllAnnotations((Method) m);
        } else if (m instanceof Field) {
            annotations = new HashSet<>();
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
