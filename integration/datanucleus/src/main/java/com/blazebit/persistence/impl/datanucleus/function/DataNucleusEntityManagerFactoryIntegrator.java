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
import javax.persistence.EntityManagerFactory;

import com.blazebit.persistence.impl.datanucleus.DataNucleusJpaProvider;
import com.blazebit.persistence.spi.EntityManagerFactoryIntegrator;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.spi.JpaProviderFactory;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.JpqlFunctionGroup;
import org.datanucleus.NucleusContext;
import org.datanucleus.store.StoreManager;
import org.datanucleus.store.rdbms.RDBMSStoreManager;
import org.datanucleus.store.rdbms.sql.expression.SQLExpressionFactory;
import org.datanucleus.store.rdbms.sql.method.SQLMethod;

import com.blazebit.apt.service.ServiceProvider;

/**
 *
 * @author Christian
 */
@ServiceProvider(EntityManagerFactoryIntegrator.class)
public class DataNucleusEntityManagerFactoryIntegrator implements EntityManagerFactoryIntegrator {

    private static final Logger LOG = Logger.getLogger(DataNucleusEntityManagerFactoryIntegrator.class.getName());
    private static final Map<String, String> VENDOR_TO_DBMS_MAPPING = new HashMap<String, String>();
    private static final String VERSION;
    private static final int MAJOR;
    private static final int MINOR;
    private static final int FIX;
    
    static {
        VENDOR_TO_DBMS_MAPPING.put("h2", "h2");
        VENDOR_TO_DBMS_MAPPING.put("mysql", "mysql");
        VENDOR_TO_DBMS_MAPPING.put("db2", "db2");
        VENDOR_TO_DBMS_MAPPING.put("firebird", "firebird");
        VENDOR_TO_DBMS_MAPPING.put("postgresql", "postgresql");
        VENDOR_TO_DBMS_MAPPING.put("oracle", "oracle");
        VENDOR_TO_DBMS_MAPPING.put("sqlite", "sqlite");
        VENDOR_TO_DBMS_MAPPING.put("sqlserver", "microsoft");
        VENDOR_TO_DBMS_MAPPING.put("sybase", "sybase");
//        VENDOR_TO_DBMS_MAPPING.put("", "cubrid");
        VENDOR_TO_DBMS_MAPPING.put("hsql", "hsql");
        VENDOR_TO_DBMS_MAPPING.put("informix", "informix");
//        VENDOR_TO_DBMS_MAPPING.put("", "ingres");
//        VENDOR_TO_DBMS_MAPPING.put("", "interbase");

        VERSION = readMavenPropertiesVersion("META-INF/maven/org.datanucleus/datanucleus-core/pom.properties");

        String[] versionParts = VERSION.split("[\\.-]");
        MAJOR = Integer.parseInt(versionParts[0]);
        MINOR = Integer.parseInt(versionParts[1]);
        FIX = Integer.parseInt(versionParts[2]);
    }

    @Override
    public String getDbms(EntityManagerFactory entityManagerFactory) {
        RDBMSStoreManager storeMgr = (RDBMSStoreManager) entityManagerFactory.unwrap(StoreManager.class);
        return VENDOR_TO_DBMS_MAPPING.get(storeMgr.getDatastoreAdapter().getVendorID());
    }

