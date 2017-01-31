/*
 * Copyright 2014 - 2017 Blazebit.
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

import com.blazebit.persistence.impl.expression.AggregateExpression;
import com.blazebit.persistence.impl.expression.ArithmeticExpression;
import com.blazebit.persistence.impl.expression.ArithmeticFactor;
import com.blazebit.persistence.impl.expression.ArithmeticOperator;
import com.blazebit.persistence.impl.expression.ArrayExpression;
import com.blazebit.persistence.impl.expression.DateLiteral;
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
import com.blazebit.persistence.impl.expression.PathElementExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.PropertyExpression;
import com.blazebit.persistence.impl.expression.SimpleCaseExpression;
import com.blazebit.persistence.impl.expression.StringLiteral;
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import com.blazebit.persistence.impl.expression.TimeLiteral;
import com.blazebit.persistence.impl.expression.TimestampLiteral;
import com.blazebit.persistence.impl.expression.TreatExpression;
import com.blazebit.persistence.impl.expression.TrimExpression;
import com.blazebit.persistence.impl.expression.TypeFunctionExpression;
import com.blazebit.persistence.impl.expression.WhenClauseExpression;
import com.blazebit.persistence.impl.predicate.BetweenPredicate;
import com.blazebit.persistence.impl.predicate.BooleanLiteral;
import com.blazebit.persistence.impl.predicate.CompoundPredicate;
import com.blazebit.persistence.impl.predicate.EqPredicate;
import com.blazebit.persistence.impl.predicate.ExistsPredicate;
import com.blazebit.persistence.impl.predicate.GePredicate;
import com.blazebit.persistence.impl.predicate.GtPredicate;
import com.blazebit.persistence.impl.predicate.InPredicate;
import com.blazebit.persistence.impl.predicate.IsEmptyPredicate;
import com.blazebit.persistence.impl.predicate.IsNullPredicate;
import com.blazebit.persistence.impl.predicate.LePredicate;
import com.blazebit.persistence.impl.predicate.LikePredicate;
import com.blazebit.persistence.impl.predicate.LtPredicate;
import com.blazebit.persistence.impl.predicate.MemberOfPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.predicate.PredicateQuantifier;
import com.blazebit.persistence.impl.predicate.QuantifiableBinaryExpressionPredicate;
import com.blazebit.persistence.impl.util.TypeConverter;
import com.blazebit.persistence.impl.util.TypeUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class SimpleQueryGenerator implements Expression.Visitor {

    protected StringBuilder sb;

    // indicates if the query generator operates in a context where it needs conditional expressions
    private BooleanLiteralRenderingContext booleanLiteralRenderingContext;
    private ParameterRenderingMode parameterRenderingMode = ParameterRenderingMode.PLACEHOLDER;

    private DateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");
    private DateFormat dfTime = new SimpleDateFormat("HH:mm:ss.SSS");

    public BooleanLiteralRenderingContext setBooleanLiteralRenderingContext(BooleanLiteralRenderingContext booleanLiteralRenderingContext) {
        BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = this.booleanLiteralRenderingContext;
        this.booleanLiteralRenderingContext = booleanLiteralRenderingContext;
        return oldBooleanLiteralRenderingContext;
    }

    public void setQueryBuffer(StringBuilder sb) {
        this.sb = sb;
    }

    protected String getBooleanConditionalExpression(boolean value) {
        return value ? "TRUE" : "FALSE";
    }

    public ParameterRenderingMode getParameterRenderingMode() {
        return parameterRenderingMode;
    }

    public ParameterRenderingMode setParameterRenderingMode(ParameterRenderingMode parameterRenderingMode) {
        ParameterRenderingMode oldParameterRenderingMode = this.parameterRenderingMode;
        this.parameterRenderingMode = parameterRenderingMode;
        return oldParameterRenderingMode;
    }

    protected String getBooleanExpression(boolean value) {
        return value ? "TRUE" : "FALSE";
    }

    protected String escapeCharacter(char character) {
        return Character.toString(character);
    }

    @Override
    public void visit(final CompoundPredicate predicate) {
        BooleanLiteralRenderingContext oldConditionalContext = setBooleanLiteralRenderingContext(BooleanLiteralRenderingContext.PREDICATE);
        ParameterRenderingMode oldParameterRenderingMode = setParameterRenderingMode(ParameterRenderingMode.PLACEHOLDER);
        boolean paranthesisRequired = predicate.getChildren().size() > 1;
        if (predicate.isNegated()) {
            sb.append("NOT ");
            if (paranthesisRequired) {
                sb.append('(');
            }
        }

        if (predicate.getChildren().size() == 1) {
            predicate.getChildren().get(0).accept(SimpleQueryGenerator.this);
            return;
        }
        final int startLen = sb.length();
        final String and = " " + predicate.getOperator().toString() + " ";
        List<Predicate> children = predicate.getChildren();
        int size = children.size();
        for (int i = 0; i < size; i++) {
            Predicate child = children.get(i);
            if (child instanceof CompoundPredicate && ((CompoundPredicate) child).getOperator() != predicate.getOperator() && !child.isNegated()) {
                sb.append("(");
                int len = sb.length();
                child.accept(SimpleQueryGenerator.this);
                if (len == sb.length()) {
                    sb.deleteCharAt(len - 1);
                } else {
                    sb.append(")");
                    sb.append(and);
                }

            } else {
                int len = sb.length();
                child.accept(SimpleQueryGenerator.this);
                if (len < sb.length()) {
                    sb.append(and);
                }
            }
        }

        if (startLen < sb.length()) {
            sb.delete(sb.length() - and.length(), sb.length());
        }
        if (predicate.isNegated() && paranthesisRequired) {
            sb.append(')');
        }
        setBooleanLiteralRenderingContext(oldConditionalContext);
        setParameterRenderingMode(oldParameterRenderingMode);
    }

    @Override
    public void visit(final EqPredicate predicate) {
        if (predicate.isNegated()) {
            visitQuantifiableBinaryPredicate(predicate, " <> ");
        } else {
            visitQuantifiableBinaryPredicate(predicate, " = ");
        }
    }

    @Override
    public void visit(IsNullPredicate predicate) {
        // Null check does not require a type to be known
        ParameterRenderingMode oldParameterRenderingMode = setParameterRenderingMode(ParameterRenderingMode.PLACEHOLDER);
        predicate.getExpression().accept(this);
        if (predicate.isNegated()) {
            sb.append(" IS NOT NULL");
        } else {
            sb.append(" IS NULL");
        }
        setParameterRenderingMode(oldParameterRenderingMode);
    }

    @Override
    public void visit(IsEmptyPredicate predicate) {
        // IS EMPTY requires a collection expression, so no need to set the nested context
        predicate.getExpression().accept(this);
        if (predicate.isNegated()) {
            sb.append(" IS NOT EMPTY");
        } else {
            sb.append(" IS EMPTY");
        }
    }

    @Override
    public void visit(final MemberOfPredicate predicate) {
        BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = setBooleanLiteralRenderingContext(BooleanLiteralRenderingContext.PLAIN);
        // Since MEMBER OF requires a collection expression on the RHS, we can safely assume parameters are fine
        ParameterRenderingMode oldParameterRenderingMode = setParameterRenderingMode(ParameterRenderingMode.PLACEHOLDER);
        predicate.getLeft().accept(this);
        setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
        setParameterRenderingMode(oldParameterRenderingMode);
        if (predicate.isNegated()) {
            sb.append(" NOT MEMBER OF ");
        } else {
            sb.append(" MEMBER OF ");
        }
        predicate.getRight().accept(SimpleQueryGenerator.this);
    }

    @Override
    public void visit(final LikePredicate predicate) {
        // Since like is defined for Strings, we can always infer types
        ParameterRenderingMode oldParameterRenderingMode = setParameterRenderingMode(ParameterRenderingMode.PLACEHOLDER);
        if (!predicate.isCaseSensitive()) {
            sb.append("UPPER(");
        }
        predicate.getLeft().accept(this);
        if (!predicate.isCaseSensitive()) {
            sb.append(")");
        }
        if (predicate.isNegated()) {
            sb.append(" NOT LIKE ");
        } else {
            sb.append(" LIKE ");
        }
        if (!predicate.isCaseSensitive()) {
            sb.append("UPPER(");
        }
        predicate.getRight().accept(SimpleQueryGenerator.this);
        if (!predicate.isCaseSensitive()) {
            sb.append(")");
        }
        if (predicate.getEscapeCharacter() != null) {
            sb.append(" ESCAPE ");
            if (!predicate.isCaseSensitive()) {
                sb.append("UPPER(");
            }
            sb.append("'").append(escapeCharacter(predicate.getEscapeCharacter())).append("'");
            if (!predicate.isCaseSensitive()) {
                sb.append(")");
            }
        }
        setParameterRenderingMode(oldParameterRenderingMode);
    }

    @Override
    public void visit(final BetweenPredicate predicate) {
        // TODO: when a type can be inferred by the results of the WHEN or ELSE clauses, we can set PLACEHOLDER, otherwise we have to render literals for parameters
        // TODO: Currently we assume that types can be inferred, and render parameters through but e.g. ":param1 BETWEEN :param2 AND :param3" will fail
        ParameterRenderingMode oldParameterRenderingMode = setParameterRenderingMode(ParameterRenderingMode.PLACEHOLDER);
        predicate.getLeft().accept(this);
        if (predicate.isNegated()) {
            sb.append(" NOT BETWEEN ");
        } else {
            sb.append(" BETWEEN ");
        }
        predicate.getStart().accept(SimpleQueryGenerator.this);
        sb.append(" AND ");
        predicate.getEnd().accept(SimpleQueryGenerator.this);
        setParameterRenderingMode(oldParameterRenderingMode);
    }

    @Override
    public void visit(final InPredicate predicate) {
        // we have to render false if the parameter list for IN is empty
        if (predicate.getRight().size() == 1) {
            Expression right = predicate.getRight().get(0);
            if (right instanceof ParameterExpression) {
                ParameterExpression parameterExpr = (ParameterExpression) right;

                // We might have named parameters
                if (parameterExpr.getValue() != null && parameterExpr.isCollectionValued()) {
                    Object collection = parameterExpr.getValue();
                    if (((Collection<?>) collection).isEmpty()) {
                        // we have to distinguish between conditional and non conditional context since hibernate parser does not support
                        // literal
                        // and the workarounds like 1 = 0 or case when only work in specific contexts
                        if (booleanLiteralRenderingContext == BooleanLiteralRenderingContext.PREDICATE) {
                            sb.append(getBooleanConditionalExpression(predicate.isNegated()));
                        } else {
                            sb.append(getBooleanExpression(predicate.isNegated()));
                        }
                        return;
                    }
                }
            } else if (right instanceof PathExpression) {
                // NOTE: this is a special case where we can transform an IN predicate to an equality predicate
                BooleanLiteralRenderingContext oldConditionalContext = setBooleanLiteralRenderingContext(BooleanLiteralRenderingContext.PLAIN);
                predicate.getLeft().accept(SimpleQueryGenerator.this);

                if (predicate.isNegated()) {
                    sb.append(" <> ");
                } else {
                    sb.append(" = ");
                }

                right.accept(SimpleQueryGenerator.this);
                setBooleanLiteralRenderingContext(oldConditionalContext);
                return;
            }
        }

        BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = setBooleanLiteralRenderingContext(BooleanLiteralRenderingContext.PLAIN);
        ParameterRenderingMode oldParameterRenderingMode = setParameterRenderingMode(ParameterRenderingMode.PLACEHOLDER);
        predicate.getLeft().accept(this);
        if (predicate.isNegated()) {
            sb.append(" NOT IN ");
        } else {
            sb.append(" IN ");
        }

        boolean paranthesisRequired;
        if (predicate.getRight().size() == 1) {
            // NOTE: other cases are handled by ResolvingQueryGenerator
            Expression singleRightExpression = predicate.getRight().get(0);
            if (singleRightExpression instanceof ParameterExpression && ((ParameterExpression) singleRightExpression).isCollectionValued() || singleRightExpression instanceof SubqueryExpression) {
                paranthesisRequired = false;
            } else {
                paranthesisRequired = true;
            }
        } else {
            paranthesisRequired = true;
        }
        if (paranthesisRequired) {
            sb.append('(');
        }
        if (!predicate.getRight().isEmpty()) {
            predicate.getRight().get(0).accept(SimpleQueryGenerator.this);
            for (int i = 1; i < predicate.getRight().size(); i++) {
                sb.append(", ");
                predicate.getRight().get(i).accept(SimpleQueryGenerator.this);
            }
        }
        if (paranthesisRequired) {
            sb.append(')');
        }
        setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
        setParameterRenderingMode(oldParameterRenderingMode);
    }

    @Override
    public void visit(final ExistsPredicate predicate) {
        if (predicate.isNegated()) {
            sb.append("NOT EXISTS ");
        } else {
            sb.append("EXISTS ");
        }
        predicate.getExpression().accept(SimpleQueryGenerator.this);
    }

    private void visitQuantifiableBinaryPredicate(QuantifiableBinaryExpressionPredicate predicate, String operator) {
        BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = setBooleanLiteralRenderingContext(BooleanLiteralRenderingContext.PLAIN);
        // TODO: Currently we assume that types can be inferred, and render parameters through but e.g. ":param1 = :param2" will fail
        ParameterRenderingMode oldParameterRenderingMode = setParameterRenderingMode(ParameterRenderingMode.PLACEHOLDER);
        predicate.getLeft().accept(SimpleQueryGenerator.this);
        sb.append(operator);
        if (predicate.getQuantifier() != PredicateQuantifier.ONE) {
            sb.append(predicate.getQuantifier().toString());
            wrapNonSubquery(predicate.getRight(), sb);
        } else {
            predicate.getRight().accept(SimpleQueryGenerator.this);
        }
        setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
        setParameterRenderingMode(oldParameterRenderingMode);
    }

    @Override
    public void visit(final GtPredicate predicate) {
        if (predicate.isNegated()) {
            sb.append("NOT ");
        }
        visitQuantifiableBinaryPredicate(predicate, " > ");
    }

    @Override
    public void visit(final GePredicate predicate) {
        if (predicate.isNegated()) {
            sb.append("NOT ");
        }
        visitQuantifiableBinaryPredicate(predicate, " >= ");
    }

    @Override
    public void visit(final LtPredicate predicate) {
        if (predicate.isNegated()) {
            sb.append("NOT ");
        }
        visitQuantifiableBinaryPredicate(predicate, " < ");
    }

    @Override
    public void visit(final LePredicate predicate) {
        if (predicate.isNegated()) {
            sb.append("NOT ");
        }
        visitQuantifiableBinaryPredicate(predicate, " <= ");
    }

    @Override
    public void visit(ParameterExpression expression) {
        String paramName;
        if (expression.getName() == null) {
            throw new IllegalStateException("Unsatisfied parameter " + expression.getName());
        } else {
            paramName = expression.getName();
        }

        String value;
        if (ParameterRenderingMode.LITERAL == parameterRenderingMode && (value = getLiteralParameterValue(expression)) != null) {
            sb.append(value);
        } else {
            sb.append(":");
            sb.append(paramName);
        }
    }

    protected String getLiteralParameterValue(ParameterExpression expression) {
        Object value = expression.getValue();
        if (value != null) {
            final TypeConverter<Object> converter = (TypeConverter<Object>) TypeUtils.getConverter(value.getClass());
            return converter.toString(value);
        }

        return null;
    }

    @Override
    public void visit(NullExpression expression) {
        sb.append("NULL");
    }

    @Override
    public void visit(PathExpression expression) {
        List<PathElementExpression> pathProperties = expression.getExpressions();
        int size = pathProperties.size();
        if (size == 0) {
            return;
        } else if (size == 1) {
            pathProperties.get(0).accept(this);
            return;
        }

        pathProperties.get(0).accept(this);

        for (int i = 1; i < size; i++) {
            sb.append(".");
            pathProperties.get(i).accept(this);
        }
    }

    @Override
    public void visit(PropertyExpression expression) {
        sb.append(expression.getProperty());
    }

    @Override
    public void visit(SubqueryExpression expression) {
        sb.append('(');
        sb.append(expression.getSubquery().getQueryString());
        sb.append(')');
    }

    @Override
    public void visit(ListIndexExpression expression) {
        sb.append("INDEX(");
        expression.getPath().accept(this);
        sb.append(')');
    }

    @Override
    public void visit(MapEntryExpression expression) {
        sb.append("ENTRY(");
        expression.getPath().accept(this);
        sb.append(')');
    }

    @Override
    public void visit(MapKeyExpression expression) {
        sb.append("KEY(");
        expression.getPath().accept(this);
        sb.append(')');
    }

    @Override
    public void visit(MapValueExpression expression) {
        sb.append("VALUE(");
        expression.getPath().accept(this);
        sb.append(')');
    }

    @Override
    public void visit(FunctionExpression expression) {
        BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = setBooleanLiteralRenderingContext(BooleanLiteralRenderingContext.PLAIN);
        ParameterRenderingMode oldParameterRenderingMode = setParameterRenderingMode(ParameterRenderingMode.PLACEHOLDER);
        boolean hasExpressions = expression.getExpressions().size() > 0;
        String functionName = expression.getFunctionName();
        sb.append(functionName);

        // @formatter:off
        if (!"CURRENT_TIME".equalsIgnoreCase(functionName)
             && !"CURRENT_DATE".equalsIgnoreCase(functionName)
             && !"CURRENT_TIMESTAMP".equalsIgnoreCase(functionName)) {
            // @formatter:on
            sb.append('(');

            if (expression instanceof AggregateExpression) {
                AggregateExpression aggregateExpression = (AggregateExpression) expression;
                if (aggregateExpression.isDistinct()) {
                    sb.append("DISTINCT ");
                }
                if (!hasExpressions && "COUNT".equalsIgnoreCase(aggregateExpression.getFunctionName())) {
                    sb.append('*');
                }
            }

            if (hasExpressions) {
                expression.getExpressions().get(0).accept(this);
                for (int i = 1; i < expression.getExpressions().size(); i++) {
                    sb.append(",");
                    expression.getExpressions().get(i).accept(this);
                }
            }
            sb.append(')');
        }

        setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
        setParameterRenderingMode(oldParameterRenderingMode);
    }

    @Override
    public void visit(TypeFunctionExpression expression) {
        visit((FunctionExpression) expression);
    }

    @Override
    public void visit(TrimExpression expression) {
        ParameterRenderingMode oldParameterRenderingMode = setParameterRenderingMode(ParameterRenderingMode.PLACEHOLDER);
        sb.append("TRIM(").append(expression.getTrimspec().name()).append(' ');

        if (expression.getTrimCharacter() != null) {
            expression.getTrimCharacter().accept(this);
            sb.append(' ');
        }

        sb.append("FROM ");
        expression.getTrimSource().accept(this);
        sb.append(')');
        setParameterRenderingMode(oldParameterRenderingMode);
    }

    @Override
    public void visit(GeneralCaseExpression expression) {
        handleCaseWhen(null, expression.getWhenClauses(), expression.getDefaultExpr());
    }

    @Override
    public void visit(SimpleCaseExpression expression) {
        handleCaseWhen(expression.getCaseOperand(), expression.getWhenClauses(), expression.getDefaultExpr());
    }

    @Override
    public void visit(WhenClauseExpression expression) {
        sb.append("WHEN ");
        BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = setBooleanLiteralRenderingContext(BooleanLiteralRenderingContext.PREDICATE);
        ParameterRenderingMode oldParameterRenderingMode = setParameterRenderingMode(ParameterRenderingMode.PLACEHOLDER);
        expression.getCondition().accept(this);
        setParameterRenderingMode(oldParameterRenderingMode);
        sb.append(" THEN ");
        setBooleanLiteralRenderingContext(BooleanLiteralRenderingContext.PLAIN);
        expression.getResult().accept(this);
        setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
    }

    private void handleCaseWhen(Expression caseOperand, List<WhenClauseExpression> whenClauses, Expression defaultExpr) {
        sb.append("CASE ");
        if (caseOperand != null) {
            ParameterRenderingMode oldParameterRenderingMode = setParameterRenderingMode(ParameterRenderingMode.PLACEHOLDER);
            caseOperand.accept(this);
            setParameterRenderingMode(oldParameterRenderingMode);
            sb.append(" ");
        }

        // TODO: when a type can be inferred by the results of the WHEN or ELSE clauses, we can set PLACEHOLDER, otherwise we have to render literals for parameters

        int size = whenClauses.size();
        for (int i = 0; i < size; i++) {
            whenClauses.get(i).accept(this);
            sb.append(" ");
        }
        sb.append("ELSE ");
        BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = setBooleanLiteralRenderingContext(BooleanLiteralRenderingContext.PLAIN);
        defaultExpr.accept(this);
        sb.append(" END");
        setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
    }

    @Override
    public void visit(ArrayExpression expression) {
        expression.getBase().accept(this);
        sb.append('[');
        expression.getIndex().accept(this);
        sb.append(']');
    }

    @Override
    public void visit(TreatExpression expression) {
        sb.append("TREAT(");
        expression.getExpression().accept(this);
        sb.append(" AS ");
        sb.append(expression.getType());
        sb.append(')');
    }

    @Override
    public void visit(ArithmeticExpression expression) {
        ParameterRenderingMode oldParameterRenderingMode = setParameterRenderingMode(ParameterRenderingMode.PLACEHOLDER);
        ArithmeticOperator op = expression.getOp();
        if (expression.getLeft() instanceof  ArithmeticExpression) {
            ArithmeticExpression left = (ArithmeticExpression) expression.getLeft();
            ArithmeticOperator leftOp = left.getOp();
            if (leftOp == ArithmeticOperator.DIVISION // (1 / 3) / 4
                    || !op.isAddOrSubtract() && leftOp.isAddOrSubtract() // (1 - 3) * 4
            ) {
                sb.append("(");
                expression.getLeft().accept(this);
                sb.append(")");
            } else {
                expression.getLeft().accept(this);
            }
        } else {
            expression.getLeft().accept(this);
        }
        sb.append(" ").append(expression.getOp().getSymbol()).append(" ");

        if (expression.getRight() instanceof  ArithmeticExpression) {
            ArithmeticExpression right = (ArithmeticExpression) expression.getRight();
            ArithmeticOperator rightOp = right.getOp();

            if (rightOp == ArithmeticOperator.DIVISION    // 1 / (3 / 4)
                    || op == ArithmeticOperator.SUBTRACTION // 1 - (3 + 4)
                    || !op.isAddOrSubtract() && rightOp.isAddOrSubtract() // 1 - (3 * 4)
            ) {
                sb.append("(");
                expression.getRight().accept(this);
                sb.append(")");
            } else {
                expression.getRight().accept(this);
            }
        } else {
            expression.getRight().accept(this);
        }
        setParameterRenderingMode(oldParameterRenderingMode);
    }

    @Override
    public void visit(ArithmeticFactor expression) {
        ParameterRenderingMode oldParameterRenderingMode = setParameterRenderingMode(ParameterRenderingMode.PLACEHOLDER);
        if (expression.isInvertSignum()) {
            sb.append('-');
        }
        expression.getExpression().accept(this);
        setParameterRenderingMode(oldParameterRenderingMode);
    }

    @Override
    public void visit(NumericLiteral expression) {
        sb.append(expression.getValue());
    }

    @Override
    public void visit(final BooleanLiteral expression) {
        if (expression.isNegated()) {
            sb.append(" NOT ");
        }
        switch (booleanLiteralRenderingContext) {
            case PLAIN:
                sb.append(expression.getValue());
                break;
            case PREDICATE:
                sb.append(getBooleanConditionalExpression(expression.getValue()));
                break;
            case CASE_WHEN:
                sb.append(getBooleanExpression(expression.getValue()));
                break;
            default:
                throw new IllegalArgumentException("Invalid boolean literal rendering context: " + booleanLiteralRenderingContext);
        }
    }

    @Override
    public void visit(StringLiteral expression) {
        sb.append('\'').append(expression.getValue()).append('\'');
    }

    @Override
    public void visit(DateLiteral expression) {
        Date value = expression.getValue();
        sb.append("{d '")
                .append(dfDate.format(value))
                .append("'}");
    }

    @Override
    public void visit(TimeLiteral expression) {
        Date value = expression.getValue();
        sb.append("{t '")
                .append(dfTime.format(value))
                .append("'}");
    }

    @Override
    public void visit(TimestampLiteral expression) {
        Date value = expression.getValue();
        sb.append("{ts '")
                .append(dfDate.format(value))
                .append(' ')
                .append(dfTime.format(value))
                .append("'}");
    }

    @Override
    public void visit(EnumLiteral expression) {
        sb.append(expression.getOriginalExpression());
    }

    @Override
    public void visit(EntityLiteral expression) {
        sb.append(expression.getOriginalExpression());
    }

    private void wrapNonSubquery(Expression p, StringBuilder sb) {
        boolean isNotSubquery = !(p instanceof SubqueryExpression);
        if (isNotSubquery) {
            sb.append("(");
        }
        p.accept(this);
        if (isNotSubquery) {
            sb.append(")");
        }
    }

    public enum BooleanLiteralRenderingContext {
        PLAIN,
        PREDICATE,
        CASE_WHEN
    }

    public enum ParameterRenderingMode {
        LITERAL,
        PLACEHOLDER;
    }

}
