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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import org.datanucleus.NucleusContext;
import org.datanucleus.store.StoreManager;
import org.datanucleus.store.rdbms.RDBMSStoreManager;
import org.datanucleus.store.rdbms.sql.expression.SQLExpressionFactory;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.spi.EntityManagerIntegrator;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.JpqlFunctionGroup;

/**
 *
 * @author Christian
 */
@ServiceProvider(EntityManagerIntegrator.class)
public class DataNucleusEntityManagerIntegrator implements EntityManagerIntegrator {

    private static final Logger LOG = Logger.getLogger(DataNucleusEntityManagerIntegrator.class.getName());
	private static final Map<String, String> vendorToDbmsMapping = new HashMap<String, String>();
	
	static {
		vendorToDbmsMapping.put("h2", "h2");
		vendorToDbmsMapping.put("mysql", "mysql");
		vendorToDbmsMapping.put("db2", "db2");
		vendorToDbmsMapping.put("firebird", "firebird");
		vendorToDbmsMapping.put("postgresql", "postgresql");
		vendorToDbmsMapping.put("oracle", "oracle");
		vendorToDbmsMapping.put("sqlite", "sqlite");
		vendorToDbmsMapping.put("sqlserver", "microsoft");
		vendorToDbmsMapping.put("sybase", "sybase");
//		vendorToDbmsMapping.put("", "cubrid");
		vendorToDbmsMapping.put("hsql", "hsql");
		vendorToDbmsMapping.put("informix", "informix");
//		vendorToDbmsMapping.put("", "ingres");
//		vendorToDbmsMapping.put("", "interbase");
	}

    @Override
    public EntityManager registerFunctions(EntityManager entityManager, Map<String, JpqlFunctionGroup> dbmsFunctions) {
        RDBMSStoreManager storeMgr = (RDBMSStoreManager) entityManager.getEntityManagerFactory().unwrap(StoreManager.class);
        SQLExpressionFactory exprFactory = storeMgr.getSQLExpressionFactory();
        String dbms = vendorToDbmsMapping.get(storeMgr.getDatastoreAdapter().getVendorID());

        // If our function is registered, we don't need to register any others anymore
        if (exprFactory.isMethodRegistered(null, "COUNT_STAR")) {
        	return entityManager;
        }

        // Register compatibility functions
    	exprFactory.registerMethod(null, "COUNT_STAR", new DataNucleusJpqlFunctionAdapter(new CountStarFunction(), true), true);
        
        for (Map.Entry<String, JpqlFunctionGroup> functionEntry : dbmsFunctions.entrySet()) {
            String functionName = functionEntry.getKey().toUpperCase();
            JpqlFunctionGroup dbmsFunctionGroup = functionEntry.getValue();
            JpqlFunction function = dbmsFunctionGroup.get(dbms);
            
            if (function == null && !dbmsFunctionGroup.contains(dbms)) {
                function = dbmsFunctionGroup.get(null);
            }
            if (function == null) {
                LOG.warning("Could not register the function '" + functionName + "' because there is neither an implementation for the dbms '" + dbms + "' nor a default implementation!");
            } else {
                exprFactory.registerMethod(null, functionName, new DataNucleusJpqlFunctionAdapter(function, dbmsFunctionGroup.isAggregate()), true); 
            }
        }
        
        return entityManager;
    }

    @Override
    public Set<String> getRegisteredFunctions(EntityManager entityManager) {
        RDBMSStoreManager storeMgr = (RDBMSStoreManager) entityManager.getEntityManagerFactory().unwrap(StoreManager.class);
        SQLExpressionFactory exprFactory = storeMgr.getSQLExpressionFactory();
        String version = readMavenPropertiesVersion("META-INF/maven/org.datanucleus/datanucleus-core/pom.properties");

//        String[] versionParts = version.split("\\.");
//        int major = Integer.parseInt(versionParts[0]);
//        int minor = Integer.parseInt(versionParts[1]);
//        int fix = Integer.parseInt(versionParts[2]);
        
        Set<Object> methodKeys = fieldGet("methodNamesSupported", exprFactory, version);
        
        if (methodKeys.isEmpty()) {
            return new HashSet<String>();
        }
        
        Set<String> functions = new HashSet<String>();
        
        for (Object methodKey : methodKeys) {
        	functions.add(((String) fieldGet("methodName", methodKey, version)).toLowerCase());
        }
        
        return functions;
    }
    
    private String readMavenPropertiesVersion(String name) {
    	InputStream is = null;
    	try {
    		is = NucleusContext.class.getClassLoader().getResourceAsStream(name);
	    	Properties p = new Properties();
	    	p.load(is);
	    	return p.getProperty("version");
    	} catch (IOException e) {
    		throw new RuntimeException("Could not access the maven version properties of datanucleus!", e);
		} finally {
    		if (is != null) {
    			try {
					is.close();
				} catch (IOException e) {
					// Ignore
				}
    		}
    	}
	}

	@SuppressWarnings("unchecked")
	private <T> T fieldGet(String fieldName, Object value, String version) {
    	Exception ex;
    	
		try {
			Field field = value.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
	    	return (T) field.get(value);
		} catch (NoSuchFieldException e) {
			ex = e;
		} catch (SecurityException e) {
			ex = e;
		} catch (IllegalArgumentException e) {
			ex = e;
		} catch (IllegalAccessException e) {
			ex = e;
		}
		
		throw new RuntimeException("Could not access the supported methods to dynamically retrieve registered functions. Please report this version of datanucleus(" + version + ") so we can provide support for it!", ex);
    }
    
}
