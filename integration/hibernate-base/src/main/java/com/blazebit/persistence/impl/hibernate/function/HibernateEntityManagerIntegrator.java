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
package com.blazebit.persistence.impl.hibernate.function;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.dialect.CUBRIDDialect;
import org.hibernate.dialect.DB2Dialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.InformixDialect;
import org.hibernate.dialect.IngresDialect;
import org.hibernate.dialect.InterbaseDialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.Oracle8iDialect;
import org.hibernate.dialect.Oracle9Dialect;
import org.hibernate.dialect.PostgreSQL81Dialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.dialect.SybaseDialect;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.dialect.function.SQLFunctionRegistry;
import org.hibernate.engine.spi.SessionFactoryImplementor;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.spi.EntityManagerIntegrator;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.JpqlFunctionGroup;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
@SuppressWarnings("deprecation")
@ServiceProvider(EntityManagerIntegrator.class)
public class HibernateEntityManagerIntegrator implements EntityManagerIntegrator {
    
    private static final Logger LOG = Logger.getLogger(EntityManagerIntegrator.class.getName());
    
    @Override
	public String getDbms(EntityManager entityManager) {
        Session s = entityManager.unwrap(Session.class);
        Dialect dialect = getDialect(s);
        return getDbmsName(dialect);
    }
    
    private String getDbmsName(Dialect dialect) {
        if (dialect instanceof MySQLDialect) {
        	return "mysql";
        } else if (dialect instanceof DB2Dialect) {
        	return "db2";
        } else if (dialect instanceof PostgreSQL81Dialect) {
        	return "postgresql";
        } else if (dialect instanceof Oracle8iDialect || dialect instanceof Oracle9Dialect) {
        	return "oracle";
        } else if (dialect instanceof SQLServerDialect) {
        	return "microsoft";
        } else if (dialect instanceof SybaseDialect) {
        	return "sybase";
        } else if (dialect instanceof H2Dialect) {
        	return "h2";
        } else if (dialect instanceof CUBRIDDialect) {
        	return "cubrid";
        } else if (dialect instanceof HSQLDialect) {
        	return "hsql";
        } else if (dialect instanceof InformixDialect) {
        	return "informix";
        } else if (dialect instanceof IngresDialect) {
        	return "ingres";
        } else if (dialect instanceof InterbaseDialect) {
        	return "interbase";
        } else {
            return null;
        }
	}
    
    @Override
    public EntityManager registerFunctions(EntityManager em, Map<String, JpqlFunctionGroup> dbmsFunctions) {
        Session s = em.unwrap(Session.class);
        Map<String, SQLFunction> functions = getFunctions(s);
        Dialect dialect = getDialect(s);
        String dbms = getDbmsName(dialect);
        
        for (Map.Entry<String, JpqlFunctionGroup> functionEntry : dbmsFunctions.entrySet()) {
            String functionName = functionEntry.getKey();
            JpqlFunctionGroup dbmsFunctionMap = functionEntry.getValue();
            JpqlFunction function = dbmsFunctionMap.get(dbms);
            
            if (function == null && !dbmsFunctionMap.contains(dbms)) {
                function = dbmsFunctionMap.get(null);
            }
            if (function == null) {
                LOG.warning("Could not register the function '" + functionName + "' because there is neither an implementation for the dbms '" + dbms + "' nor a default implementation!");
            } else {
                functions.put(functionName, new HibernateJpqlFunctionAdapter(function));
            }
        }
        
        return em;
    }

    @Override
    public Set<String> getRegisteredFunctions(EntityManager em) {
        Session s = em.unwrap(Session.class);
        return getFunctions(s).keySet();
    }
    
    @SuppressWarnings("unchecked")
	private Map<String, SQLFunction> getFunctions(Session s) {
        String version = s.getClass().getPackage().getImplementationVersion();

        String[] versionParts = version.split("\\.");
        int major = Integer.parseInt(versionParts[0]);
        int minor = Integer.parseInt(versionParts[1]);
        int fix = Integer.parseInt(versionParts[2]);
        String type = versionParts[3];

        if (major < 5 || (major == 5 && minor == 0 && fix == 0 && "Beta1".equals(type))) {
	        // Implementation detail: Hibernate uses a mutable map, so we can do this
	        return getDialect(s).getFunctions();
        } else {
            SessionFactoryImplementor sf = (SessionFactoryImplementor) s.getSessionFactory();
            SQLFunctionRegistry registry = sf.getSqlFunctionRegistry();
            Exception ex;
            
            // We have to retrieve the functionMap the old fashioned way via reflection :(
            try {
            	Field f = SQLFunctionRegistry.class.getDeclaredField("functionMap");
            	f.setAccessible(true);
            	return (Map<String, SQLFunction>) f.get(registry);
            } catch (NoSuchFieldException e) {
    			ex = e;
            } catch (IllegalArgumentException e) {
            	// This can never happen
				ex = e;
			} catch (IllegalAccessException e) {
				ex = e;
			}
            
			throw new RuntimeException("Could not access the function map to dynamically register functions. Please report this version of hibernate(" + version + ") so we can provide support for it!", ex);
        }
    }
    
    private Dialect getDialect(Session s) {
        SessionFactoryImplementor sf = (SessionFactoryImplementor) s.getSessionFactory();
        return sf.getDialect();
    }
}
