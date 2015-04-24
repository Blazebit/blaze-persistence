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

import com.blazebit.persistence.impl.expression.AggregateExpression;
import com.blazebit.persistence.impl.expression.ArrayExpression;
import com.blazebit.persistence.impl.expression.CompositeExpression;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.FooExpression;
import com.blazebit.persistence.impl.expression.FunctionExpression;
import com.blazebit.persistence.impl.expression.GeneralCaseExpression;
import com.blazebit.persistence.impl.expression.LiteralExpression;
import com.blazebit.persistence.impl.expression.ParameterExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.SimpleCaseExpression;
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import com.blazebit.persistence.impl.expression.WhenClauseExpression;
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
import com.blazebit.persistence.impl.predicate.PredicateQuantifier;
import com.blazebit.persistence.impl.predicate.QuantifiableBinaryExpressionPredicate;
import com.blazebit.persistence.impl.expression.VisitorAdapter;
import java.util.List;

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public class SimpleQueryGenerator extends VisitorAdapter {

    protected StringBuilder sb;

    public void setQueryBuffer(StringBuilder sb) {
        this.sb = sb;
    }

    @Override
    public void visit(AndPredicate predicate) {
        if (predicate.getChildren().size() == 1) {
            predicate.getChildren().get(0).accept(this);
            return;
        }
        final int startLen = sb.length();
        final String and = " AND ";
        for (Predicate child : predicate.getChildren()) {
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
    }

    @Override
    public void visit(OrPredicate predicate) {
        if (predicate.getChildren().size() == 1) {
            predicate.getChildren().get(0).accept(this);
            return;
        }
        final String or = " OR ";
        for (Predicate child : predicate.getChildren()) {
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
    }

    @Override
    public void visit(NotPredicate predicate) {
        boolean requiresParanthesis = predicate.getPredicate() instanceof AndPredicate || predicate.getPredicate() instanceof OrPredicate;
        sb.append("NOT ");
        if (requiresParanthesis) {
            sb.append("(");
            predicate.getPredicate().accept(this);
            sb.append(")");
        } else {
            predicate.getPredicate().accept(this);
        }
    }

    @Override
    public void visit(EqPredicate predicate) {
        if (predicate.isNegated()) {
            visitQuantifiableBinaryPredicate(predicate, " <> ");
        } else {
            visitQuantifiableBinaryPredicate(predicate, " = ");
        }
    }

    @Override
    public void visit(IsNullPredicate predicate) {
        predicate.getExpression().accept(this);
        if (predicate.isNegated()) {
            sb.append(" IS NOT NULL");
        } else {
            sb.append(" IS NULL");
        }
    }

    @Override
    public void visit(IsEmptyPredicate predicate) {
        predicate.getExpression().accept(this);
        if (predicate.isNegated()) {
            sb.append(" IS NOT EMPTY");
        } else {
            sb.append(" IS EMPTY");
        }
    }

    @Override
    public void visit(MemberOfPredicate predicate) {
        predicate.getLeft().accept(this);
        if (predicate.isNegated()) {
            sb.append(" NOT MEMBER OF ");
        } else {
            sb.append(" MEMBER OF ");
        }
        predicate.getRight().accept(this);
    }

    @Override
    public void visit(LikePredicate predicate) {
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
            sb.append("'").append(predicate.getEscapeCharacter()).append("'");
            if (!predicate.isCaseSensitive()) {
                sb.append(")");
            }
        }
    }

    @Override
    public void visit(BetweenPredicate predicate) {
        predicate.getLeft().accept(this);
        if (predicate.isNegated()) {
            sb.append(" NOT BETWEEN ");
        } else {
            sb.append(" BETWEEN ");
        }
        predicate.getStart().accept(this);
        sb.append(" AND ");
        predicate.getEnd().accept(this);
    }

    @Override
    public void visit(InPredicate predicate) {
        predicate.getLeft().accept(this);
        if (predicate.isNegated()) {
            sb.append(" NOT");
        }
        sb.append(" IN ");
        predicate.getRight().accept(this);
    }

    @Override
    public void visit(ExistsPredicate predicate) {
        if (predicate.isNegated()) {
            sb.append("NOT ");
        }
        sb.append("EXISTS ");
        predicate.getExpression().accept(this);
    }

    private void visitQuantifiableBinaryPredicate(QuantifiableBinaryExpressionPredicate predicate, String operator) {
        predicate.getLeft().accept(this);
        sb.append(operator);
        if (predicate.getQuantifier() != PredicateQuantifier.ONE) {
            sb.append(predicate.getQuantifier().toString());
            wrapNonSubquery(predicate.getRight(), sb);
        } else {
            predicate.getRight().accept(this);
        }
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
        for (Expression e : expression.getExpressions()) {
            e.accept(this);
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
    public void visit(PathExpression expression) {
        sb.append(expression.getPath());
    }

    @Override
    public void visit(SubqueryExpression expression) {
        sb.append('(');
        sb.append(expression.getSubquery().getQueryString());
        sb.append(')');
    }

    @Override
    public void visit(FunctionExpression expression) {
        sb.append(expression.getFunctionName());
        sb.append('(');
        
        if (expression instanceof AggregateExpression) {
            AggregateExpression aggregateExpression = (AggregateExpression) expression;
            if (aggregateExpression.isDistinct()) {
                sb.append("DISTINCT ");
            }
        }
        
        if (!expression.getExpressions().isEmpty()) {
            expression.getExpressions().get(0).accept(this);
            for (int i = 1; i < expression.getExpressions().size(); i++) {
                sb.append(",");
                expression.getExpressions().get(i).accept(this);
            }
        }
        sb.append(')');
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
        expression.getCondition().accept(this);
        sb.append(" THEN ");
        expression.getResult().accept(this);
    }

    private void handleCaseWhen(Expression caseOperand, List<WhenClauseExpression> whenClauses, Expression defaultExpr) {
        sb.append("CASE ");
        if (caseOperand != null) {
            caseOperand.accept(this);
            sb.append(" ");
        }

        for (WhenClauseExpression whenClause : whenClauses) {
            whenClause.accept(this);
            sb.append(" ");
        }
        sb.append("ELSE ");
        defaultExpr.accept(this);
        sb.append(" END");
    }

    @Override
    public void visit(ArrayExpression expression) {
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
