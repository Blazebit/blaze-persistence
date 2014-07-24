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

import com.blazebit.persistence.SubqueryBuilder;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;

/**
 *
 * @author ccbem
 */
public class SubqueryBuilderImpl<U> extends AbstractBaseQueryBuilder<Tuple, SubqueryBuilder< U>> implements SubqueryBuilder<U> /* TODO: is Tuple OK?*/ {
    private final U result;
    private final SubqueryBuilderListener listener;
    
    //TODO: prevent duplication of aliases from the main query
    public SubqueryBuilderImpl(CriteriaBuilderFactoryImpl cbf, EntityManager em, Class<?> fromClazz, String alias, U result, ParameterManager parameterManager, SubqueryBuilderListener listener) {
        super(cbf, em, Tuple.class, fromClazz, alias, parameterManager);
        this.result = result;
        this.listener = listener;
    }
    
    @Override
    public U end() {
        listener.onBuilderEnded(this);
        return result;
    }

    @Override
    public SubqueryBuilder<U> select(String expression) {
        return (SubqueryBuilder<U>) super.select(expression);
    }

    @Override
    public SubqueryBuilder<U> select(String expression, String alias) {
        return (SubqueryBuilder<U>) super.select(expression, alias);
    }
}
