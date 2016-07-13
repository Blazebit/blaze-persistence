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

import com.blazebit.persistence.impl.expression.*;
import com.blazebit.persistence.impl.predicate.AndPredicate;
import com.blazebit.persistence.impl.predicate.BetweenPredicate;
import com.blazebit.persistence.impl.predicate.EqPredicate;
import com.blazebit.persistence.impl.predicate.ExistsPredicate;
import com.blazebit.persistence.impl.predicate.GePredicate;
import com.blazebit.persistence.impl.predicate.GtPredicate;
import com.blazebit.persistence.impl.predicate.InPredicate;
import com.blazebit.persistence.impl.predicate.IsEmptyPredicate;
import com.blazebit.persistence.impl.predicate.MemberOfPredicate;
import com.blazebit.persistence.impl.predicate.IsNullPredicate;
import com.blazebit.persistence.impl.predicate.LePredicate;
import com.blazebit.persistence.impl.predicate.LikePredicate;
import com.blazebit.persistence.impl.predicate.LtPredicate;
import com.blazebit.persistence.impl.predicate.NotPredicate;
import com.blazebit.persistence.impl.predicate.OrPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.predicate.QuantifiableBinaryExpressionPredicate;
import com.blazebit.persistence.impl.predicate.PredicateQuantifier;

