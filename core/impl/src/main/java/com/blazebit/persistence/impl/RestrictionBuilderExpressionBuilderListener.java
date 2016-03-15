package com.blazebit.persistence.impl;

import com.blazebit.persistence.impl.builder.expression.ExpressionBuilder;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilderEndedListener;
import com.blazebit.persistence.impl.builder.predicate.RestrictionBuilderImpl;

public class RestrictionBuilderExpressionBuilderListener implements ExpressionBuilderEndedListener {
    
    private final RestrictionBuilderImpl<?> restrictionBuilder;
    
    public RestrictionBuilderExpressionBuilderListener(RestrictionBuilderImpl<?> restrictionBuilder) {
        this.restrictionBuilder = restrictionBuilder;
    }

    @Override
    public void onBuilderEnded(ExpressionBuilder builder) {
        restrictionBuilder.setLeftExpression(builder.getExpression());
    }
    
}
