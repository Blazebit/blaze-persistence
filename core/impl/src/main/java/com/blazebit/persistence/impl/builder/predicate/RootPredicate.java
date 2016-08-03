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
package com.blazebit.persistence.impl.builder.predicate;

import com.blazebit.persistence.impl.ParameterManager;
import com.blazebit.persistence.impl.expression.BooleanExpression;
import com.blazebit.persistence.impl.expression.VisitorAdapter;
import com.blazebit.persistence.impl.expression.AndExpression;
import com.blazebit.persistence.impl.predicate.PredicateBuilder;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class RootPredicate extends PredicateBuilderEndedListenerImpl {

    private final AndExpression predicate;

    private final VisitorAdapter parameterRegistrationVisitor;

    public RootPredicate(ParameterManager parameterManager) {
        this.predicate = new AndExpression();
        this.parameterRegistrationVisitor = parameterManager.getParameterRegistrationVisitor();
    }

    @Override
    public void onBuilderEnded(PredicateBuilder builder) {
        super.onBuilderEnded(builder);
        BooleanExpression expression = builder.getExpression();

        // register parameter expressions
        expression.accept(parameterRegistrationVisitor);
        predicate.getChildren().add(expression);
    }

    public AndExpression getPredicate() {
        return predicate;
    }
}
