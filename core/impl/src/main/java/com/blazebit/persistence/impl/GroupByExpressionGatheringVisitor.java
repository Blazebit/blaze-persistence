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

import com.blazebit.persistence.parser.expression.AbortableVisitorAdapter;
import com.blazebit.persistence.parser.expression.AggregateExpression;
import com.blazebit.persistence.parser.expression.ArithmeticExpression;
import com.blazebit.persistence.parser.expression.ArithmeticFactor;
import com.blazebit.persistence.parser.expression.ArrayExpression;
import com.blazebit.persistence.parser.expression.DateLiteral;
import com.blazebit.persistence.parser.expression.EntityLiteral;
import com.blazebit.persistence.parser.expression.EnumLiteral;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.FunctionExpression;
import com.blazebit.persistence.parser.expression.GeneralCaseExpression;
import com.blazebit.persistence.parser.expression.ListIndexExpression;
import com.blazebit.persistence.parser.expression.LiteralExpression;
import com.blazebit.persistence.parser.expression.MapEntryExpression;
import com.blazebit.persistence.parser.expression.MapKeyExpression;
import com.blazebit.persistence.parser.expression.MapValueExpression;
import com.blazebit.persistence.parser.expression.NullExpression;
import com.blazebit.persistence.parser.expression.NumericLiteral;
import com.blazebit.persistence.parser.expression.ParameterExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.PropertyExpression;
import com.blazebit.persistence.parser.expression.SimpleCaseExpression;
import com.blazebit.persistence.parser.expression.StringLiteral;
import com.blazebit.persistence.parser.expression.SubqueryExpression;
import com.blazebit.persistence.parser.expression.TimeLiteral;
import com.blazebit.persistence.parser.expression.TimestampLiteral;
import com.blazebit.persistence.parser.expression.TreatExpression;
import com.blazebit.persistence.parser.expression.TrimExpression;
import com.blazebit.persistence.parser.expression.VisitorAdapter;
import com.blazebit.persistence.parser.expression.WhenClauseExpression;
import com.blazebit.persistence.parser.predicate.BetweenPredicate;
import com.blazebit.persistence.parser.predicate.BinaryExpressionPredicate;
import com.blazebit.persistence.parser.predicate.BooleanLiteral;
import com.blazebit.persistence.parser.predicate.CompoundPredicate;
import com.blazebit.persistence.parser.predicate.InPredicate;
import com.blazebit.persistence.parser.predicate.IsEmptyPredicate;
import com.blazebit.persistence.parser.predicate.IsNullPredicate;
import com.blazebit.persistence.parser.predicate.Predicate;
import com.blazebit.persistence.spi.DbmsDialect;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Returns false if expression is required in groupBy, true otherwise
 * @author Christian Beikov
 * @since 1.0.0
 */
class GroupByExpressionGatheringVisitor extends AbortableVisitorAdapter {

    private final boolean treatSizeAsAggregate;
    private final AliasManager aliasManager;
    private final ParameterManager parameterManager;
    private final DbmsDialect dbmsDialect;
    private final Set<Expression> expressions = new LinkedHashSet<>();
    /**
     * Is set to true once a parameter/subquery expression is encountered to collect the surrounding expressions
     */
    private boolean collect;

    public GroupByExpressionGatheringVisitor(boolean treatSizeAsAggregate, AliasManager aliasManager, ParameterManager parameterManager, DbmsDialect dbmsDialect) {
        this.treatSizeAsAggregate = treatSizeAsAggregate;
        this.aliasManager = aliasManager;
        this.parameterManager = parameterManager;
        this.dbmsDialect = dbmsDialect;
    }

    public void clear() {
        expressions.clear();
        collect = false;
    }

    private boolean setCollect(boolean collect) {
        boolean oldCollect = this.collect;
        this.collect = collect;
        return oldCollect;
    }

