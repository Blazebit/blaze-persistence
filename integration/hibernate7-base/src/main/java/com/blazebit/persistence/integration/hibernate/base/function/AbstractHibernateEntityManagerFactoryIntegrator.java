/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate.base.function;

import com.blazebit.persistence.integration.hibernate.base.HibernateAccessUtils;
import com.blazebit.persistence.spi.EntityManagerFactoryIntegrator;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.JpqlFunctionGroup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.Session;
import org.hibernate.dialect.CockroachDialect;
import org.hibernate.dialect.DB2Dialect;
import org.hibernate.dialect.DB2iDialect;
import org.hibernate.dialect.DB2zDialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.HANADialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.MariaDBDialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.OracleDialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.dialect.SpannerDialect;
import org.hibernate.dialect.SybaseDialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.sqm.function.SqmFunctionDescriptor;
import org.hibernate.query.sqm.function.SqmFunctionRegistry;
import org.hibernate.query.sqm.produce.function.ArgumentTypesValidator;
import org.hibernate.query.sqm.produce.function.FunctionParameterType;
import org.hibernate.query.sqm.produce.function.StandardArgumentsValidators;
import org.hibernate.query.sqm.produce.function.StandardFunctionArgumentTypeResolvers;
import org.hibernate.query.sqm.produce.function.StandardFunctionReturnTypeResolvers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Christian Beikov
 * @since 1.6.7
 */
public abstract class AbstractHibernateEntityManagerFactoryIntegrator implements EntityManagerFactoryIntegrator {

    private static final Logger LOG = Logger.getLogger(EntityManagerFactoryIntegrator.class.getName());
    private static final Set<String> NATIVE_WINDOW_FUNCTIONS = new HashSet<>(Arrays.asList(
        "listagg",
        "every",
        "row_number",
        "rank",
        "dense_rank",
        "percentile_cont",
        "percentile_disc",
        "percent_rank",
        "lag",
        "lead",
        "last_value",
        "first_value",
        "nth_value",
        "mode",
        "cume_dist"
    ));
    private static final Set<String> FUNCTIONS_TO_SKIP = new HashSet<>(Arrays.asList(
        "concat"
    ));

    protected String getDbmsName(Dialect dialect) {
        if (dialect instanceof MariaDBDialect) {
            return "mariadb";
        } else if (dialect instanceof MySQLDialect) {
            if (((MySQLDialect) dialect).getMySQLVersion().isSameOrAfter(8)) {
                return "mysql8";
            } else {
                return "mysql";
            }
        } else if (dialect instanceof CockroachDialect) {
            return "cockroach";
        } else if (dialect instanceof DB2iDialect) {
            return "db2i";
        } else if (dialect instanceof DB2zDialect) {
            return "db2z";
        } else if (dialect instanceof DB2Dialect) {
            return "db2";
        } else if (dialect instanceof HANADialect) {
            return "hana";
        } else if (dialect instanceof SpannerDialect) {
            return "spanner";
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
        } else if (dialect instanceof HSQLDialect) {
            return "hsql";
        } else {
            switch (dialect.getClass().getSimpleName()) {
                case "CUBRIDDialect":
                    return "cubrid";
                case "InformixDialect":
                    return "informix";
                case "IngresDialect":
                    return "ingres";
                default:
                    return null;
            }
        }
    }

    @Override
    public EntityManagerFactory registerFunctions(EntityManagerFactory entityManagerFactory, Map<String, JpqlFunctionGroup> dbmsFunctions) {
        EntityManager em = null;

        try {
            em = entityManagerFactory.createEntityManager();
            Session s = em.unwrap(Session.class);
            SessionFactoryImplementor sfi = (SessionFactoryImplementor) s.getSessionFactory();
            SqmFunctionRegistry sqmFunctionRegistry = HibernateAccessUtils.getSqmFunctionRegistry(sfi);
            Dialect dialect = getDialect(s);
            String dbms = getDbmsName(dialect);

            for (Map.Entry<String, JpqlFunctionGroup> functionEntry : dbmsFunctions.entrySet()) {
                String functionName = functionEntry.getKey();
                JpqlFunctionGroup dbmsFunctionMap = functionEntry.getValue();
                if (NATIVE_WINDOW_FUNCTIONS.contains(functionName) || FUNCTIONS_TO_SKIP.contains(functionName)) {
                    // In Hibernate 6 we have to skip some functions as they are provided with special HQL syntax
                    continue;
                }
                if ("ntile".equals(functionName)) {
                    // Special case for window function
                    sqmFunctionRegistry.namedWindowDescriptorBuilder("ntile")
                        .setReturnTypeResolver(StandardFunctionReturnTypeResolvers.invariant(sfi.getTypeConfiguration().getBasicTypeForJavaType(Integer.class)))
                        .setArgumentTypeResolver(StandardFunctionArgumentTypeResolvers.invariant(FunctionParameterType.INTEGER))
                        .setArgumentsValidator(new ArgumentTypesValidator(StandardArgumentsValidators.exactly(1), FunctionParameterType.INTEGER))
                        .register();
                    continue;
                }
                JpqlFunction function = dbmsFunctionMap.get(dbms);

                if (function == null && !dbmsFunctionMap.contains(dbms)) {
                    function = dbmsFunctionMap.get(null);
                }
                if (function == null) {
                    LOG.warning("Could not register the function '" + functionName + "' because there is neither an implementation for the dbms '" + dbms + "' nor a default implementation!");
                } else {
                    sqmFunctionRegistry.register(functionName, new HibernateJpqlFunctionAdapter(sfi, dbmsFunctionMap.getKind(), function));
                }
            }

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
            Map<String, JpqlFunction> map = new HashMap<>();
            HibernateAccessUtils.getSqmFunctionRegistry(sf).getFunctionsByName().forEach( entry -> {
                SqmFunctionDescriptor function = entry.getValue();
                if (function instanceof HibernateJpqlFunctionAdapter) {
                    map.put(entry.getKey(), ((HibernateJpqlFunctionAdapter) function).unwrap());
                } else {
                    map.put(entry.getKey(), new HibernateSqmFunctionDescriptorAdapter(sf, entry.getValue()));
                }
            });
            return map;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    protected Dialect getDialect(Session s) {
        SessionFactoryImplementor sf = (SessionFactoryImplementor) s.getSessionFactory();
        return sf.getJdbcServices().getDialect();
    }
}
