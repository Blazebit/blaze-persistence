package com.blazebit.persistence.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.MacroConfiguration;
import com.blazebit.persistence.impl.expression.SimpleCachingExpressionFactory;
import com.blazebit.persistence.impl.expression.SubqueryExpressionFactory;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.JpqlMacro;


public class MainQuery {

    final CriteriaBuilderFactoryImpl cbf;
    final JpqlMacroStorage macroStorage;
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
        this.macroStorage = new JpqlMacroStorage(cbf.getExpressionFactory(), cbf.getMacroConfiguration());
        this.expressionFactory = new JpqlMacroAwareExpressionFactory(cbf.getExpressionFactory(), macroStorage);
        this.subqueryExpressionFactory = new JpqlMacroAwareExpressionFactory(cbf.getSubqueryExpressionFactory(), macroStorage);
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

}
