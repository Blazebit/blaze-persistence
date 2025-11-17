/*
 * Copyright (c) 2008, 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//     Gavin King      - 3.2
//     Linda DeMichiel - 2.1
//     Linda DeMichiel - 2.0

package jakarta.persistence;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.criteria.CriteriaBuilder;

/**
 * Interface used to interact with the persistence unit, and to
 * create new instances of {@link EntityManager}.
 *
 * <p>A persistence unit defines the set of all classes that are
 * related or grouped by the application, and which must be
 * colocated in their mapping to a single database. If two entity
 * types participate in an association, then they must belong to
 * the same persistence unit.
 *
 * <p>A persistence unit may be defined by a {@code persistence.xml}
 * file, or it may be defined at runtime via the
 * {@link PersistenceConfiguration} API.
 *
 * <p>Every persistence unit has a <em>transaction type</em>,
 * either {@link PersistenceUnitTransactionType#JTA JTA}, or
 * {@link PersistenceUnitTransactionType#RESOURCE_LOCAL RESOURCE_LOCAL}.
 * Resource-local transactions are managed programmatically via the
 * {@link EntityTransaction} interface.
 *
 * <p>An {@link EntityManagerFactory} with a lifecycle managed by
 * the application may be created using the static operations of
 * the {@link Persistence} class:
 * <ul>
 * <li>if the persistence unit is defined in {@code persistence.xml},
 *     an entity manager factory may be created by calling
 *     {@link Persistence#createEntityManagerFactory(String)} or
 *     {@link Persistence#createEntityManagerFactory(String,Map)},
 *     or
 * <li>if the persistence unit was defined using
 *     {@link PersistenceConfiguration}, an entity manager factory
 *     may be created by calling
 *     {@link Persistence#createEntityManagerFactory(PersistenceConfiguration)}.
 * </ul>
 *
 * <p>Usually, there is exactly one {@code EntityManagerFactory} for
 * each persistence unit:
 * {@snippet :
 * // create a factory at initialization time
 * static final EntityManagerFactory entityManagerFactory =
 *         Persistence.createEntityManagerFactory("orderMgt");
 * }
 *
 * <p>Alternatively, in the Jakarta EE environment, a
 * container-managed {@code EntityManagerFactory} may be obtained
 * by dependency injection, using {@link PersistenceUnit}.
 * {@snippet :
 * // inject the container-managed factory
 * @PersistenceUnit(unitName="orderMgt")
 * EntityManagerFactory entityManagerFactory;
 * }
 *
 * <p>An application-managed {@code EntityManager} may be created
 * via a call to {@link #createEntityManager()}. However, this
 * approach places complete responsibility for cleanup and exception
 * management on the client, and is thus considered error-prone. It
 * is much safer to use the methods {@link #runInTransaction} and
 * {@link #callInTransaction} to obtain {@code EntityManager}s.
 * Alternatively, in the Jakarta EE environment, a container-managed
 * {@link EntityManager} may be obtained by dependency injection,
 * using {@link PersistenceContext}, and the application need not
 * interact with the {@code EntityManagerFactory} directly.
 *
 * <p>The {@code EntityManagerFactory} provides access to certain
 * other useful APIs:
 * <ul>
 * <li>an instance of {@link Metamodel} exposing a model of the
 *     managed types associated with the persistence unit may be
 *     obtained by calling {@link #getMetamodel()},
 * <li>an instance of {@link SchemaManager}, allowing programmatic
 *     control over schema generation and validation, may be
 *     obtained by calling {@link #getSchemaManager()},
 * <li>an instance of {@link Cache}, allowing direct programmatic
 *     control over the second-level cache, may be obtained by
 *     calling {@link #getCache()},
 * <li>the {@link CriteriaBuilder}, used to define criteria queries,
 *     may be obtained by calling {@link #getCriteriaBuilder()},
 *     and
 * <li>the {@link PersistenceUnitUtil} may be obtained by calling
 *     {@link #getPersistenceUnitUtil()}.
 * </ul>
 *
 * <p>When the application has finished using the entity manager
 * factory, or when the application terminates, the application
 * should {@linkplain #close} the entity manager factory. If
 * necessary, a {@link java.lang.ref.Cleaner} may be used:
 * {@snippet :
 * // factory should be destroyed before program terminates
 * Cleaner.create().register(entityManagerFactory, entityManagerFactory::close);
 * }
 * Once an {@code EntityManagerFactory} has been closed, all its
 * entity managers are considered to be in the closed state.
 *
 * @see EntityManager
 *
 * @since 1.0
 */
public interface EntityManagerFactory extends AutoCloseable {

    /**
     * Create a new application-managed {@link EntityManager}. This
     * method returns a new {@code EntityManager} instance each time
     * it is invoked. 
     * <p>The {@link EntityManager#isOpen} method will return true
     * on the returned instance.
     * @return entity manager instance
     * @throws IllegalStateException if the entity manager factory
     * has been closed
     */
    EntityManager createEntityManager();
    