    public Set<Expression> extractGroupByExpressions(Expression expression, ClauseType clauseType) {
        // grouping by a literal does not make sense and even causes errors in some DBs such as PostgreSQL
        // Also, grouping by just a parameter does not make sense as the value is constant
        if (expression instanceof LiteralExpression || expression instanceof ParameterExpression) {
            return Collections.emptySet();
        }
        clear();
        try {
            // When having a predicate at the top level, we have to collect
            collect = expression instanceof Predicate;
            boolean expressionWasSplit = expression.accept(this);
            // In the HAVING clause we additionally add the path expressions to the expression set for DBMS like MySQL that's don't support partial expression structure matching
            if (clauseType == ClauseType.HAVING && !dbmsDialect.supportsGroupByExpressionInHavingMatching()) {
                expression.accept(new VisitorAdapter() {
                    @Override
                    public void visit(FunctionExpression expression) {
                        // Skip aggregate expressions
                        if (expression instanceof AggregateExpression || (treatSizeAsAggregate && com.blazebit.persistence.parser.util.ExpressionUtils.isSizeFunction(expression))) {
                            return;
                        }
                        super.visit(expression);
                    }

                    @Override
                    public void visit(SubqueryExpression expression) {
                        GroupByExpressionGatheringVisitor.this.visit(expression);
                    }

                    @Override
                    public void visit(PathExpression expression) {
                        if (expression.getBaseNode() == null) {
                            ((SelectInfo) aliasManager.getAliasInfo(expression.toString())).getExpression().accept(this);
                        } else {
                            expressions.add(expression);
                        }
                    }

                    @Override
                    public void visit(TreatExpression expression) {
                        expressions.add(expression);
                    }

                    @Override
                    public void visit(PropertyExpression expression) {
                        expressions.add(expression);
                    }

                    @Override
                    public void visit(ListIndexExpression expression) {
                        expressions.add(expression);
                    }

                    @Override
                    public void visit(MapEntryExpression expression) {
                        expressions.add(expression);
                    }

                    @Override
                    public void visit(MapKeyExpression expression) {
                        expressions.add(expression);
                    }

                    @Override
                    public void visit(MapValueExpression expression) {
                        expressions.add(expression);
                    }
                });
                return expressions;
            }
            if (expressionWasSplit) {
                return expressions;
            }
        } catch (IllegalParameterException ex) {
            throw new IllegalArgumentException("Can't use the expression '" + expression + "' as an implicit group by clause, because the parameter '" + ex.parameterExpression + "' with the value '" + ex.value + "' can't be rendered as literal which is required!", ex);
        }

        return Collections.singleton(expression);
    }

    private Boolean baseExpression(Expression expression) {
        if (collect) {
            expressions.add(expression);
        }
        // No subqueries or parameters can occur from here on
        return false;
    }

    @Override
    public Boolean visit(PathExpression expression) {
        if (expression.getBaseNode() == null) {
            return ((SelectInfo) aliasManager.getAliasInfo(expression.toString())).getExpression().accept(this);
        } else {
            return baseExpression(expression);
        }
    }

    @Override
    public Boolean visit(ArrayExpression expression) {
        throw new IllegalArgumentException("At this point array expressions are not allowed anymore!");
    }

    @Override
    public Boolean visit(TreatExpression expression) {
        return baseExpression(expression);
    }

    @Override
    public Boolean visit(PropertyExpression expression) {
        return baseExpression(expression);
    }

    @Override
    public Boolean visit(ListIndexExpression expression) {
        return baseExpression(expression);
    }

    @Override
    public Boolean visit(MapEntryExpression expression) {
        return baseExpression(expression);
    }

    @Override
    public Boolean visit(MapKeyExpression expression) {
        return baseExpression(expression);
    }

    @Override
    public Boolean visit(MapValueExpression expression) {
        return baseExpression(expression);
    }

