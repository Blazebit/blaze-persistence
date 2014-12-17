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
import com.blazebit.persistence.impl.hibernate.function.pageposition.MySQLPagePositionFunction;
import com.blazebit.persistence.impl.hibernate.function.pageposition.OraclePagePositionFunction;
import com.blazebit.persistence.impl.hibernate.function.pageposition.PagePositionFunction;
import com.blazebit.persistence.impl.hibernate.function.pageposition.TransactSQLPagePositionFunction;
import com.blazebit.persistence.spi.EntityManagerEnricher;
import java.util.Map;
import javax.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.dialect.Dialect;
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
@ServiceProvider(EntityManagerEnricher.class)
public class FunctionEntityManagerEnricher implements EntityManagerEnricher {
    
    private static final String PAGE_POSITION_FUNCTION = "page_position";
    
    @Override
    public EntityManager enrich(EntityManager em) {
        Session s = em.unwrap(Session.class);
        SessionFactoryImplementor sf = (SessionFactoryImplementor) s.getSessionFactory();
        Dialect dialect = sf.getDialect();
        
        enrich(dialect);

        return em;
    }
    
    public void enrich(Dialect dialect) {
        // Implementation detail: Hibernate uses a mutable map, so we can do this
        Map<String, SQLFunction> functions = dialect.getFunctions();
        
        if (dialect instanceof MySQLDialect) {
            functions.put(PAGE_POSITION_FUNCTION, new MySQLPagePositionFunction());
        } else if (dialect instanceof Oracle8iDialect) {
            functions.put(PAGE_POSITION_FUNCTION, new OraclePagePositionFunction());
        } else if (dialect instanceof SQLServerDialect) {
            functions.put(PAGE_POSITION_FUNCTION, new TransactSQLPagePositionFunction());
        } else if (dialect instanceof SybaseDialect) {
            functions.put(PAGE_POSITION_FUNCTION, new TransactSQLPagePositionFunction());
        } else {
            functions.put(PAGE_POSITION_FUNCTION, new PagePositionFunction());
        }
    }
}