    /**
     * Create a new application-managed {@link EntityManager} with
     * the given {@link Map} specifying property settings. This
     * method returns a new {@code EntityManager} instance each time
     * it is invoked.
     * <p>The {@link EntityManager#isOpen} method will return true
     * on the returned instance.
     * @param map properties for entity manager
     * @return entity manager instance
     * @throws IllegalStateException if the entity manager factory
     * has been closed
     */
    EntityManager createEntityManager(Map<?, ?> map);

    /**
     * Create a new JTA application-managed {@link EntityManager} with
     * the specified synchronization type. This method returns a new
     * {@code EntityManager} instance each time it is invoked.
     * <p>The {@link EntityManager#isOpen} method will return true on
     * the returned instance.
     * @param synchronizationType  how and when the entity manager should
     *                             be synchronized with the current JTA
     *                             transaction
     * @return entity manager instance
     * @throws IllegalStateException if the entity manager factory has
     * been configured for resource-local entity managers or is closed
     *
     * @since 2.1
     */
    EntityManager createEntityManager(SynchronizationType synchronizationType);

    /**
     * Create a new JTA application-managed {@link EntityManager} with
     * the specified synchronization type and map of properties. This
     * method returns a new {@code EntityManager} instance each time it
     * is invoked.
     * <p>The {@link EntityManager#isOpen} method will return true on the
     * returned instance.
     * @param synchronizationType  how and when the entity manager should
     *                             be synchronized with the current JTA
     *                             transaction
     * @param map properties for entity manager
     * @return entity manager instance
     * @throws IllegalStateException if the entity manager factory has
     * been configured for resource-local entity managers or is closed
     *
     * @since 2.1
     */
    EntityManager createEntityManager(SynchronizationType synchronizationType, Map<?, ?> map);

    /**
     * Return an instance of {@link CriteriaBuilder} which may be used
     * to construct {@link jakarta.persistence.criteria.CriteriaQuery}
     * objects.
     * @return an instance of {@link CriteriaBuilder}
     * @throws IllegalStateException if the entity manager factory has
     * been closed
     *
     * @see EntityManager#getCriteriaBuilder()
     *
     * @since 2.0
     */
    CriteriaBuilder getCriteriaBuilder();
    
    /**
     * Return an instance of the {@link Metamodel} interface for access
     * to the metamodel of the persistence unit.
     * @return an instance of {@link Metamodel}
     * @throws IllegalStateException if the entity manager factory
     * has been closed
     *
     * @since 2.0
     */
    Metamodel getMetamodel();

    /**
     * Indicates whether the factory is open. Returns true until the
     * factory has been closed.
     * @return boolean indicating whether the factory is open
     */
    boolean isOpen();
    
    /**
     * Close the factory, releasing any resources that it holds.
     * After a factory instance has been closed, all methods invoked
     * on it will throw the {@link IllegalStateException}, except
     * for {@link #isOpen}, which will return false. Once an
     * {@code EntityManagerFactory} has been closed, all its
     * entity managers are considered to be in the closed state.
     * @throws IllegalStateException if the entity manager factory
     * has been closed
     */
    void close();

    /**
     * The name of the persistence unit.
     *
     * @since 3.2
     */
    String getName();

    /**
     * Get the properties and associated values that are in effect
     * for the entity manager factory. Changing the contents of the
     * map does not change the configuration in effect.
     * @return properties
     * @throws IllegalStateException if the entity manager factory 
     * has been closed
     *
     * @since 2.0
     */
    Map<String, Object> getProperties();

    /**
     * Access the cache that is associated with the entity manager 
     * factory (the "second level cache").
     * @return an instance of {@link Cache}, or null if there is no
     * second-level cache in use
     * @throws IllegalStateException if the entity manager factory
     * has been closed
     *
     * @since 2.0
     */
    Cache getCache();

    /**
     * Return interface providing access to utility methods for the
     * persistence unit.
     * @return an instance of {@link PersistenceUnitUtil}
     * @throws IllegalStateException if the entity manager factory
     * has been closed
     *
     * @since 2.0
     */
    PersistenceUnitUtil getPersistenceUnitUtil();

    /**
     * The type of transaction management used by this persistence
     * unit, either resource-local transaction management, or JTA.
     *
     * @since 3.2
     */
    PersistenceUnitTransactionType getTransactionType();

    /**
     * Return interface providing access to schema management
     * operations for the persistence unit.
     * @return an instance of {@link SchemaManager}
     * @throws IllegalStateException if the entity manager factory
     * has been closed
     *
     * @since 3.2
     */
    SchemaManager getSchemaManager();