    @Override
    public Boolean visit(ParameterExpression expression) {
        // Since the parameter expression is not collected, there is no need to mark it as "used in implicit group by"
        if (collect) {
            return false;
        }
        // For implicit group by expressions we must render literals because DBMS can't be sure two question marks i.e. parameter markers,
        // will be assigned to the same value. To ensure that, we try to resolve the literal value and throw an exception if that's not possible
        String literalParameterValue = parameterManager.getLiteralParameterValue(expression, true);
        if (literalParameterValue == null) {
            throw new IllegalParameterException(expression, parameterManager.getParameterValue(expression.getName()));
        }
        parameterManager.getParameter(expression.getName()).setUsedInImplicitGroupBy(true);
        return false;
    }

    @Override
    public Boolean visit(SubqueryExpression expression) {
        if (!(expression.getSubquery() instanceof SubqueryInternalBuilder<?>)) {
            throw new IllegalArgumentException("Unexpected subquery subtype: " + expression.getSubquery());
        }
        SubqueryInternalBuilder<?> builder = (SubqueryInternalBuilder<?>) expression.getSubquery();
        expressions.addAll(builder.getCorrelatedExpressions(aliasManager));
        return true;
    }

    @Override
    public Boolean visit(ArithmeticFactor expression) {
        boolean oldCollect = setCollect(false);
        if (expression.getExpression().accept(this)) {
            setCollect(oldCollect);
            return true;
        }
        setCollect(oldCollect);

        if (oldCollect) {
            this.expressions.add(expression);
        }

        return false;
    }

    @Override
    public Boolean visit(FunctionExpression expression) {
        // When encountering an aggregate expression, we have to collect expressions of the "upper" level
        if (expression instanceof AggregateExpression || (treatSizeAsAggregate && com.blazebit.persistence.parser.util.ExpressionUtils.isSizeFunction(expression))) {
            return true;
        }

        // don't add non-deterministic functions
        if (collect) {
            String functionName;
            if (ExpressionUtils.isFunctionFunctionExpression(expression)) {
                functionName = ((StringLiteral) expression.getExpressions().get(0)).getValue();
            } else {
                functionName = expression.getFunctionName();
            }

            // Currently we only consider these functions as non-deterministic, but we might want to make this configurable
            switch (functionName.toUpperCase()) {
                case "CURRENT_DATE":
                case "CURRENT_TIME":
                case "CURRENT_TIMESTAMP":
                    return true;
                default:
                    break;
            }
        }

        boolean oldCollect = setCollect(false);
        List<Expression> expressions = expression.getExpressions();
        int size = expressions.size();
        for (int i = 0; i < size; i++) {
            if (expressions.get(i).accept(this)) {
                // Add previous expressions which are non-complex
                collectExpressions(expressions, 0, i);
                collectExpressions(expressions, i + 1, size);
                setCollect(oldCollect);
                return true;
            }
        }
        setCollect(oldCollect);

        // don't add window functions
        if (expression.getResolvedWindowDefinition() != null) {
            return true;
        }
        if (oldCollect) {
            this.expressions.add(expression);
        }

        return false;
    }

    @Override
    public Boolean visit(GeneralCaseExpression expression) {
        List<WhenClauseExpression> expressions = expression.getWhenClauses();
        int size = expressions.size();

        boolean oldCollect = setCollect(false);
        for (int i = 0; i < size; i++) {
            if (expressions.get(i).accept(this)) {
                setCollect(true);
                collectExpressions(expressions, 0, i);
                collectExpressions(expressions, i + 1, size);
                if (expression.getDefaultExpr() != null) {
                    expression.getDefaultExpr().accept(this);
                }
                setCollect(oldCollect);

                return true;
            }
        }

        if (expression.getDefaultExpr() != null && expression.getDefaultExpr().accept(this)) {
            collectExpressions(expressions, 0, size);
            setCollect(oldCollect);
            return true;
        }
        setCollect(oldCollect);

        if (oldCollect) {
            this.expressions.add(expression);
        }

        return false;
    }

