package com.blazebit.persistence.impl;

import java.util.Set;

import javax.persistence.EntityManager;

import com.blazebit.persistence.impl.jpaprovider.JpaProvider;
import com.blazebit.persistence.impl.jpaprovider.JpaProviders;
import com.blazebit.persistence.spi.DbmsDialect;


public class MainQuery {

    final CriteriaBuilderFactoryImpl cbf;
    final EntityManager em;
    final JpaProvider jpaProvider;
    final DbmsDialect dbmsDialect;
    final Set<String> registeredFunctions;
    final ParameterManager parameterManager;
    final CTEManager cteManager;
    
    private MainQuery(CriteriaBuilderFactoryImpl cbf, EntityManager em, JpaProvider jpaProvider, DbmsDialect dbmsDialect, Set<String> registeredFunctions, ParameterManager parameterManager) {
        super();
        this.cbf = cbf;
        this.em = em;
        this.jpaProvider = jpaProvider;
        this.dbmsDialect = dbmsDialect;
        this.registeredFunctions = registeredFunctions;
        this.parameterManager = parameterManager;
        this.cteManager = new CTEManager(this);
    }
    
    public static MainQuery create(CriteriaBuilderFactoryImpl cbf, EntityManager em, DbmsDialect dbmsDialect, Set<String> registeredFunctions) {
        if (cbf == null) {
            throw new NullPointerException("criteriaBuilderFactory");
        }
        if (em == null) {
            throw new NullPointerException("entityManager");
        }
        
        ParameterManager parameterManager = new ParameterManager();
        return new MainQuery(cbf, em, JpaProviders.resolveJpaProvider(em), dbmsDialect, registeredFunctions, parameterManager);
    }
}
