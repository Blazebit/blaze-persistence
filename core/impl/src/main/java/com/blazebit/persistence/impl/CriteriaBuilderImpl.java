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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.ObjectBuilder;
import com.blazebit.persistence.SelectObjectBuilder;
import com.blazebit.persistence.impl.expression.ExpressionFactoryImpl;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;

/**
 *
 * @param <T> The query result type
 * @author Christian Beikov
 */
public class CriteriaBuilderImpl<T> extends AbstractQueryBuilder<T, CriteriaBuilder<T>> implements CriteriaBuilder<T> {

    public CriteriaBuilderImpl(CriteriaBuilderFactoryImpl cbf, EntityManager em, Class<T> clazz, String alias, ExpressionFactoryImpl expressionFactory) {
        super(cbf, em, clazz, alias, expressionFactory);
    }

    @Override
    public <Y> SelectObjectBuilder<CriteriaBuilder<Y>> selectNew(Class<Y> clazz) {
        return (SelectObjectBuilder<CriteriaBuilder<Y>>) super.selectNew(clazz);
    }
    
    @Override
    public <Y> CriteriaBuilder<Y> selectNew(ObjectBuilder<Y> builder) {
        return (CriteriaBuilder<Y>) super.selectNew(builder);
    }

    @Override
    public CriteriaBuilder<Tuple> select(String expression) {
        return (CriteriaBuilder<Tuple>) super.select(expression);
    }

    @Override
    public CriteriaBuilder<Tuple> select(String expression, String alias) {
        return (CriteriaBuilder<Tuple>) super.select(expression, alias);
    }

}
