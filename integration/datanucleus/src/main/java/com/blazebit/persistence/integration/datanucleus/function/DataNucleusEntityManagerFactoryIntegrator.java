/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.integration.datanucleus.function;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.integration.datanucleus.DataNucleusJpaProvider;
import com.blazebit.persistence.integration.jpa.function.CountStarFunction;
import com.blazebit.persistence.spi.EntityManagerFactoryIntegrator;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.spi.JpaProviderFactory;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.JpqlFunctionGroup;
import org.datanucleus.NucleusContext;
import org.datanucleus.store.StoreManager;
import org.datanucleus.store.rdbms.RDBMSStoreManager;
import org.datanucleus.store.rdbms.identifier.DatastoreIdentifier;
import org.datanucleus.store.rdbms.query.QueryGenerator;
import org.datanucleus.store.rdbms.sql.SQLStatement;
import org.datanucleus.store.rdbms.sql.expression.SQLExpressionFactory;
import org.datanucleus.store.rdbms.sql.method.SQLMethod;
import org.datanucleus.store.rdbms.table.Table;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 *
 * @since 1.2.0
 */
@ServiceProvider(EntityManagerFactoryIntegrator.class)
public class DataNucleusEntityManagerFactoryIntegrator implements EntityManagerFactoryIntegrator {

    public static final String VERSION;
    public static final int MAJOR;
    public static final int MINOR;
    public static final int FIX;