    @Override
    public Boolean visit(SimpleCaseExpression expression) {
        List<WhenClauseExpression> expressions = expression.getWhenClauses();
        int size = expressions.size();

        boolean oldCollect = setCollect(false);
        if (expression.getCaseOperand().accept(this)) {
            setCollect(true);
            collectExpressions(expressions, 0, size);
            if (expression.getDefaultExpr() != null) {
                expression.getDefaultExpr().accept(this);
            }
            setCollect(oldCollect);

            return true;
        }

        for (int i = 0; i < size; i++) {
            if (expressions.get(i).accept(this)) {
                setCollect(true);
                // Add previous expressions which are non-complex
                expression.getCaseOperand().accept(this);
                collectExpressions(expressions, 0, i);
                collectExpressions(expressions, i + 1, size);
                if (expression.getDefaultExpr() != null) {
                    expression.getDefaultExpr().accept(this);
                }
                setCollect(oldCollect);

                return true;
            }
        }

        if (expression.getDefaultExpr() != null && expression.getDefaultExpr().accept(this)) {
            setCollect(true);
            // Add previous expressions which are non-complex
            expression.getCaseOperand().accept(this);
            collectExpressions(expressions, 0, size);
            setCollect(oldCollect);
            return true;
        }
        setCollect(oldCollect);

        if (oldCollect) {
            this.expressions.add(expression);
        }

        return false;
    }

    @Override
    public Boolean visit(WhenClauseExpression expression) {
        boolean oldCollect = setCollect(false);
        if (expression.getCondition().accept(this)) {
            collectExpressions(expression.getResult());
            setCollect(oldCollect);
            return true;
        }
        if (expression.getResult().accept(this)) {
            collectExpressions(expression.getCondition());
            setCollect(oldCollect);
            return true;
        }

        setCollect(oldCollect);

        if (oldCollect) {
            expression.getCondition().accept(this);
            expression.getResult().accept(this);
        }

        return false;
    }

    @Override
    public Boolean visit(TrimExpression expression) {
        boolean oldCollect = setCollect(false);
        if (expression.getTrimCharacter() != null && expression.getTrimCharacter().accept(this)) {
            collectExpressions(expression.getTrimSource());
            setCollect(oldCollect);
            return true;
        }
        if (expression.getTrimSource().accept(this)) {
            if (expression.getTrimCharacter() != null) {
                collectExpressions(expression.getTrimCharacter());
            }
            setCollect(oldCollect);
            return true;
        }
        setCollect(oldCollect);

        if (oldCollect) {
            expressions.add(expression);
        }

        return false;
    }

    @Override
    public Boolean visit(ArithmeticExpression expression) {
        boolean oldCollect = setCollect(false);
        if (expression.getLeft().accept(this)) {
            collectExpressions(expression.getRight());
            setCollect(oldCollect);
            return true;
        }
        if (expression.getRight().accept(this)) {
            collectExpressions(expression.getLeft());
            setCollect(oldCollect);
            return true;
        }
        setCollect(oldCollect);

        if (oldCollect) {
            expressions.add(expression);
        }

        return false;
    }

    @Override
    public Boolean visit(IsNullPredicate predicate) {
        if (collect) {
            predicate.getExpression().accept(this);
            return true;
        }
        return predicate.getExpression().accept(this);
    }

    @Override
    public Boolean visit(IsEmptyPredicate predicate) {
        if (collect) {
            predicate.getExpression().accept(this);
            return true;
        }
        return predicate.getExpression().accept(this);
    }

