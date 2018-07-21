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

package com.blazebit.persistence.parser;

import com.blazebit.persistence.parser.expression.AggregateExpression;
import com.blazebit.persistence.parser.expression.ArithmeticExpression;
import com.blazebit.persistence.parser.expression.ArithmeticFactor;
import com.blazebit.persistence.parser.expression.ArithmeticOperator;
import com.blazebit.persistence.parser.expression.ArrayExpression;
import com.blazebit.persistence.parser.expression.DateLiteral;
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
import com.blazebit.persistence.parser.expression.SimpleCaseExpression;
import com.blazebit.persistence.parser.expression.StringLiteral;
import com.blazebit.persistence.parser.expression.SubqueryExpression;
import com.blazebit.persistence.parser.expression.TimeLiteral;
import com.blazebit.persistence.parser.expression.TimestampLiteral;
import com.blazebit.persistence.parser.expression.TreatExpression;
import com.blazebit.persistence.parser.expression.TrimExpression;
import com.blazebit.persistence.parser.expression.TypeFunctionExpression;
import com.blazebit.persistence.parser.expression.WhenClauseExpression;
import com.blazebit.persistence.parser.predicate.BetweenPredicate;
import com.blazebit.persistence.parser.predicate.BooleanLiteral;
import com.blazebit.persistence.parser.predicate.CompoundPredicate;
import com.blazebit.persistence.parser.predicate.EqPredicate;
import com.blazebit.persistence.parser.predicate.ExistsPredicate;
import com.blazebit.persistence.parser.predicate.GePredicate;
import com.blazebit.persistence.parser.predicate.GtPredicate;
import com.blazebit.persistence.parser.predicate.InPredicate;
import com.blazebit.persistence.parser.predicate.IsEmptyPredicate;
import com.blazebit.persistence.parser.predicate.IsNullPredicate;
import com.blazebit.persistence.parser.predicate.LePredicate;
import com.blazebit.persistence.parser.predicate.LikePredicate;
import com.blazebit.persistence.parser.predicate.LtPredicate;
import com.blazebit.persistence.parser.predicate.MemberOfPredicate;
import com.blazebit.persistence.parser.predicate.Predicate;
import com.blazebit.persistence.parser.predicate.PredicateQuantifier;
import com.blazebit.persistence.parser.predicate.QuantifiableBinaryExpressionPredicate;
import com.blazebit.persistence.parser.util.TypeConverter;
import com.blazebit.persistence.parser.util.TypeUtils;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class SimpleQueryGenerator implements Expression.Visitor {

    private static final ThreadLocal<SimpleQueryGenerator> INSTANCE_CACHE = new ThreadLocal<SimpleQueryGenerator>() {
        @Override
        protected SimpleQueryGenerator initialValue() {
            return new SimpleQueryGenerator(new StringBuilder());
        }
    };

    protected StringBuilder sb;

    // indicates if the query generator operates in a context where it needs conditional expressions
    private BooleanLiteralRenderingContext booleanLiteralRenderingContext;
    private ParameterRenderingMode parameterRenderingMode = ParameterRenderingMode.PLACEHOLDER;

    public SimpleQueryGenerator() {
    }

    private SimpleQueryGenerator(StringBuilder sb) {
        this.sb = sb;
    }

    /**
     * Returns the thread local {@link SimpleQueryGenerator} instance.
     * Take special care that this method is not invoked in a nested context since {@link SimpleQueryGenerator} is mutable.
     * Always invoke clear
     *
     * @return the thread local instance
     */
    public static SimpleQueryGenerator getThreadLocalInstance() {
        return INSTANCE_CACHE.get();
    }

    public void generate(Expression expression) {
        expression.accept(this);
    }

    public void clear() {
        this.sb.setLength(0);
        this.booleanLiteralRenderingContext = null;
        this.parameterRenderingMode = ParameterRenderingMode.PLACEHOLDER;
    }

    public BooleanLiteralRenderingContext setBooleanLiteralRenderingContext(BooleanLiteralRenderingContext booleanLiteralRenderingContext) {
        BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = this.booleanLiteralRenderingContext;
        this.booleanLiteralRenderingContext = booleanLiteralRenderingContext;
        return oldBooleanLiteralRenderingContext;
    }

    public StringBuilder getQueryBuffer() {
        return sb;
    }

    public void setQueryBuffer(StringBuilder sb) {
        this.sb = sb;
    }

    protected String getBooleanConditionalExpression(boolean value) {
        return value ? "TRUE" : "FALSE";
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
        boolean parenthesisRequired = predicate.getChildren().size() > 1;
        if (predicate.isNegated()) {
            sb.append("NOT ");
            if (parenthesisRequired) {
                sb.append('(');
            }
        }

        if (predicate.getChildren().size() == 1) {
            predicate.getChildren().get(0).accept(this);
            return;
        }
        final int startLen = sb.length();
        final String operator = " " + predicate.getOperator().toString() + " ";
        List<Predicate> children = predicate.getChildren();
        int size = children.size();
        for (int i = 0; i < size; i++) {
            Predicate child = children.get(i);
            if (child instanceof CompoundPredicate && ((CompoundPredicate) child).getOperator() != predicate.getOperator() && !child.isNegated()) {
                sb.append("(");
                int len = sb.length();
                child.accept(this);
                // If the child was empty, we remove the opening parenthesis again
                if (len == sb.length()) {
                    sb.deleteCharAt(len - 1);
                } else {
                    sb.append(")");
                    sb.append(operator);
                }
            } else {
                child.accept(this);
                sb.append(operator);
            }
        }

        // Delete the last operator only if the children actually generated something
        if (startLen < sb.length()) {
            sb.delete(sb.length() - operator.length(), sb.length());
        }
        if (predicate.isNegated() && parenthesisRequired) {
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
        predicate.getRight().accept(this);
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
        predicate.getRight().accept(this);
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
        predicate.getStart().accept(this);
        sb.append(" AND ");
        predicate.getEnd().accept(this);
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
                predicate.getLeft().accept(this);

                if (predicate.isNegated()) {
                    sb.append(" <> ");
                } else {
                    sb.append(" = ");
                }

                right.accept(this);
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
            if (singleRightExpression instanceof ParameterExpression && ((ParameterExpression) singleRightExpression).isCollectionValued()) {
                paranthesisRequired = false;
            } else {
                paranthesisRequired = !(singleRightExpression instanceof SubqueryExpression) || !isSimpleSubquery((SubqueryExpression) singleRightExpression);
            }
        } else {
            paranthesisRequired = true;
        }
        if (paranthesisRequired) {
            sb.append('(');
        }
        if (!predicate.getRight().isEmpty()) {
            predicate.getRight().get(0).accept(this);
            for (int i = 1; i < predicate.getRight().size(); i++) {
                sb.append(", ");
                predicate.getRight().get(i).accept(this);
            }
        }
        if (paranthesisRequired) {
            sb.append(')');
        }
        setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
        setParameterRenderingMode(oldParameterRenderingMode);
    }

    protected boolean isSimpleSubquery(SubqueryExpression expression) {
        return true;
    }

    @Override
    public void visit(final ExistsPredicate predicate) {
        if (predicate.isNegated()) {
            sb.append("NOT EXISTS ");
        } else {
            sb.append("EXISTS ");
        }
        predicate.getExpression().accept(this);
    }

    private void visitQuantifiableBinaryPredicate(QuantifiableBinaryExpressionPredicate predicate, String operator) {
        BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = setBooleanLiteralRenderingContext(BooleanLiteralRenderingContext.PLAIN);
        // TODO: Currently we assume that types can be inferred, and render parameters through but e.g. ":param1 = :param2" will fail
        ParameterRenderingMode oldParameterRenderingMode = setParameterRenderingMode(ParameterRenderingMode.PLACEHOLDER);
        predicate.getLeft().accept(this);
        sb.append(operator);
        if (predicate.getQuantifier() != PredicateQuantifier.ONE) {
            sb.append(predicate.getQuantifier().toString());
        }
        predicate.getRight().accept(this);
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
            if (Character.isDigit(paramName.charAt(0))) {
                sb.append("?");
            } else {
                sb.append(":");
            }
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
        visitWhenClauseCondition(expression.getCondition());
        setParameterRenderingMode(oldParameterRenderingMode);
        sb.append(" THEN ");

        final boolean requiresParenthesis = needsParenthesisForCaseResult(expression.getResult());
        if (requiresParenthesis) {
            sb.append('(');
        }

        setBooleanLiteralRenderingContext(BooleanLiteralRenderingContext.PLAIN);
        expression.getResult().accept(this);
        setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);

        if (requiresParenthesis) {
            sb.append(')');
        }
    }

    protected void visitWhenClauseCondition(Expression condition) {
        condition.accept(this);
    }

    private void handleCaseWhen(Expression caseOperand, List<WhenClauseExpression> whenClauses, Expression defaultExpr) {
        sb.append("CASE ");
        if (caseOperand != null) {
            ParameterRenderingMode oldParameterRenderingMode = setParameterRenderingMode(ParameterRenderingMode.PLACEHOLDER);
            caseOperand.accept(this);
            setParameterRenderingMode(oldParameterRenderingMode);
            sb.append(' ');
        }

        // TODO: when a type can be inferred by the results of the WHEN or ELSE clauses, we can set PLACEHOLDER, otherwise we have to render literals for parameters

        int size = whenClauses.size();
        for (int i = 0; i < size; i++) {
            whenClauses.get(i).accept(this);
            sb.append(' ');
        }

        if (defaultExpr != null) {
            sb.append("ELSE ");
            BooleanLiteralRenderingContext oldBooleanLiteralRenderingContext = setBooleanLiteralRenderingContext(BooleanLiteralRenderingContext.PLAIN);

            final boolean requiresParenthesis = needsParenthesisForCaseResult(defaultExpr);
            if (requiresParenthesis) {
                sb.append('(');
            }

            defaultExpr.accept(this);

            if (requiresParenthesis) {
                sb.append(')');
            }
            setBooleanLiteralRenderingContext(oldBooleanLiteralRenderingContext);
            sb.append(' ');
        } else {
            sb.append("ELSE ");
            visit((NullExpression) null);
            sb.append(' ');
        }

        sb.append("END");
    }

    protected boolean needsParenthesisForCaseResult(Expression expression) {
        return false;
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
        TypeUtils.STRING_CONVERTER.appendTo(expression.getValue(), sb);
    }

    @Override
    public void visit(DateLiteral expression) {
        TypeUtils.DATE_AS_DATE_CONVERTER.appendTo(expression.getValue(), sb);
    }

    @Override
    public void visit(TimeLiteral expression) {
        TypeUtils.DATE_AS_TIME_CONVERTER.appendTo(expression.getValue(), sb);
    }

    @Override
    public void visit(TimestampLiteral expression) {
        Date value = expression.getValue();
        if (value instanceof java.sql.Timestamp) {
            TypeUtils.TIMESTAMP_CONVERTER.appendTo((Timestamp) value, sb);
        } else {
            TypeUtils.DATE_TIMESTAMP_CONVERTER.appendTo(expression.getValue(), sb);
        }
    }

    @Override
    public void visit(EnumLiteral expression) {
        sb.append(expression.getOriginalExpression());
    }

    @Override
    public void visit(EntityLiteral expression) {
        sb.append(expression.getOriginalExpression());
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public enum BooleanLiteralRenderingContext {
        PLAIN,
        PREDICATE,
        CASE_WHEN
    }

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public enum ParameterRenderingMode {
        LITERAL,
        PLACEHOLDER;
    }

}
