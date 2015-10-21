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
import com.blazebit.persistence.impl.ParameterRegistrationVisitor;
import com.blazebit.persistence.impl.expression.VisitorAdapter;
import com.blazebit.persistence.impl.predicate.AndPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.predicate.PredicateBuilder;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class RootPredicate extends PredicateBuilderEndedListenerImpl {

    private final AndPredicate predicate;

    private final VisitorAdapter parameterRegistrationVisitor;

    public RootPredicate(ParameterManager parameterManager) {
        this.predicate = new AndPredicate();
        this.parameterRegistrationVisitor = new ParameterRegistrationVisitor(parameterManager);
    }

    @Override
    public void onBuilderEnded(PredicateBuilder builder) {
        super.onBuilderEnded(builder);
        Predicate pred = builder.getPredicate();

        // register parameter expressions
        pred.accept(parameterRegistrationVisitor);
        predicate.getChildren().add(pred);
    }

    public AndPredicate getPredicate() {
        return predicate;
    }
}
