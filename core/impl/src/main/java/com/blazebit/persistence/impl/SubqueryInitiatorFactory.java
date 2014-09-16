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

import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.AliasManager;
import com.blazebit.persistence.impl.CriteriaBuilderFactoryImpl;
import com.blazebit.persistence.impl.JoinManager;
import com.blazebit.persistence.impl.ParameterManager;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import javax.persistence.EntityManager;

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public class SubqueryInitiatorFactory {

    private final CriteriaBuilderFactoryImpl cbf;
    private final EntityManager em;
    private final ParameterManager parameterManager;
    private final AliasManager aliasManager;
    private final ExpressionFactory expressionFactory;
    private final JoinManager parentJoinManager;

    public SubqueryInitiatorFactory(CriteriaBuilderFactoryImpl cbf, EntityManager em, ParameterManager parameterManager, AliasManager aliasManager, JoinManager parentJoinManager, ExpressionFactory expressionFactory) {
        this.cbf = cbf;
        this.em = em;
        this.parameterManager = parameterManager;
        this.aliasManager = aliasManager;
        this.expressionFactory = expressionFactory;
        this.parentJoinManager = parentJoinManager;
    }

    public <T> SubqueryInitiator<T> createSubqueryInitiator(T result, SubqueryBuilderListener<T> listener) {
        return new SubqueryInitiatorImpl<T>(cbf, em, result, parameterManager, aliasManager, parentJoinManager, listener, expressionFactory);
    }
}