    /**
     * Define the query, typed query, or stored procedure query as
     * a named query such that future query objects can be created
     * from it using the {@link EntityManager#createNamedQuery} or
     * {@link EntityManager#createNamedStoredProcedureQuery} methods.
     * <p>Any configuration of the query object (except for actual
     * parameter binding) in effect when the named query is added
     * is retained as part of the named query definition. This
     * includes configuration information such as max results, hints,
     * flush mode, lock mode, result set mapping information, and
     * information about stored procedure parameters.
     * <p>When the query is executed, information that can be set by
     * means of the query APIs can be overridden. Information that is
     * overridden does not affect the named query as registered with
     * the entity manager factory, and thus does not affect subsequent
     * query objects created from it by calling {@code createNamedQuery}
     * or {@code createNamedStoredProcedureQuery}.
     * <p>If a named query of the same name has been previously defined,
     * either statically via metadata or via this method, that query
     * definition is replaced.
     *
     * @param name name for the query
     * @param query a {@link Query}, {@link TypedQuery},
     *             or {@link StoredProcedureQuery}
     *
     * @since 2.1
     */
    void addNamedQuery(String name, Query query);

    /**
     * Return an object of the specified type to allow access to
     * a provider-specific API. If the provider implementation of
     * {@code EntityManagerFactory} does not support the given
     * type, the {@link PersistenceException} is thrown.
     * @param cls the class of the object to be returned.
     *            This is usually either the underlying class
     *            implementing {@code EntityManagerFactory} or an
     *            interface it implements.
     * @return an instance of the specified class
     * @throws PersistenceException if the provider does not support
     *        the given type
     * @since 2.1
     */
    <T> T unwrap(Class<T> cls);

    /**
     * Add a named copy of the given {@link EntityGraph} to this
     * {@code EntityManagerFactory}. If an entity graph with the
     * given name already exists, it is replaced.
     * @param graphName  name for the entity graph
     * @param entityGraph  entity graph
     * @since 2.1
     */
    <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph);

    /**
     * A map keyed by {@linkplain NamedQuery#name query name}, containing
     * {@linkplain TypedQueryReference references} to every named query whose
     * result type is assignable to the given Java type.
     * @param resultType any Java type, including {@code Object.class}
     *                   meaning all queries
     * @return a map keyed by query name
     * @param <R> the specified upper bound on the query result types
     *
     * @since 3.2
     */
    <R> Map<String, TypedQueryReference<R>> getNamedQueries(Class<R> resultType);

    /**
     * A map keyed by {@linkplain NamedEntityGraph#name graph name}, containing
     * every named {@linkplain EntityGraph entity graph} whose entity type is
     * assignable to the given Java type.
     * @param entityType any Java type, including {@code Object.class}
     *                   meaning all entity graphs
     * @return a map keyed by graph name
     * @param <E> the specified upper bound on the entity graph types
     *
     * @since 3.2
     */
    <E> Map<String, EntityGraph<? extends E>> getNamedEntityGraphs(Class<E> entityType);

    /**
     * Create a new application-managed {@link EntityManager} with an active
     * transaction, and execute the given function, passing the {@code EntityManager}
     * to the function.
     * <p>
     * If the transaction type of the persistence unit is JTA, and there is a JTA
     * transaction already associated with the caller, then the {@code EntityManager}
     * is associated with this current transaction. If the given function throws an
     * exception, the JTA transaction is marked for rollback, and the exception is
     * rethrown.
     * <p>
     * Otherwise, if the transaction type of the persistence unit is resource-local,
     * or if there is no JTA transaction already associated with the caller, then
     * the {@code EntityManager} is associated with a new transaction. If the given
     * function returns without throwing an exception, this transaction is committed.
     * If the function does throw an exception, the transaction is rolled back, and
     * the exception is rethrown.
     * <p>
     * Finally, the {@code EntityManager} is closed before this method returns
     * control to the client.
     *
     * @param work a function to be executed in the scope of the transaction
     *
     * @since 3.2
     */
    void runInTransaction(Consumer<EntityManager> work);
    /**
     * Create a new application-managed {@link EntityManager} with an active
     * transaction, and call the given function, passing the {@code EntityManager}
     * to the function.
     * <p>
     * If the transaction type of the persistence unit is JTA, and there is a JTA
     * transaction already associated with the caller, then the {@code EntityManager}
     * is associated with this current transaction. If the given function returns
     * without throwing an exception, the result of the function is returned. If the
     * given function throws an exception, the JTA transaction is marked for rollback,
     * and the exception is rethrown.
     * <p>
     * Otherwise, if the transaction type of the persistence unit is resource-local,
     * or if there is no JTA transaction already associated with the caller, then
     * the {@code EntityManager} is associated with a new transaction. If the given
     * function returns without throwing an exception, this transaction is committed
     * and the result of the function is returned. If the function does throw an
     * exception, the transaction is rolled back, and the exception is rethrown.
    * <p>
     * Finally, the {@code EntityManager} is closed before this method returns
     * control to the client.
     *
     * @param work a function to be called in the scope of the transaction
     * @return the value returned by the given function
     *
     * @since 3.2
     */
    <R> R callInTransaction(Function<EntityManager, R> work);
}
