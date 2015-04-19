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

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.spi.EntityManagerIntegrator;
import com.blazebit.persistence.spi.JpqlFunction;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.dialect.CUBRIDDialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.InformixDialect;
import org.hibernate.dialect.IngresDialect;
import org.hibernate.dialect.InterbaseDialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.Oracle8iDialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.dialect.SybaseDialect;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.spi.SessionFactoryImplementor;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
@ServiceProvider(EntityManagerIntegrator.class)
public class HibernateEntityManagerIntegrator implements EntityManagerIntegrator {
    
    private static final Logger LOG = Logger.getLogger(EntityManagerIntegrator.class.getName());
    
    @Override
    public EntityManager registerFunctions(EntityManager em, Map<String, Map<String, JpqlFunction>> dbmsFunctions) {
        Dialect dialect = getDialect(em);

        String dbms;
        
        if (dialect instanceof MySQLDialect) {
            dbms = "mysql";
        } else if (dialect instanceof Oracle8iDialect) {
            dbms = "oracle";
        } else if (dialect instanceof SQLServerDialect) {
            dbms = "microsoft";
        } else if (dialect instanceof SybaseDialect) {
            dbms = "sybase";
        } else if (dialect instanceof H2Dialect) {
            dbms = "h2";
        } else if (dialect instanceof CUBRIDDialect) {
            dbms = "cubrid";
        } else if (dialect instanceof HSQLDialect) {
            dbms = "hsql";
        } else if (dialect instanceof InformixDialect) {
            dbms = "informix";
        } else if (dialect instanceof IngresDialect) {
            dbms = "ingres";
        } else if (dialect instanceof InterbaseDialect) {
            dbms = "interbase";
        } else {
            dbms = null;
        }
        
        // Implementation detail: Hibernate uses a mutable map, so we can do this
        Map<String, SQLFunction> functions = dialect.getFunctions();
        
        for (Map.Entry<String, Map<String, JpqlFunction>> functionEntry : dbmsFunctions.entrySet()) {
            String functionName = functionEntry.getKey();
            Map<String, JpqlFunction> dbmsFunctionMap = functionEntry.getValue();
            JpqlFunction function = dbmsFunctionMap.get(dbms);
            
            if (function == null) {
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
        return getDialect(em).getFunctions().keySet();
    }
    
    private Dialect getDialect(EntityManager em) {
        Session s = em.unwrap(Session.class);
        SessionFactoryImplementor sf = (SessionFactoryImplementor) s.getSessionFactory();
        return sf.getDialect();
    }
}
