/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.integration.hibernate.base.function;

import com.blazebit.persistence.spi.EntityManagerFactoryIntegrator;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.JpqlFunctionGroup;
import org.hibernate.Session;
import org.hibernate.dialect.CUBRIDDialect;
import org.hibernate.dialect.DB2Dialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.InformixDialect;
import org.hibernate.dialect.IngresDialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.OracleDialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.dialect.SybaseDialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.query.sqm.function.SqmFunctionDescriptor;
import org.hibernate.query.sqm.function.SqmFunctionRegistry;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
@SuppressWarnings("deprecation")
public abstract class AbstractHibernateEntityManagerFactoryIntegrator implements EntityManagerFactoryIntegrator {

    private static final Logger LOG = Logger.getLogger(EntityManagerFactoryIntegrator.class.getName());

    protected String getDbmsName(EntityManagerFactory emf, EntityManager em, Dialect dialect) {
        if (dialect instanceof MySQLDialect) {
            try {
                boolean close = em == null;
                EntityManager entityManager = em == null ? emf.createEntityManager() : em;
                try {
                    Connection connection = entityManager.unwrap(SessionImplementor.class).connection();
                    if (connection.getMetaData().getDatabaseMajorVersion() > 7) {
                        return "mysql8";
                    } else {
                        return "mysql";
                    }
                } finally {
                    if (close) {
                        entityManager.close();
                    }
                }
            } catch (Exception ex) {
                throw new RuntimeException("Could not determine the MySQL Server version!", ex);
            }
        } else if (dialect instanceof DB2Dialect) {
            return "db2";
        } else if (dialect instanceof PostgreSQLDialect) {
            return "postgresql";
        } else if (dialect instanceof OracleDialect) {
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
        } else {
            return null;
        }
    }
    
    @Override
    public EntityManagerFactory registerFunctions(EntityManagerFactory entityManagerFactory, Map<String, JpqlFunctionGroup> dbmsFunctions) {
        EntityManager em = null;
        
        try {
            em = entityManagerFactory.createEntityManager();
            Session s = em.unwrap(Session.class);
            SessionFactoryImplementor sfi = (SessionFactoryImplementor) s.getSessionFactory();
            Map<String, SqmFunctionDescriptor> originalFunctions = getFunctions(s);
            Map<String, SqmFunctionDescriptor> functions = new TreeMap<String, SqmFunctionDescriptor>(String.CASE_INSENSITIVE_ORDER);
            functions.putAll(originalFunctions);
            Dialect dialect = getDialect(s);
            String dbms = getDbmsName(entityManagerFactory, em, dialect);
            
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
                    functions.put(functionName, new HibernateJpqlFunctionAdapter(sfi, function));
                }
            }
            
            replaceFunctions(s, functions);
            
            return entityManagerFactory;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    @Override
    public Map<String, JpqlFunction> getRegisteredFunctions(EntityManagerFactory entityManagerFactory) {
        EntityManager em = null;
        
        try {
            em = entityManagerFactory.createEntityManager();
            Session s = em.unwrap(Session.class);
            SessionFactoryImplementor sf = (SessionFactoryImplementor) s.getSessionFactory();
            Map<String, SqmFunctionDescriptor> functions = getFunctions(s);
            Map<String, JpqlFunction> map = new HashMap<>(functions.size());
            for (Map.Entry<String, SqmFunctionDescriptor> entry : functions.entrySet()) {
                SqmFunctionDescriptor function = entry.getValue();
                if (function instanceof HibernateJpqlFunctionAdapter) {
                    map.put(entry.getKey(), ((HibernateJpqlFunctionAdapter) function).unwrap());
                } else {
                    map.put(entry.getKey(), new HibernateSQLFunctionAdapter(sf, entry.getValue()));
                }
            }
            return map;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, SqmFunctionDescriptor> getFunctions(Session s) {
        return ((SessionFactoryImplementor) s.getSessionFactory()).getQueryEngine().getSqmFunctionRegistry().getFunctions();
    }
    
    private void replaceFunctions(Session s, Map<String, SqmFunctionDescriptor> newFunctions) {
        SqmFunctionRegistry sqmFunctionRegistry = ((SessionFactoryImplementor) s.getSessionFactory()).getQueryEngine().getSqmFunctionRegistry();
        for (Map.Entry<String, SqmFunctionDescriptor> entry : newFunctions.entrySet()) {
            sqmFunctionRegistry.register(entry.getKey(), entry.getValue());
        }
    }
    
    protected Dialect getDialect(Session s) {
        SessionFactoryImplementor sf = (SessionFactoryImplementor) s.getSessionFactory();
        return sf.getDialect();
    }
}