    @Override
    public JpaProviderFactory getJpaProviderFactory(EntityManagerFactory entityManagerFactory) {
        return new JpaProviderFactory() {
            @Override
            public JpaProvider createJpaProvider(EntityManager em) {
                return new DataNucleusJpaProvider(em, MAJOR, MINOR, FIX);
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public EntityManagerFactory registerFunctions(EntityManagerFactory entityManagerFactory, Map<String, JpqlFunctionGroup> dbmsFunctions) {
        RDBMSStoreManager storeMgr = (RDBMSStoreManager) entityManagerFactory.unwrap(StoreManager.class);
        SQLExpressionFactory exprFactory = storeMgr.getSQLExpressionFactory();
        String dbms = VENDOR_TO_DBMS_MAPPING.get(storeMgr.getDatastoreAdapter().getVendorID());

        // Register compatibility functions
        if (!exprFactory.isMethodRegistered(null, "COUNT_STAR")) {
            exprFactory.registerMethod(null, "COUNT_STAR", new DataNucleusJpqlFunctionAdapter(new CountStarFunction(), true), true);
        }

        // DataNucleus4 uses a month function that is 0 based which conflicts with ANSI EXTRACT(MONTH)
        if (MAJOR < 5 && !(exprFactory.getMethod("java.util.Date", "getMonth", null) instanceof DataNucleusJpqlFunctionAdapter)) {
            LOG.warning("Overriding DataNucleus native 'MONTH' function to return months 1-based like ANSI EXTRACT instead of 0-based!");
            
            JpqlFunctionGroup dbmsFunctionGroup = dbmsFunctions.get("month");
            JpqlFunction function = dbmsFunctionGroup.get(dbms);
            
            if (function == null && !dbmsFunctionGroup.contains(dbms)) {
                function = dbmsFunctionGroup.get(null);
            }
            
            SQLMethod method = new DataNucleusJpqlFunctionAdapter(function, dbmsFunctionGroup.isAggregate());
            String version = readMavenPropertiesVersion("META-INF/maven/org.datanucleus/datanucleus-core/pom.properties");
            Set<Object> methodKeys = fieldGet("methodNamesSupported", exprFactory, version);

            for (Object methodKey : methodKeys) {
                if ("getMonth".equals((String) fieldGet("methodName", methodKey, version)) && "java.util.Date".equals((String) fieldGet("clsName", methodKey, version))) {
                    ((Map<Object, Object>) fieldGet("methodByClassMethodName", exprFactory, version)).put(methodKey, method);
                }
            }
        }
        
        for (Map.Entry<String, JpqlFunctionGroup> functionEntry : dbmsFunctions.entrySet()) {
            String functionName = functionEntry.getKey().toUpperCase();
            JpqlFunctionGroup dbmsFunctionGroup = functionEntry.getValue();
            JpqlFunction function = dbmsFunctionGroup.get(dbms);
            
            if (function == null && !dbmsFunctionGroup.contains(dbms)) {
                function = dbmsFunctionGroup.get(null);
            }
            if (function == null) {
                LOG.warning("Could not register the function '" + functionName + "' because there is neither an implementation for the dbms '" + dbms + "' nor a default implementation!");
            } else if (!exprFactory.isMethodRegistered(null, functionName)) {
                exprFactory.registerMethod(null, functionName, new DataNucleusJpqlFunctionAdapter(function, dbmsFunctionGroup.isAggregate()), true);
            }
        }
        
        return entityManagerFactory;
    }

    @Override
    public Set<String> getRegisteredFunctions(EntityManagerFactory entityManagerFactory) {
        RDBMSStoreManager storeMgr = (RDBMSStoreManager) entityManagerFactory.unwrap(StoreManager.class);
        SQLExpressionFactory exprFactory = storeMgr.getSQLExpressionFactory();
        
        Set<Object> methodKeys = fieldGet("methodNamesSupported", exprFactory, VERSION);
        
        if (methodKeys.isEmpty()) {
            return new HashSet<String>();
        }
        
        Set<String> functions = new HashSet<String>();
        
        for (Object methodKey : methodKeys) {
            functions.add(((String) fieldGet("methodName", methodKey, VERSION)).toLowerCase());
        }
        
        return functions;
    }
    
    private static String readMavenPropertiesVersion(String name) {
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
        Field field = null;
        boolean madeAccessible = false;
        
        try {
            field = value.getClass().getDeclaredField(fieldName);
            madeAccessible = !field.isAccessible();
            
            if (madeAccessible) {
                field.setAccessible(true);
            }
            
            return (T) field.get(value);
        } catch (NoSuchFieldException e) {
            ex = e;
        } catch (SecurityException e) {
            ex = e;
        } catch (IllegalArgumentException e) {
            ex = e;
        } catch (IllegalAccessException e) {
            ex = e;
        } finally {
            if (madeAccessible) {
                field.setAccessible(false);
            }
        }
        
        throw new RuntimeException("Could not access the supported methods to dynamically retrieve registered functions. Please report this version of datanucleus(" + version + ") so we can provide support for it!", ex);
    }
    
}
