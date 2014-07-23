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
    
    //TODO: prevent duplication of aliases from the main query
    public SubqueryBuilderImpl(EntityManager em, String alias, U result, ParameterManager parameterManager) {
        super(em, Tuple.class, alias, parameterManager);
        this.result = result;
    }
    
    @Override
    public U end() {
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
