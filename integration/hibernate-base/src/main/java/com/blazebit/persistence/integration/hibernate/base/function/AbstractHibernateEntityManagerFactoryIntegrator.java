/*
 * Copyright 2014 - 2021 Blazebit.
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

import com.blazebit.persistence.integration.hibernate.base.spi.HibernateVersionProvider;
import com.blazebit.persistence.spi.EntityManagerFactoryIntegrator;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.JpqlFunctionGroup;
import org.hibernate.Session;
import org.hibernate.Version;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.dialect.function.SQLFunctionRegistry;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public abstract class AbstractHibernateEntityManagerFactoryIntegrator implements EntityManagerFactoryIntegrator {

    protected static final int MAJOR;
    protected static final int MINOR;
    protected static final int FIX;
    protected static final String TYPE;

    private static final Logger LOG = Logger.getLogger(EntityManagerFactoryIntegrator.class.getName());
    private static final boolean USE_FUNCTION_REGISTRY;
    private static final String VERSION_STRING;

    static {
        HibernateVersionProvider usedProvider = null;
        try {
            Iterator<HibernateVersionProvider> iter = ServiceLoader.load(HibernateVersionProvider.class).iterator();
            if (iter.hasNext()) {
                usedProvider = iter.next();
                VERSION_STRING = usedProvider.getVersion();
            } else {
                String versionString = Version.getVersionString();
                if ("[WORKING]".equals(versionString)) {
                    try {
                        Method getModule = Class.class.getMethod("getModule");
                        Method getDescriptor = getModule.getReturnType().getMethod("getDescriptor");
                        Method version = getDescriptor.getReturnType().getMethod("version");
                        Method get = version.getReturnType().getMethod("get");
                        Object module = getModule.invoke(Version.class);
                        Object descriptor = getDescriptor.invoke(module);
                        Object versionOptional = version.invoke(descriptor);
                        versionString = get.invoke(versionOptional).toString();
                    } catch (NoSuchMethodException ex) {
                        throw new IllegalArgumentException("Error while trying to resolve the Hibernate version. This can happen when using an uber-jar deployment. In that case, please provide a service provider implementation of " + HibernateVersionProvider.class.getName());
                    } catch (Exception ex) {
                        throw new IllegalArgumentException("An error happened while trying to resolve the Hibernate version through the module version descriptor", ex);
                    }
                }
                VERSION_STRING = versionString;
            }
            String[] parts = VERSION_STRING.split("[\\.-]");
            MAJOR = Integer.parseInt(parts[0]);
            MINOR = Integer.parseInt(parts[1]);
            FIX = Integer.parseInt(parts[2]);
            TYPE = parts[3];
        } catch (RuntimeException ex) {
            if (usedProvider == null) {
                throw new IllegalArgumentException("Error while trying to resolve the Hibernate version. This can happen when using an uber-jar deployment. In that case, please provide a service provider implementation of " + HibernateVersionProvider.class.getName(), ex);
            } else {
                throw new IllegalArgumentException("An error happened while trying to resolve the Hibernate version through the version provider " + usedProvider.getClass().getName(), ex);
            }
        }
        boolean useFunctionRegistry = false;
        try {
            SQLFunctionRegistry.class.getDeclaredField("userFunctions");
        } catch (NoSuchFieldException ex) {
            // The field was removed in Hibernate 5.0.0.Beta1 which marks the point at which SQLFunctionRegistry is the driver for finding functions
            useFunctionRegistry = true;
        }
        USE_FUNCTION_REGISTRY = useFunctionRegistry;
    }

    protected String getDbmsName(EntityManagerFactory emf, EntityManager em, Dialect dialect) {
        Set<String> classNames = getClassNames(dialect);
        if (classNames.contains("org.hibernate.dialect.MySQL8Dialect") || classNames.contains("org.hibernate.dialect.MariaDB10Dialect")) {
            return "mysql8";
        } else if (classNames.contains("org.hibernate.dialect.MySQLDialect")) {
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
        } else if (classNames.contains("org.hibernate.dialect.DB2Dialect")) {
            return "db2";
        } else if (classNames.contains("org.hibernate.dialect.PostgreSQL81Dialect") || classNames.contains("org.hibernate.dialect.PostgreSQLDialect")) {
            return "postgresql";
        } else if (classNames.contains("org.hibernate.dialect.Oracle8iDialect") || classNames.contains("org.hibernate.dialect.Oracle9Dialect") || classNames.contains("org.hibernate.dialect.OracleDialect")) {
            return "oracle";
        } else if (classNames.contains("org.hibernate.dialect.SQLServerDialect")) {
            return "microsoft";
        } else if (classNames.contains("org.hibernate.dialect.SybaseDialect")) {
            return "sybase";
        } else if (classNames.contains("org.hibernate.dialect.H2Dialect")) {
            return "h2";
        } else if (classNames.contains("org.hibernate.dialect.CUBRIDDialect")) {
            return "cubrid";
        } else if (classNames.contains("org.hibernate.dialect.HSQLDialect")) {
            return "hsql";
        } else if (classNames.contains("org.hibernate.dialect.InformixDialect")) {
            return "informix";
        } else if (classNames.contains("org.hibernate.dialect.IngresDialect")) {
            return "ingres";
        } else if (classNames.contains("org.hibernate.dialect.InterbaseDialect")) {
            return "interbase";
        } else if (classNames.contains("org.hibernate.dialect.CockroachDB192Dialect")) {
            return "cockroach";
        } else {
            return null;
        }
    }

    private Set<String> getClassNames(Dialect dialect) {
        Set<String> classNames = new HashSet<>();
        Class<?> c = dialect.getClass();
        do {
            classNames.add(c.getName());
        } while ((c = c.getSuperclass()) != Object.class);
        return classNames;
    }

    @Override
    public EntityManagerFactory registerFunctions(EntityManagerFactory entityManagerFactory, Map<String, JpqlFunctionGroup> dbmsFunctions) {
        EntityManager em = null;

        try {
            em = entityManagerFactory.createEntityManager();
            Session s = em.unwrap(Session.class);
            Map<String, SQLFunction> originalFunctions = getFunctions(s);
            Map<String, SQLFunction> functions = new TreeMap<String, SQLFunction>(String.CASE_INSENSITIVE_ORDER);
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
                    if (functions.containsKey(functionName)) {
                        LOG.finest("Using ORM registered function '" + functionName + "' because there is neither an implementation for the dbms '" + dbms + "' nor a default implementation.");
                    } else {
                        LOG.warning("Could not register the function '" + functionName + "' because there is neither an implementation for the dbms '" + dbms + "' nor a default implementation!");
                    }
                } else {
                    functions.put(functionName, new HibernateJpqlFunctionAdapter(function));
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
            Map<String, SQLFunction> functions = getFunctions(s);
            Map<String, JpqlFunction> map = new HashMap<>(functions.size());
            for (Map.Entry<String, SQLFunction> entry : functions.entrySet()) {
                SQLFunction function = entry.getValue();
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
    private Map<String, SQLFunction> getFunctions(Session s) {
        if (USE_FUNCTION_REGISTRY) {
            SessionFactoryImplementor sf = (SessionFactoryImplementor) s.getSessionFactory();
            SQLFunctionRegistry registry = sf.getSqlFunctionRegistry();
            Exception ex;

            // We have to retrieve the functionMap the old fashioned way via reflection :(
            Field f = null;
            boolean madeAccessible = false;

            try {
                f = SQLFunctionRegistry.class.getDeclaredField("functionMap");
                madeAccessible = !f.isAccessible();

                if (madeAccessible) {
                    f.setAccessible(true);
                }

                return (Map<String, SQLFunction>) f.get(registry);
            } catch (NoSuchFieldException e) {
                ex = e;
            } catch (IllegalArgumentException e) {
                // This can never happen
                ex = e;
            } catch (IllegalAccessException e) {
                ex = e;
            } finally {
                if (f != null && madeAccessible) {
                    f.setAccessible(false);
                }
            }

            throw new RuntimeException("Could not access the function map to dynamically register functions. Please report the version of hibernate your are using so we can provide support for it!", ex);
        } else {
            // Implementation detail: Hibernate uses a mutable map, so we can do this
            return getDialect(s).getFunctions();
        }
    }

    private void replaceFunctions(Session s, Map<String, SQLFunction> newFunctions) {
        if (USE_FUNCTION_REGISTRY) {
            SessionFactoryImplementor sf = (SessionFactoryImplementor) s.getSessionFactory();
            SQLFunctionRegistry registry = sf.getSqlFunctionRegistry();
            Exception ex;

            // We have to retrieve the functionMap the old fashioned way via reflection :(
            Field f = null;
            boolean madeAccessible = false;

            try {
                f = SQLFunctionRegistry.class.getDeclaredField("functionMap");
                madeAccessible = !f.isAccessible();

                if (madeAccessible) {
                    f.setAccessible(true);
                }

                f.set(registry, newFunctions);
                return;
            } catch (NoSuchFieldException e) {
                ex = e;
            } catch (IllegalArgumentException e) {
                // This can never happen
                ex = e;
            } catch (IllegalAccessException e) {
                ex = e;
            } finally {
                if (f != null && madeAccessible) {
                    f.setAccessible(false);
                }
            }

            throw new RuntimeException("Could not access the function map to dynamically register functions. Please report the version of hibernate your are using so we can provide support for it!", ex);
        } else {
            Exception ex;
            Field f = null;
            boolean madeAccessible = false;

            try {
                f = Dialect.class.getDeclaredField("sqlFunctions");
                madeAccessible = !f.isAccessible();

                if (madeAccessible) {
                    f.setAccessible(true);
                }

                f.set(getDialect(s), newFunctions);
                return;
            } catch (NoSuchFieldException e) {
                ex = e;
            } catch (IllegalArgumentException e) {
                // This can never happen
                ex = e;
            } catch (IllegalAccessException e) {
                ex = e;
            } finally {
                if (f != null && madeAccessible) {
                    f.setAccessible(false);
                }
            }
            throw new RuntimeException("Could not access the function map to dynamically register functions. Please report the version of hibernate your are using so we can provide support for it!", ex);
        }
    }

    protected Dialect getDialect(Session s) {
        SessionFactoryImplementor sf = (SessionFactoryImplementor) s.getSessionFactory();
        return sf.getDialect();
    }
}