    @Override
    public Boolean visit(BetweenPredicate predicate) {
        if (collect) {
            predicate.getLeft().accept(this);
            predicate.getStart().accept(this);
            predicate.getEnd().accept(this);
            return true;
        }

        if (predicate.getLeft().accept(this)) {
            collectExpressions(predicate.getStart());
            collectExpressions(predicate.getEnd());

            return true;
        }
        if (predicate.getStart().accept(this)) {
            collectExpressions(predicate.getLeft());
            collectExpressions(predicate.getEnd());

            return true;
        }
        if (predicate.getEnd().accept(this)) {
            collectExpressions(predicate.getLeft());
            collectExpressions(predicate.getStart());
            return true;
        }

        return false;
    }

    @Override
    public Boolean visit(InPredicate predicate) {
        List<Expression> expressions = predicate.getRight();
        int size = expressions.size();

        if (collect) {
            predicate.getLeft().accept(this);

            for (int i = 0; i < size; i++) {
                expressions.get(i).accept(this);
            }

            return true;
        }

        if (predicate.getLeft().accept(this)) {
            collectExpressions(expressions);
            return true;
        }

        for (int i = 0; i < size; i++) {
            if (expressions.get(i).accept(this)) {
                // Add previous expressions which are non-complex
                collectExpressions(predicate.getLeft());
                collectExpressions(expressions, 0, i);
                // Add other expressions that are also non-complex
                collectExpressions(expressions, i + 1, size);

                return true;
            }
        }

        return false;
    }

    @Override
    protected Boolean visit(BinaryExpressionPredicate predicate) {
        if (collect) {
            predicate.getLeft().accept(this);
            predicate.getRight().accept(this);
            return true;
        }

        if (predicate.getLeft().accept(this)) {
            collectExpressions(predicate.getRight());
            return true;
        }
        if (predicate.getRight().accept(this)) {
            collectExpressions(predicate.getLeft());
            return true;
        }

        return false;
    }

    @Override
    public Boolean visit(CompoundPredicate predicate) {
        List<Predicate> children = predicate.getChildren();
        int size = children.size();

        if (collect) {
            for (int i = 0; i < size; i++) {
                children.get(i).accept(this);
            }

            return true;
        }

        for (int i = 0; i < size; i++) {
            if (children.get(i).accept(this)) {
                // Add previous expressions which are non-complex
                collectExpressions(children, 0, i);
                // Add other expressions that are also non-complex
                collectExpressions(children, i + 1, size);

                return true;
            }
        }

        return false;
    }

    /* Never collect literals */

    @Override
    public Boolean visit(NullExpression expression) {
        return false;
    }

    @Override
    public Boolean visit(NumericLiteral expression) {
        return false;
    }

    @Override
    public Boolean visit(BooleanLiteral expression) {
        return false;
    }

    @Override
    public Boolean visit(StringLiteral expression) {
        return false;
    }

    @Override
    public Boolean visit(DateLiteral expression) {
        return false;
    }

    @Override
    public Boolean visit(TimeLiteral expression) {
        return false;
    }

    @Override
    public Boolean visit(TimestampLiteral expression) {
        return false;
    }

    @Override
    public Boolean visit(EnumLiteral expression) {
        return false;
    }

    @Override
    public Boolean visit(EntityLiteral expression) {
        return false;
    }

    private void collectExpressions(Expression expression) {
        // Force collect of expressions
        boolean oldCollect = setCollect(true);
        expression.accept(this);
        setCollect(oldCollect);
    }

    private void collectExpressions(List<? extends Expression> expressions) {
        collectExpressions(expressions, 0, expressions.size());
    }

    private void collectExpressions(List<? extends Expression> expressions, int start, int end) {
        if (start >= end) {
            return;
        }
        // Force collect of expressions
        boolean oldCollect = setCollect(true);
        for (int i = start; i < end; i++) {
            expressions.get(i).accept(this);
        }
        setCollect(oldCollect);
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.6.0
     */
    private static class IllegalParameterException extends RuntimeException {

        private final ParameterExpression parameterExpression;
        private final Object value;

        public IllegalParameterException(ParameterExpression parameterExpression, Object value) {
            this.parameterExpression = parameterExpression;
            this.value = value;
        }
    }
}