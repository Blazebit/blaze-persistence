package com.blazebit.persistence.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import com.blazebit.persistence.impl.expression.*;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.JpqlMacro;


public class MainQuery {

    private final JpqlMacroStorage macroStorage;

    final CriteriaBuilderFactoryImpl cbf;
    final ExpressionFactory expressionFactory;
    final ExpressionFactory subqueryExpressionFactory;
    final EntityManager em;
    final EntityMetamodel metamodel;
    final JpaProvider jpaProvider;
    final DbmsDialect dbmsDialect;
    final Set<String> registeredFunctions;
    final ParameterManager parameterManager;
    final CTEManager cteManager;
    final Map<String, String> properties;

    
    private MainQuery(CriteriaBuilderFactoryImpl cbf, EntityManager em, JpaProvider jpaProvider, DbmsDialect dbmsDialect, Set<String> registeredFunctions, ParameterManager parameterManager, Map<String, String> properties) {
        super();
        this.cbf = cbf;
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
        this.properties = properties;
    }
    
    public static MainQuery create(CriteriaBuilderFactoryImpl cbf, EntityManager em, String dbms, DbmsDialect dbmsDialect, Set<String> registeredFunctions) {
        if (cbf == null) {
            throw new NullPointerException("criteriaBuilderFactory");
        }
        if (em == null) {
            throw new NullPointerException("entityManager");
        }
        
        ParameterManager parameterManager = new ParameterManager();
        Map<String, String> inheritedProperties = new HashMap<String, String>(cbf.getProperties());
        return new MainQuery(cbf, em, cbf.createJpaProvider(em), dbmsDialect, registeredFunctions, parameterManager, inheritedProperties);
    }

    public final void registerMacro(String macroName, JpqlMacro jpqlMacro) {
        macroStorage.registerMacro(macroName, jpqlMacro);
    }

    public EntityManager getEm() {
        return em;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
