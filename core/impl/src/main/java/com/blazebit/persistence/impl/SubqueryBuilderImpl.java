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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;

import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.Subquery;
import com.blazebit.persistence.spi.DbmsDialect;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public class SubqueryBuilderImpl<T> extends AbstractCommonQueryBuilder<Tuple, SubqueryBuilder<T>> implements SubqueryBuilder<T>, Subquery {

    private final T result;
    private final SubqueryBuilderListener<T> listener;

    public SubqueryBuilderImpl(CriteriaBuilderFactoryImpl cbf, EntityManager em, DbmsDialect dbmsDialect, Class<?> fromClazz, String alias, T result, ParameterManager parameterManager, AliasManager aliasManager, JoinManager parentJoinManager, SubqueryBuilderListener<T> listener, ExpressionFactory expressionFactory, Set<String> registeredFunctions) {
        super(cbf, em, dbmsDialect, Tuple.class, alias, parameterManager, aliasManager, parentJoinManager, expressionFactory, registeredFunctions);
        this.result = result;
        this.listener = listener;
    }

    @Override
    public List<Expression> getSelectExpressions() {
        List<Expression> selectExpressions = new ArrayList<Expression>(selectManager.getSelectInfos().size());

        for (SelectInfo info : selectManager.getSelectInfos()) {
            selectExpressions.add(info.getExpression());
        }

        return selectExpressions;
    }

    @Override
    public T end() {
        listener.onBuilderEnded(this);
        return result;
    }

    public T getResult() {
        return result;
    }
}
