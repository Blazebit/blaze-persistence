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

package com.blazebit.persistence.impl;

import com.blazebit.persistence.impl.builder.expression.ExpressionBuilder;
import com.blazebit.persistence.impl.builder.expression.ExpressionBuilderEndedListener;
import com.blazebit.persistence.impl.builder.predicate.RestrictionBuilderImpl;

/**
 * @author Christian Beikov
 * @since 1.0.0
 */
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
