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
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.ExpressionFactoryImpl;
import com.blazebit.persistence.spi.QueryTransformer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.persistence.EntityManager;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class CriteriaBuilderFactoryImpl implements CriteriaBuilderFactory {

    private final List<QueryTransformer> queryTransformers;
    private final ExpressionFactory expressionFactory;
    private final Properties properties;

    public CriteriaBuilderFactoryImpl(CriteriaBuilderConfigurationImpl config) {
        this.queryTransformers = new ArrayList<QueryTransformer>(config.getQueryTransformers());
        this.expressionFactory = new ExpressionFactoryImpl();
        this.properties = new Properties(config.getProperties());
    }

    public List<QueryTransformer> getQueryTransformers() {
        return queryTransformers;
    }

    public ExpressionFactory getExpressionFactory() {
        return expressionFactory;
    }

    public Properties getProperties() {
        return properties;
    }

    @Override
    public <T> CriteriaBuilder<T> from(EntityManager em, Class<T> clazz) {
        return new CriteriaBuilderImpl<T>(this, em, clazz, clazz.getSimpleName().toLowerCase());
    }

    @Override
    public <T> CriteriaBuilder<T> from(EntityManager em, Class<T> clazz, String alias) {
        return new CriteriaBuilderImpl<T>(this, em, clazz, alias);
    }

}