import java.util.EnumSet;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class SimpleQueryGenerator extends VisitorAdapter {

    protected StringBuilder sb;

    // indicates if the query generator operates in a context where it needs conditional expressions
    private boolean conditionalContext;

    public boolean isConditionalContext() {
        return conditionalContext;
    }

    public boolean setConditionalContext(boolean conditionalContext) {
        boolean oldConditionalContext = this.conditionalContext;
        this.conditionalContext = conditionalContext;
        return oldConditionalContext;
    }

    public void setQueryBuffer(StringBuilder sb) {
        this.sb = sb;
    }

    protected String getBooleanConditionalExpression(boolean value) {
        return value ? "TRUE" : "FALSE";
    }

    protected String getBooleanExpression(boolean value) {
        return value ? "TRUE" : "FALSE";
    }

    protected String escapeCharacter(char character) {
        return Character.toString(character);
    }

    @Override
    public void visit(AndPredicate predicate) {
        boolean oldConditionalContext = setConditionalContext(true);
        if (predicate.getChildren().size() == 1) {
            predicate.getChildren().get(0).accept(this);
            return;
        }
        final int startLen = sb.length();
        final String and = " AND ";
        List<Predicate> children = predicate.getChildren();
        int size = children.size();
        for (int i = 0; i < size; i++) {
            Predicate child = children.get(i);
            if (child instanceof OrPredicate) {
                sb.append("(");
                int len = sb.length();
                child.accept(this);
                if (len == sb.length()) {
                    sb.deleteCharAt(len - 1);
                } else {
                    sb.append(")");
                    sb.append(and);
                }

            } else {
                int len = sb.length();
                child.accept(this);
                if (len < sb.length()) {
                    sb.append(and);
                }
            }
        }

        if (startLen < sb.length()) {
            sb.delete(sb.length() - and.length(), sb.length());
        }
        setConditionalContext(oldConditionalContext);
    }

    @Override
    public void visit(OrPredicate predicate) {
        boolean oldConditionalContext = setConditionalContext(true);
        if (predicate.getChildren().size() == 1) {
            predicate.getChildren().get(0).accept(this);
            return;
        }
        final String or = " OR ";
        List<Predicate> children = predicate.getChildren();
        int size = children.size();
        for (int i = 0; i < size; i++) {
            Predicate child = children.get(i);
            if (child instanceof AndPredicate) {
                sb.append("(");
                int len = sb.length();
                child.accept(this);
                if (len == sb.length()) {
                    sb.deleteCharAt(len - 1);
                } else {
                    sb.append(")");
                    sb.append(or);
                }

            } else {
                int len = sb.length();
                child.accept(this);
                if (len < sb.length()) {
                    sb.append(or);
                }
            }
        }
        if (predicate.getChildren().size() > 1) {
            sb.delete(sb.length() - or.length(), sb.length());
        }
        setConditionalContext(oldConditionalContext);
    }

    @Override
    public void visit(NotPredicate predicate) {
        boolean oldConditionalContext = setConditionalContext(true);
        boolean requiresParanthesis = predicate.getPredicate() instanceof AndPredicate || predicate.getPredicate() instanceof OrPredicate;
        sb.append("NOT ");
        if (requiresParanthesis) {
            sb.append("(");
            predicate.getPredicate().accept(this);
            sb.append(")");
        } else {
            predicate.getPredicate().accept(this);
        }
        setConditionalContext(oldConditionalContext);
    }

    @Override
    public void visit(EqPredicate predicate) {
        boolean oldConditionalContext = setConditionalContext(false);
        if (predicate.isNegated()) {
            visitQuantifiableBinaryPredicate(predicate, " <> ");
        } else {
            visitQuantifiableBinaryPredicate(predicate, " = ");
        }
        setConditionalContext(oldConditionalContext);
    }

    @Override
    public void visit(IsNullPredicate predicate) {
        boolean oldConditionalContext = setConditionalContext(false);
        predicate.getExpression().accept(this);
        if (predicate.isNegated()) {
            sb.append(" IS NOT NULL");
        } else {
            sb.append(" IS NULL");
        }
        setConditionalContext(oldConditionalContext);
    }

    @Override
    public void visit(IsEmptyPredicate predicate) {
        boolean oldConditionalContext = setConditionalContext(false);
        predicate.getExpression().accept(this);
        if (predicate.isNegated()) {
            sb.append(" IS NOT EMPTY");
        } else {
            sb.append(" IS EMPTY");
        }
        setConditionalContext(oldConditionalContext);
    }

    @Override
    public void visit(MemberOfPredicate predicate) {
        boolean oldConditionalContext = setConditionalContext(false);
        predicate.getLeft().accept(this);
        if (predicate.isNegated()) {
            sb.append(" NOT MEMBER OF ");
        } else {
            sb.append(" MEMBER OF ");
        }
        predicate.getRight().accept(this);
        setConditionalContext(oldConditionalContext);
    }

    @Override
    public void visit(LikePredicate predicate) {
        boolean oldConditionalContext = setConditionalContext(false);
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
        setConditionalContext(oldConditionalContext);
    }

    @Override
    public void visit(BetweenPredicate predicate) {
        boolean oldConditionalContext = setConditionalContext(false);
        predicate.getLeft().accept(this);
        if (predicate.isNegated()) {
            sb.append(" NOT BETWEEN ");
        } else {
            sb.append(" BETWEEN ");
        }
        predicate.getStart().accept(this);
        sb.append(" AND ");
        predicate.getEnd().accept(this);
        setConditionalContext(oldConditionalContext);
    }

    @Override
    public void visit(InPredicate predicate) {
        // we have to render false if the parameter list for IN is empty
        if (predicate.getRight() instanceof ParameterExpression) {
            ParameterExpression parameterExpr = (ParameterExpression) predicate.getRight();
            
            // We might have named parameters
            if (parameterExpr.getValue() != null) {
                Object list = parameterExpr.getValue();
                if (list instanceof List<?>) {
                    if (((List<?>) list).isEmpty()) {
                        // we have to distinguish between conditional and non conditional context since hibernate parser does not support
                        // literal
                        // and the workarounds like 1 = 0 or case when only work in specific contexts
                        if (conditionalContext) {
                            sb.append(getBooleanConditionalExpression(predicate.isNegated()));
                        } else {
                            sb.append(getBooleanExpression(predicate.isNegated()));
                        }
                        return;
                    }
                }
            }
        }

        boolean oldConditionalContext = setConditionalContext(false);
        predicate.getLeft().accept(this);
        if (predicate.isNegated()) {
            sb.append(" NOT");
        }
        sb.append(" IN ");

        predicate.getRight().accept(this);
        setConditionalContext(oldConditionalContext);
    }

    @Override
    public void visit(ExistsPredicate predicate) {
        boolean oldConditionalContext = setConditionalContext(false);
        if (predicate.isNegated()) {
            sb.append("NOT ");
        }
        sb.append("EXISTS ");
        predicate.getExpression().accept(this);
        setConditionalContext(oldConditionalContext);
    }

    private void visitQuantifiableBinaryPredicate(QuantifiableBinaryExpressionPredicate predicate, String operator) {
        boolean oldConditionalContext = setConditionalContext(false);
        predicate.getLeft().accept(this);
        sb.append(operator);
        if (predicate.getQuantifier() != PredicateQuantifier.ONE) {
            sb.append(predicate.getQuantifier().toString());
            wrapNonSubquery(predicate.getRight(), sb);
        } else {
            predicate.getRight().accept(this);
        }
        setConditionalContext(oldConditionalContext);
    }

    @Override
    public void visit(GtPredicate predicate) {
        visitQuantifiableBinaryPredicate(predicate, " > ");
    }

    @Override
    public void visit(GePredicate predicate) {
        visitQuantifiableBinaryPredicate(predicate, " >= ");
    }

    @Override
    public void visit(LtPredicate predicate) {
        visitQuantifiableBinaryPredicate(predicate, " < ");
    }

    @Override
    public void visit(LePredicate predicate) {
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

        sb.append(":");
        sb.append(paramName);
    }

    @Override
    public void visit(CompositeExpression expression) {
        List<Expression> expressions = expression.getExpressions();
        int size = expressions.size();
        for (int i = 0; i < size; i++) {
            expressions.get(i).accept(this);
        }
    }

    @Override
    public void visit(FooExpression expression) {
        sb.append(expression.toString());
    }

    @Override
    public void visit(LiteralExpression expression) {
        sb.append(expression.getLiteral());
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
    public void visit(FunctionExpression expression) {
        boolean oldConditionalContext = setConditionalContext(false);
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

        setConditionalContext(oldConditionalContext);
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
        boolean oldConditionalContext = setConditionalContext(true);
        expression.getCondition().accept(this);
        sb.append(" THEN ");
        setConditionalContext(false);
        expression.getResult().accept(this);
        setConditionalContext(oldConditionalContext);
    }

    private void handleCaseWhen(Expression caseOperand, List<WhenClauseExpression> whenClauses, Expression defaultExpr) {
        boolean oldConditionalContext = setConditionalContext(false);
        sb.append("CASE ");
        if (caseOperand != null) {
            caseOperand.accept(this);
            sb.append(" ");
        }

        int size = whenClauses.size();
        for (int i = 0; i < size; i++) {
            whenClauses.get(i).accept(this);
            sb.append(" ");
        }
        sb.append("ELSE ");
        defaultExpr.accept(this);
        sb.append(" END");
        setConditionalContext(oldConditionalContext);
    }

    @Override
    public void visit(ArrayExpression expression) {
        expression.getBase().accept(this);
        sb.append('[');
        expression.getIndex().accept(this);
        sb.append(']');
    }

    @Override
    public void visit(ArithmeticExpression expression) {
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
    }

    @Override
    public void visit(ArithmeticFactor expression) {
        if (expression.isInvertSignum()) {
            sb.append('-');
        }
        expression.getExpression().accept(this);
    }

    @Override
    public void visit(NumericLiteral expression) {
        sb.append(expression.getValue());
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

}
