/*
 * Copyright 2015 Blazebit.
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
package com.blazebit.persistence.impl.datanucleus.function;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.spi.EntityManagerIntegrator;
import com.blazebit.persistence.spi.JpqlFunction;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import org.datanucleus.NucleusContext;
import org.datanucleus.plugin.Bundle;
import org.datanucleus.plugin.ConfigurationElement;
import org.datanucleus.plugin.Extension;
import org.datanucleus.plugin.ExtensionPoint;

/**
 *
 * @author Christian
 */
@ServiceProvider(EntityManagerIntegrator.class)
public class DataNucleusEntityManagerIntegrator implements EntityManagerIntegrator {

    @Override
    public EntityManager registerFunctions(EntityManager entityManager, Map<String, Map<String, JpqlFunction>> dbmsFunctions) {
        NucleusContext context = entityManager.unwrap(NucleusContext.class);
        ExtensionPoint point = context.getPluginManager().getExtensionPoint("org.datanucleus.store.rdbms.sql_method");
        Bundle bundle = new Bundle("blaze-persistence", "blaze-persistence", "blazebit", "1.0", null);
        Extension extension = new Extension(point.getUniqueId(), bundle);
        
        // TODO: Implement
//        extension.addConfigurationElement(newMethod(extension, name, dbms, clazz));
        
//        <sql-method method="PAGE_POSITION" datastore="mysql" evaluator="com.blazebit.persistence.impl.datanucleus.function.pageposition.MySQLPagePositionSQLMethod"/>
//        <sql-method method="PAGE_POSITION" datastore="oracle" evaluator="com.blazebit.persistence.impl.datanucleus.function.pageposition.OraclePagePositionSQLMethod"/>
//        <sql-method method="PAGE_POSITION" datastore="sybase" evaluator="com.blazebit.persistence.impl.datanucleus.function.pageposition.TransactSQLPagePositionSQLMethod"/>
//        <sql-method method="PAGE_POSITION" datastore="microsoft" evaluator="com.blazebit.persistence.impl.datanucleus.function.pageposition.TransactSQLPagePositionSQLMethod"/>
//        <sql-method method="PAGE_POSITION" evaluator="com.blazebit.persistence.impl.datanucleus.function.pageposition.PagePositionSQLMethod"/>
        
//        point.addExtension(extension);
        return entityManager;
    }

    @Override
    public Set<String> getRegisteredFunctions(EntityManager entityManager) {
        // TODO: Implement
        return new HashSet<String>();
    }

    private ConfigurationElement newMethod(Extension extension, String name, String datastore, Class<?> clazz) {
        ConfigurationElement elem = new ConfigurationElement(extension, "sql-method", null);
        elem.putAttribute("datastore", datastore);
        elem.putAttribute("class", clazz.getName());
        elem.putAttribute("method", name);
        return elem;
    }
    
}