    private static final Logger LOG = Logger.getLogger(DataNucleusEntityManagerFactoryIntegrator.class.getName());
    private static final Map<String, String> VENDOR_TO_DBMS_MAPPING = new HashMap<String, String>();
    
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
    public JpaProviderFactory getJpaProviderFactory(final EntityManagerFactory entityManagerFactory) {
        return new JpaProviderFactory() {
            @Override
            public JpaProvider createJpaProvider(EntityManager em) {
                PersistenceUnitUtil persistenceUnitUtil = entityManagerFactory == null ? null : entityManagerFactory.getPersistenceUnitUtil();
                if (persistenceUnitUtil == null && em != null) {
                    persistenceUnitUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
                }
                return new DataNucleusJpaProvider(persistenceUnitUtil, MAJOR, MINOR, FIX);
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
        if (!exprFactory.isMethodRegistered(null, CountStarFunction.FUNCTION_NAME)) {
            exprFactory.registerMethod(null, CountStarFunction.FUNCTION_NAME, new DataNucleusJpqlFunctionAdapter(new CountStarFunction(), true), true);
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
            Set<Object> methodKeys = fieldGet("methodNamesSupported", exprFactory, VERSION);

            for (Object methodKey : methodKeys) {
                if ("getMonth".equals((String) fieldGet("methodName", methodKey, VERSION)) && "java.util.Date".equals((String) fieldGet("clsName", methodKey, VERSION))) {
                    ((Map<Object, Object>) fieldGet("methodByClassMethodName", exprFactory, VERSION)).put(methodKey, method);
                }
            }
        }

        // construct map for checking existence of functions in a case-insensitive way
        Map<String, JpqlFunction> registeredFunctions = getRegisteredFunctions(entityManagerFactory);
        Map<String, String> caseInsensitiveRegisteredFunctions = new HashMap<>(registeredFunctions.size());
        for (String registeredFunctionName : registeredFunctions.keySet()) {
            caseInsensitiveRegisteredFunctions.put(registeredFunctionName.toLowerCase(), registeredFunctionName);
        }

        for (Map.Entry<String, JpqlFunctionGroup> functionEntry : dbmsFunctions.entrySet()) {
            String functionName = functionEntry.getKey();
            JpqlFunctionGroup dbmsFunctionGroup = functionEntry.getValue();
            JpqlFunction function = dbmsFunctionGroup.get(dbms);
            
            if (function == null && !dbmsFunctionGroup.contains(dbms)) {
                function = dbmsFunctionGroup.get(null);
            }
            if (function == null) {
                LOG.warning("Could not register the function '" + functionName + "' because there is neither an implementation for the dbms '" + dbms + "' nor a default implementation!");
            } else if (!caseInsensitiveRegisteredFunctions.containsKey(functionName.toLowerCase())) {
                exprFactory.registerMethod(null, functionName, new DataNucleusJpqlFunctionAdapter(function, dbmsFunctionGroup.isAggregate()), true);
            }
        }
        
        return entityManagerFactory;
    }

    @Override
    public Map<String, JpqlFunction> getRegisteredFunctions(EntityManagerFactory entityManagerFactory) {
        NucleusContext context = entityManagerFactory.unwrap(NucleusContext.class);
        RDBMSStoreManager storeMgr = (RDBMSStoreManager) entityManagerFactory.unwrap(StoreManager.class);
        String storeName = storeMgr.getDatastoreAdapter().getVendorID();
        SQLExpressionFactory exprFactory = storeMgr.getSQLExpressionFactory();
        
        Set<Object> methodKeys = fieldGet("methodNamesSupported", exprFactory, VERSION);
        
        if (methodKeys.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, JpqlFunction> functions = new HashMap<>();

        // We need to construct a statement object and how this is done changed between 4 and 5 so we have to do a little reflection hack
        // We need this because the function methods retrieve the expression factory through it
        Class<?>[] parameterTypes = {RDBMSStoreManager.class, Table.class, DatastoreIdentifier.class, String.class};
        SQLStatement stmt;

        try {
            Constructor c = Class.forName("org.datanucleus.store.rdbms.sql.SelectStatement").getConstructor(parameterTypes);
            stmt = (SQLStatement) c.newInstance(storeMgr, null, null, null);
        } catch (Exception e) {
            try {
                Constructor c = Class.forName("org.datanucleus.store.rdbms.sql.SQLStatement").getConstructor(parameterTypes);
                stmt = (SQLStatement) c.newInstance(storeMgr, null, null, null);
            } catch (Exception e2) {
                throw new RuntimeException("Could not access the required methods to dynamically retrieve registered functions. Please report this version of datanucleus(" + VERSION + ") so we can provide support for it!", e2);
            }
        }

        // Well apparently expressions get their class loader resolver by asking the statement they are part of
        // which in turn asks the query generator that is responsible for it
        // So this is the most non-hackish way to get this to work...
        QueryGenerator noopGenerator = (QueryGenerator) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{ QueryGenerator.class }, new QueryGeneratorInvocationHandler(context));
        stmt.setQueryGenerator(noopGenerator);
        
        for (Object methodKey : methodKeys) {
            String className = fieldGet("clsName", methodKey, VERSION);
            String datastoreName = fieldGet("datastoreName", methodKey, VERSION);
            String name = fieldGet("methodName", methodKey, VERSION);
            if (className.isEmpty()
                    && name.indexOf('.') == -1
                    && ("ALL".equals(datastoreName) || storeName.equals(datastoreName))) {
                // Only consider normal functions
                SQLMethod method = exprFactory.getMethod(null, name, Collections.emptyList());
                if (method instanceof DataNucleusJpqlFunctionAdapter) {
                    functions.put(name, ((DataNucleusJpqlFunctionAdapter) method).unwrap());
                } else {
                    functions.put(name, new JpqlFunctionSQLMethod(stmt, method));
                }
            }
        }

        // The length function is the single exception to all functions that is based on a class
        SQLMethod method = exprFactory.getMethod("java.lang.String", "length", Collections.emptyList());
        functions.put("length", new JpqlFunctionInstanceSQLMethod(stmt, method));
        
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
        
        throw new RuntimeException("Could not access the required methods to dynamically retrieve registered functions. Please report this version of datanucleus(" + version + ") so we can provide support for it!", ex);
    }
    
}
