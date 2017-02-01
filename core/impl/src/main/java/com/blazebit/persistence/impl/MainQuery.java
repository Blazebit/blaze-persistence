/*
 * Copyright 2014 - 2017 Blazebit.
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

import java.util.Set;

import javax.persistence.EntityManager;

import com.blazebit.persistence.impl.expression.AbstractCachingExpressionFactory;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.JpqlMacro;


public class MainQuery {

    final CriteriaBuilderFactoryImpl cbf;
    final ExpressionFactory expressionFactory;
    final ExpressionFactory subqueryExpressionFactory;
    final EntityManager em;
    final EntityMetamodelImpl metamodel;
    final JpaProvider jpaProvider;
    final DbmsDialect dbmsDialect;
    final Set<String> registeredFunctions;
    final ParameterManager parameterManager;
    final CTEManager cteManager;

    private final JpqlMacroStorage macroStorage;
    private QueryConfiguration queryConfiguration;

    private MainQuery(CriteriaBuilderFactoryImpl cbf, EntityManager em, JpaProvider jpaProvider, DbmsDialect dbmsDialect, Set<String> registeredFunctions, ParameterManager parameterManager) {
        super();
        this.cbf = cbf;
        this.queryConfiguration = cbf.getQueryConfiguration();
        // NOTE: we unwrap the ExpressionFactory as it is a JpqlMacroAwareExpressionFactory and we need the caching one
        this.macroStorage = new JpqlMacroStorage(cbf.getExpressionFactory().unwrap(AbstractCachingExpressionFactory.class), cbf.getMacroConfiguration());
        this.expressionFactory = new JpqlMacroAwareExpressionFactory(cbf.getExpressionFactory().unwrap(AbstractCachingExpressionFactory.class), macroStorage);
        this.subqueryExpressionFactory = new JpqlMacroAwareExpressionFactory(cbf.getSubqueryExpressionFactory().unwrap(AbstractCachingExpressionFactory.class), macroStorage);
        this.em = em;
        this.metamodel = cbf.getMetamodel();
        this.jpaProvider = jpaProvider;
        this.dbmsDialect = dbmsDialect;
        this.registeredFunctions = registeredFunctions;
        this.parameterManager = parameterManager;
        this.cteManager = new CTEManager(this);
    }
    
    public static MainQuery create(CriteriaBuilderFactoryImpl cbf, EntityManager em, String dbms, DbmsDialect dbmsDialect, Set<String> registeredFunctions) {
        if (cbf == null) {
            throw new NullPointerException("criteriaBuilderFactory");
        }
        if (em == null) {
            throw new NullPointerException("entityManager");
        }
        
        ParameterManager parameterManager = new ParameterManager();
        return new MainQuery(cbf, em, cbf.createJpaProvider(em), dbmsDialect, registeredFunctions, parameterManager);
    }

    public final void registerMacro(String macroName, JpqlMacro jpqlMacro) {
        macroStorage.registerMacro(macroName, jpqlMacro);
    }

    public EntityManager getEm() {
        return em;
    }

    public EntityMetamodelImpl getMetamodel() {
        return metamodel;
    }

    public QueryConfiguration getMutableQueryConfiguration() {
        if (!(queryConfiguration instanceof MutableQueryConfiguration)) {
            queryConfiguration = new MutableQueryConfiguration(queryConfiguration);
        }

        return queryConfiguration;
    }

    public QueryConfiguration getQueryConfiguration() {
        return queryConfiguration;
    }
}
