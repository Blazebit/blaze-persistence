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
import com.blazebit.persistence.impl.predicate.*;

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
public class SimpleQueryGenerator extends VisitorAdapter {

    protected StringBuilder sb;

    // indicates if the query generator operates in a context where it needs conditional expressions
    private boolean conditionalContext;

    private DateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");
    private DateFormat dfTime = new SimpleDateFormat("HH:mm:ss.SSS");

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
    public void visit(final CompoundPredicate predicate) {
        boolean oldConditionalContext = setConditionalContext(true);
        handleNegatable(predicate, predicate.getChildren().size() > 1, new DeferredRenderer() {
            @Override
            public void render() {
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
            }
        });

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
    public void visit(final MemberOfPredicate predicate) {
        boolean oldConditionalContext = setConditionalContext(false);
        predicate.getLeft().accept(this);
        sb.append(" ");
        handleNegatable(predicate, false, new DeferredRenderer() {
            @Override
            public void render() {
                sb.append("MEMBER OF ");
                predicate.getRight().accept(SimpleQueryGenerator.this);
            }
        });
        setConditionalContext(oldConditionalContext);

    }

    @Override
    public void visit(final LikePredicate predicate) {
        boolean oldConditionalContext = setConditionalContext(false);
        if (!predicate.isCaseSensitive()) {
            sb.append("UPPER(");
        }
        predicate.getLeft().accept(this);
        if (!predicate.isCaseSensitive()) {
            sb.append(")");
        }
        sb.append(" ");
        handleNegatable(predicate, false, new DeferredRenderer() {
            @Override
            public void render() {
                sb.append("LIKE ");
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
            }
        });
        setConditionalContext(oldConditionalContext);
    }

    @Override
    public void visit(final BetweenPredicate predicate) {
        boolean oldConditionalContext = setConditionalContext(false);
        predicate.getLeft().accept(this);
        sb.append(" ");
        handleNegatable(predicate, false, new DeferredRenderer() {
            @Override
            public void render() {
                sb.append("BETWEEN ");
                predicate.getStart().accept(SimpleQueryGenerator.this);
                sb.append(" AND ");
                predicate.getEnd().accept(SimpleQueryGenerator.this);
            }
        });
        setConditionalContext(oldConditionalContext);
    }

    @Override
    public void visit(final InPredicate predicate) {
        // we have to render false if the parameter list for IN is empty
        if (predicate.getRight().size() == 1 && predicate.getRight().get(0) instanceof ParameterExpression) {
            ParameterExpression parameterExpr = (ParameterExpression) predicate.getRight().get(0);
            
            // We might have named parameters
            if (parameterExpr.getValue() != null && parameterExpr.isCollectionValued()) {
                Object collection = parameterExpr.getValue();
                if (((Collection<?>) collection).isEmpty()) {
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

        boolean oldConditionalContext = setConditionalContext(false);
        predicate.getLeft().accept(this);
        sb.append(" ");
        handleNegatable(predicate, false, new DeferredRenderer() {
            @Override
            public void render() {
                sb.append("IN ");

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
            }
        });
        setConditionalContext(oldConditionalContext);
    }

    @Override
    public void visit(final ExistsPredicate predicate) {
        boolean oldConditionalContext = setConditionalContext(false);
        handleNegatable(predicate, false, new DeferredRenderer() {
            @Override
            public void render() {
                sb.append("EXISTS ");
                predicate.getExpression().accept(SimpleQueryGenerator.this);
            }
        });
        setConditionalContext(oldConditionalContext);
    }

    private void visitQuantifiableBinaryPredicate(QuantifiableBinaryExpressionPredicate predicate, String operator) {
        boolean oldConditionalContext = setConditionalContext(false);
        predicate.getLeft().accept(SimpleQueryGenerator.this);
        sb.append(operator);
        if (predicate.getQuantifier() != PredicateQuantifier.ONE) {
            sb.append(predicate.getQuantifier().toString());
            wrapNonSubquery(predicate.getRight(), sb);
        } else {
            predicate.getRight().accept(SimpleQueryGenerator.this);
        }
        setConditionalContext(oldConditionalContext);
    }

    @Override
    public void visit(final GtPredicate predicate) {
        handleNegatable(predicate, false, new DeferredRenderer() {
                    @Override
                    public void render() {
                        visitQuantifiableBinaryPredicate(predicate, " > ");
                    }
                }
        );
    }

    @Override
    public void visit(final GePredicate predicate) {
        handleNegatable(predicate, false, new DeferredRenderer() {
                    @Override
                    public void render() {
                        visitQuantifiableBinaryPredicate(predicate, " >= ");
                    }
                }
        );
    }

    @Override
    public void visit(final LtPredicate predicate) {
        handleNegatable(predicate, false, new DeferredRenderer() {
                    @Override
                    public void render() {
                        visitQuantifiableBinaryPredicate(predicate, " < ");
                    }
                }
        );
    }

    @Override
    public void visit(final LePredicate predicate) {
        handleNegatable(predicate, false, new DeferredRenderer() {
                    @Override
                    public void render() {
                        visitQuantifiableBinaryPredicate(predicate, " <= ");
                    }
                }
        );
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
    public void visit(TypeFunctionExpression expression) {
        visit((FunctionExpression) expression);
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

    @Override
    public void visit(final BooleanLiteral expression) {
        handleNegatable(expression, false, new DeferredRenderer() {
            @Override
            public void render() {
                sb.append(expression.getValue());
            }
        });
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

    private void handleNegatable(Negatable negatable, boolean paranthesisRequired, DeferredRenderer renderer) {
        if (negatable.isNegated()) {
            sb.append("NOT ");
            if (paranthesisRequired) {
                sb.append('(');
            }
            renderer.render();
            if (paranthesisRequired) {
                sb.append(')');
            }
        } else {
            renderer.render();
        }
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

    private static interface DeferredRenderer {
        public void render();
    }

}
