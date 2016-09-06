package com.blazebit.persistence.impl;

import java.util.LinkedHashSet;
import java.util.Set;

import com.blazebit.persistence.impl.expression.*;

class GroupByExpressionGatheringVisitor extends VisitorAdapter {

    private final GroupByUsableDetectionVisitor groupByUsableDetectionVisitor;
    private Set<Expression> expressions = new LinkedHashSet<Expression>();

    public GroupByExpressionGatheringVisitor() {
    	this(false);
    }
    
    public GroupByExpressionGatheringVisitor(boolean treatSizeAsAggregate) {
    	this.groupByUsableDetectionVisitor = new GroupByUsableDetectionVisitor(treatSizeAsAggregate);
	}

	public Set<Expression> getExpressions() {
        return expressions;
    }

    private boolean handleExpression(Expression expression) {
        if (expression.accept(groupByUsableDetectionVisitor)) {
        	return true;
        } else {
        	expressions.add(expression);
	        return false;
        }
    }

    @Override
    public void visit(PathExpression expression) {
        handleExpression(expression);
    }

    @Override
    public void visit(ArrayExpression expression) {
        throw new IllegalArgumentException("At this point array expressions are not allowed anymore!");
    }

    @Override
    public void visit(TreatExpression expression) {
        handleExpression(expression);
    }

    @Override
    public void visit(PropertyExpression expression) {
        handleExpression(expression);
    }

    @Override
    public void visit(ParameterExpression expression) {
    }

    @Override
    public void visit(ArithmeticExpression expression) {
        handleExpression(expression);
    }

    @Override
    public void visit(ArithmeticFactor expression) {
        handleExpression(expression);
    }

    @Override
    public void visit(CompositeExpression expression) {
        if (handleExpression(expression)) {
            // TODO: This is wrong, but something similar would be needed
            // for (Expression expr : expression.getExpressions()) {
            // expr.accept(this);
            // }
        }
    }

    @Override
    public void visit(FooExpression expression) {
        // We skip this, because foo expressions as part of a predicate is not grouping relevant
    }

    @Override
    public void visit(LiteralExpression expression) {
        handleExpression(expression);
    }

    @Override
    public void visit(NullExpression expression) {
        // We skip this, because grouping by null does not make sense
    }

    @Override
    public void visit(SubqueryExpression expression) {
        // We skip subqueries since we can't use them in group bys
    }

    @Override
    public void visit(FunctionExpression expression) {
        if (!(expression instanceof AggregateExpression)) {
            handleExpression(expression);
        }
    }

    @Override
    public void visit(GeneralCaseExpression expression) {
        super.visit(expression);
        handleExpression(expression);
    }

    @Override
    public void visit(SimpleCaseExpression expression) {
        super.visit(expression);
        handleExpression(expression);
    }
}