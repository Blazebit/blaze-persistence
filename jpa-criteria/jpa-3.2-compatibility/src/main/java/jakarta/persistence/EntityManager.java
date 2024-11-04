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
import java.util.List;

import jakarta.persistence.criteria.CriteriaSelect;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.CriteriaDelete;

/**
 * Interface used to interact with the persistence context.
 *
 * <p>An instance of {@code EntityManager} must be obtained from
 * an {@link EntityManagerFactory}, and is only able to manage
 * persistence of entities belonging to the associated persistence
 * unit.
 *
 * <p>An application-managed {@code EntityManager} may be created
 * via a call to {@link EntityManagerFactory#createEntityManager()}.
 * The {@code EntityManager} must be explicitly closed via a call
 * to {@link #close()}, to allow resources to be cleaned up by the
 * persistence provider. This approach places almost complete
 * responsibility for cleanup and exception management on the client,
 * and is thus considered quite error-prone. It is much safer to use
 * the methods {@link EntityManagerFactory#runInTransaction} and
 * {@link EntityManagerFactory#callInTransaction}.
 * {@snippet :
 * entityManagerFactory.runInTransaction(entityManager -> {
 *     // do work in a persistence context
 *     ...
 * });
 * }
 *
 * <p>In the Jakarta EE environment, a container-managed
 * {@link EntityManager} may be obtained by dependency injection,
 * using {@link PersistenceContext}.
 * {@snippet :
 * // inject the container-managed entity manager
 * @PersistenceContext(unitName="orderMgt")
 * EntityManager entityManager;
 * }
 *
 * <p>If the persistence unit has
 * {@linkplain PersistenceUnitTransactionType#RESOURCE_LOCAL
 * resource local} transaction management, transactions must
 * be managed using the {@link EntityTransaction} obtained by
 * calling {@link #getTransaction()}.
 *
 * <p>A complete idiom for custom application management of
 * the {@link EntityManager} and its associated resource-local
 * {@link EntityTransaction} is as follows:
 * {@snippet :
 * EntityManager entityManager = entityManagerFactory.createEntityManager();
 * EntityTransaction transaction = entityManager.getTransaction();
 * try {
 *     transaction.begin();
 *     // do work
 *     ...
 *     transaction.commit();
 * }
 * catch (Exception e) {
 *     if (transaction.isActive()) transaction.rollback();
 *     throw e;
 * }
 * finally {
 *     entityManager.close();
 * }
 * }
 *
 * <p>Each {@code EntityManager} instance is associated with a
 * distinct <em>persistence context</em>. A persistence context
 * is a set of entity instances in which for any given persistent
 * entity identity (defined by an entity type and primary key)
 * there is at most one entity instance. The entity instances
 * associated with a persistence context are considered managed
 * objects, with a well-defined lifecycle under the control of
 * the persistence provider.
 *
 * <p>Any entity instance can be characterized as being in one of
 * the following lifecycle states:
 * <ul>
 * <li>A <em>new</em> entity has no persistent identity, and is
 *     not yet associated with any persistence context.
 * <li>A <em>managed</em> entity is an instance with a persistent
 *     identity that is currently associated with a persistence
 *     context.
 * <li>A <em>detached</em> entity is an instance with a persistent
 *     identity that is not (or no longer) associated with any
 *     active persistence context.
 * <li>A <em>removed</em> entity is an instance with a persistent
 *     identity, and associated with a persistence context, that
 *     is scheduled for removal from the database upon transaction
 *     commit.
 * </ul>
 *
 * <p>The {@code EntityManager} API is used to perform operations
 * that affect the state of the persistence context, or that modify
 * the lifecycle state of individual entity instances. The client
 * may {@linkplain #persist} and {@linkplain #remove} instances,
 * {@linkplain #find(Class, Object) find} entities by their primary
 * key, and execute {@linkplain #createQuery(String) queries} which
 * range over entity types. An entity may be disassociated from
 * the persistence context by calling {@link #detach}, and a
 * persistence context may be completely cleared, detaching all
 * its entities, by calling {@link #clear()}.
 *
 * <p>The client may also make changes to the state of an entity
 * instance by mutating the entity directly, or it may request
 * that the state of a detached instance be {@linkplain #merge
 * merged}, replacing the state of a managed instance with the
 * same persistent identity. Note that there is no explicit
 * "update" operation; since an entity is a managed object,
 * modifications to its persistent fields and properties are
 * automatically detected, as long as it is associated with an
 * active persistence context.
 *
 * <p>Modifications to the state of entities associated with a
 * persistence context are not immediately synchronized with the
 * database. Synchronization happens during a process called
 * <em>flush</em>. The timing of the flush process depends on the
 * {@linkplain FlushModeType flush mode}, which may be set
 * explicitly by calling {@link #setFlushMode(FlushModeType)}.
 * <ul>
 * <li>For {@link FlushModeType#COMMIT}, the persistence context
 *     is flushed before the transaction commits.
 * <li>For {@link FlushModeType#AUTO}, which is the default, the
 *     persistence context must also be flushed before execution
 *     of any query whose result set would be affected by
 *     unflushed modifications to entities associated with the
 *     persistence context.
 * </ul>
 * The client may force an immediate flush to occur by calling
 * {@link #flush()}.
 *
 * <p>At any given moment, a persistence context might hold an
 * optimistic or pessimistic <em>lock</em> on an entity instance.
 * The full range of possible lock types is enumerated by
 * {@link LockModeType}. Some operations of this interface,
 * including the methods {@link #lock(Object, LockModeType)},
 * {@link #refresh(Object, LockModeType)}, and
 * {@link #find(Class, Object, LockModeType)}, accept an explicit
 * {@link LockModeType}, allowing the client to request a specific
 * type of lock.
 *
 * <p>Interaction of the persistence context (or first-level cache)
 * with the second-level cache, if any, may be controlled by
 * calling {@link #setCacheRetrieveMode(CacheRetrieveMode)} and
 * {@link #setCacheStoreMode(CacheStoreMode)}.
 *
 * <p>Some operations accept one or more built-in and vendor-specific
 * options:
 * <ul>
 * <li>{@link #find(Class, Object, FindOption...)} and 
 *     {@link #find(EntityGraph, Object, FindOption...)} accept
 *     {@link FindOption}s,
 * <li>{@link #refresh(Object, RefreshOption...)} accepts
 *     {@link RefreshOption}s, and
 * <li>{@link #lock(Object, LockModeType, LockOption...)} accepts
 *     {@link LockOption}s.
 * </ul>
 *
 * @see Query
 * @see TypedQuery
 * @see CriteriaQuery
 * @see PersistenceContext
 * @see StoredProcedureQuery
 * @see EntityManagerFactory
 * 
 * @since 1.0
 */
public interface EntityManager extends AutoCloseable {

    /**
     * Make a new entity instance managed and persistent, resulting in
     * its insertion in the database when the persistence context is
     * synchronized with the database, or make a removed entity managed,
     * undoing the effect of a previous call to {@link #remove(Object)}.
     * This operation cascades to every entity related by an association
     * marked {@link CascadeType#PERSIST cascade=PERSIST}. If the given
     * entity instance is already managed, that is, if it already belongs
     * to this persistence context, and has not been marked for removal,
     * it is itself ignored, but the operation still cascades.
     * @param entity  a new, managed, or removed entity instance
     * @throws EntityExistsException if the given entity is detached
     * (if the entity is detached, the {@code EntityExistsException}
     * may be thrown when the persist operation is invoked, or the
     * {@code EntityExistsException} or another {@code PersistenceException}
     * may be thrown at flush or commit time)
     * @throws IllegalArgumentException if the given instance is not an
     *         entity
     * @throws TransactionRequiredException if there is no transaction
     *         when invoked on a container-managed entity manager that
     *         is of type {@link PersistenceContextType#TRANSACTION}
     */
    void persist(Object entity);
    
    /**
     * Merge the state of the given new or detached entity instance
     * into the current persistence context, resulting in, respectively,
     * an insert or possible update when the persistence context is
     * synchronized with the database. Return a managed instance with
     * the same persistent state as the given entity instance, but a
     * distinct Java object identity. If the given entity is detached,
     * the returned entity has the same persistent identity. This
     * operation cascades to every entity related by an association
     * marked {@link CascadeType#MERGE cascade=MERGE}. If the given
     * entity instance is managed, that is, if it belongs to this
     * persistence context, and has not been marked for removal, it is
     * itself ignored, but the operation still cascades, and it is
     * returned directly.
     * @param entity  a new, managed, or detached entity instance
     * @return the managed instance that the state was merged to
     * @throws IllegalArgumentException if the instance is not an entity
     *         or is a removed entity
     * @throws TransactionRequiredException if there is no transaction
     *         when invoked on a container-managed entity manager of
     *         that is of type {@link PersistenceContextType#TRANSACTION}
     */
    <T> T merge(T entity);

    /**
     * Mark a managed entity instance as removed, resulting in its deletion
     * from the database when the persistence context is synchronized with
     * the database. This operation cascades to every entity related by an
     * association marked {@link CascadeType#REMOVE cascade=REMOVE}. If the
     * given entity instance is already removed, it is ignored. If the
     * given entity is new, it is itself ignored, but the operation still
     * cascades.
     * @param entity  a managed, new, or removed entity instance
     * @throws IllegalArgumentException if the instance is not an entity
     *         or is a detached entity
     * @throws TransactionRequiredException if invoked on a
     *         container-managed entity manager of type
     *         {@link PersistenceContextType#TRANSACTION} and there is
     *         no transaction
     */
    void remove(Object entity);
    
    /**
     * Find by primary key.
     * Search for an entity of the specified class and primary key.
     * If the entity instance is contained in the persistence context,
     * it is returned from there.
     * @param entityClass  entity class
     * @param primaryKey  primary key
     * @return the found entity instance or null if the entity does
     *         not exist
     * @throws IllegalArgumentException if the first argument does
     *         not denote an entity type or if the second argument is
     *         not a valid type for that entity's primary key or is
     *         null
     */
    <T> T find(Class<T> entityClass, Object primaryKey);
    
    /**
     * Find by primary key, using the specified properties. 
     * Search for an entity of the specified class and primary key. 
     * If the entity instance is contained in the persistence 
     * context, it is returned from there. 
     * If a vendor-specific property or hint is not recognized, 
     * it is silently ignored. 
     * @param entityClass  entity class
     * @param primaryKey   primary key
     * @param properties  standard and vendor-specific properties 
     *        and hints
     * @return the found entity instance or null if the entity does
     *         not exist
     * @throws IllegalArgumentException if the first argument does 
     *         not denote an entity type or if the second argument
     *         is not a valid type for that entity's primary key or
     *         is null
     * @since 2.0
     */
    <T> T find(Class<T> entityClass, Object primaryKey,
               Map<String, Object> properties);
    
    /**
     * Find by primary key and obtain the given lock type for the
     * resulting entity. Search for an entity of the specified class and
     * primary key, and lock it with respect to the specified lock type.
     * If the entity instance is contained in the persistence context,
     * it is returned from there, and the effect of this method is the
     * same as if the {@link #lock} method had been called on the entity.
     * <p> If the entity is found within the persistence context and
     * the lock mode type is pessimistic and the entity has a version
     * attribute, the persistence provider must perform optimistic
     * version checks when obtaining the database lock. If these checks
     * fail, the {@link OptimisticLockException} is thrown.
     * <p>If the lock mode type is pessimistic and the entity instance
     * is found but cannot be locked:
     * <ul>
     * <li>the {@link PessimisticLockException} is thrown if the
     *     database locking failure causes transaction-level rollback
     * <li>the {@link LockTimeoutException} is thrown if the database
     *     locking failure causes only statement-level rollback
     * </ul>
     * @param entityClass  entity class
     * @param primaryKey  primary key
     * @param lockMode  lock mode
     * @return the found entity instance or null if the entity does
     *         not exist
     * @throws IllegalArgumentException if the first argument does
     *         not denote an entity type or the second argument is
     *         not a valid type for that entity's primary key or
     *         is null
     * @throws TransactionRequiredException if there is no 
     *         transaction and a lock mode other than {@code NONE} is
     *         specified or if invoked on an entity manager which has
     *         not been joined to the current transaction and a lock
     *         mode other than {@code NONE} is specified
     * @throws OptimisticLockException if the optimistic version 
     *         check fails
     * @throws PessimisticLockException if pessimistic locking 
     *         fails and the transaction is rolled back
     * @throws LockTimeoutException if pessimistic locking fails and
     *         only the statement is rolled back
     * @throws PersistenceException if an unsupported lock call is made
     * @since 2.0
     */
    <T> T find(Class<T> entityClass, Object primaryKey,
               LockModeType lockMode);

    /**
     * Find by primary key and lock the entity, using the specified
     * properties. Search for an entity of the specified class and
     * primary key, and lock it with respect to the specified lock type.
     * If the entity instance is contained in the persistence context,
     * it is returned from there.  
     * <p> If the entity is found within the persistence context and
     * the lock mode type is pessimistic and the entity has a version
     * attribute, the persistence provider must perform optimistic
     * version checks when obtaining the database lock. If these checks
     * fail, the {@link OptimisticLockException} is thrown.
     * <p>If the lock mode type is pessimistic and the entity instance
     * is found but cannot be locked:
     * <ul>
     * <li>the {@link PessimisticLockException} is thrown if the
     *     database locking failure causes transaction-level rollback
     * <li>the {@link LockTimeoutException} is thrown if the database
     *     locking failure causes only statement-level rollback
     * </ul>
     * <p>If a vendor-specific property or hint is not recognized, 
     * it is silently ignored.  
     * <p>Portable applications should not rely on the standard
     * timeout hint. Depending on the database in use and the locking
     * mechanisms used by the provider, the hint may or may not be
     * observed.
     * @param entityClass  entity class
     * @param primaryKey  primary key
     * @param lockMode  lock mode
     * @param properties  standard and vendor-specific properties
     *        and hints
     * @return the found entity instance or null if the entity does
     *         not exist
     * @throws IllegalArgumentException if the first argument does
     *         not denote an entity type or the second argument is
     *         not a valid type for that entity's primary key or
     *         is null
     * @throws TransactionRequiredException if there is no 
     *         transaction and a lock mode other than {@code NONE} is
     *         specified or if invoked on an entity manager which has
     *         not been joined to the current transaction and a lock
     *         mode other than {@code NONE} is specified
     * @throws OptimisticLockException if the optimistic version check
     *         fails
     * @throws PessimisticLockException if pessimistic locking fails
     *         and the transaction is rolled back
     * @throws LockTimeoutException if pessimistic locking fails and
     *        only the statement is rolled back
     * @throws PersistenceException if an unsupported lock call is made
     * @since 2.0
     */
    <T> T find(Class<T> entityClass, Object primaryKey,
               LockModeType lockMode,
               Map<String, Object> properties);

    /**
     * Find an instance of the given entity class by primary key,
     * using the specified {@linkplain FindOption options}.
     * Search for an entity with the specified class and primary key.
     * If the given options include a {@link LockModeType}, lock it
     * with respect to the specified lock type.
     * If the entity instance is contained in the persistence context,
     * it is returned from there.
     * <p>If the entity is found within the persistence context and
     * the lock mode type is pessimistic and the entity has a version
     * attribute, the persistence provider must perform optimistic
     * version checks when obtaining the database lock.  If these checks
     * fail, the {@code OptimisticLockException} is thrown.
     * <p>If the lock mode type is pessimistic and the entity instance
     * is found but cannot be locked:
     * <ul>
     * <li>the {@code PessimisticLockException} is thrown if the
     *     database locking failure causes transaction-level
     *     rollback
     * <li>the {@code LockTimeoutException} is thrown if the database
     *     locking failure causes only statement-level rollback
     * </ul>
     * <p>If a vendor-specific {@linkplain FindOption option} is not
     * recognized, it is silently ignored.
     * <p>Portable applications should not rely on the standard
     * {@linkplain Timeout timeout option}. Depending on the database
     * in use and the locking mechanisms used by the provider, this
     * option may or may not be observed.
     * @param entityClass  entity class
     * @param primaryKey  primary key
     * @param options  standard and vendor-specific options
     * @return the found entity instance or null if the entity does
     *         not exist
     * @throws IllegalArgumentException if there are contradictory
     *         options, if the first argument does not denote an entity
     *         type belonging to the persistence unit, or if the second
     *         argument is not a valid non-null instance of the entity
     *         primary key type
     * @throws TransactionRequiredException if there is no transaction
     *         and a lock mode other than {@code NONE} is
     *         specified or if invoked on an entity manager which has
     *         not been joined to the current transaction and a lock
     *         mode other than {@code NONE} is specified
     * @throws OptimisticLockException if the optimistic version check
     *         fails
     * @throws PessimisticLockException if pessimistic locking fails
     *         and the transaction is rolled back
     * @throws LockTimeoutException if pessimistic locking fails and
     *         only the statement is rolled back
     * @throws PersistenceException if an unsupported lock call is made
     * @since 3.2
     */
    <T> T find(Class<T> entityClass, Object primaryKey,
               FindOption... options);

    /**
     * Find an instance of the root entity of the given {@link EntityGraph}
     * by primary key, using the specified {@linkplain FindOption options},
     * and interpreting the {@code EntityGraph} as a load graph.
     * Search for an entity with the specified type and primary key.
     * If the given options include a {@link LockModeType}, lock it
     * with respect to the specified lock type.
     * If the entity instance is contained in the persistence context,
     * it is returned from there.
     * <p> If the entity is found within the persistence context and
     * the lock mode type is pessimistic and the entity has a version
     * attribute, the persistence provider must perform optimistic
     * version checks when obtaining the database lock. If these checks
     * fail, the {@code OptimisticLockException} is thrown.
     * <p>If the lock mode type is pessimistic and the entity instance
     * is found but cannot be locked:
     * <ul>
     * <li>the {@link PessimisticLockException} is thrown if the
     *     database locking failure causes transaction-level rollback
     * <li>the {@link LockTimeoutException} is thrown if the database
     *     locking failure causes only statement-level rollback
     * </ul>
     * <p>If a vendor-specific {@linkplain FindOption option} is not
     * recognized, it is silently ignored.
     * <p>Portable applications should not rely on the standard
     * {@linkplain Timeout timeout option}. Depending on the database
     * in use and the locking mechanisms used by the provider, this
     * option may or may not be observed.
     * @param entityGraph  entity graph interpreted as a load graph
     * @param primaryKey  primary key
     * @param options  standard and vendor-specific options
     * @return the found entity instance or null if the entity does
     *         not exist
     * @throws IllegalArgumentException if there are contradictory
     *         options, if the first argument does not denote an entity
     *         type belonging to the persistence unit, or if the second
     *         argument is not a valid non-null instance of the entity
     *         primary key type
     * @throws TransactionRequiredException if there is no transaction
     *         and a lock mode other than {@code NONE} is
     *         specified or if invoked on an entity manager which has
     *         not been joined to the current transaction and a lock
     *         mode other than {@code NONE} is specified
     * @throws OptimisticLockException if the optimistic version check
     *         fails
     * @throws PessimisticLockException if pessimistic locking fails
     *         and the transaction is rolled back
     * @throws LockTimeoutException if pessimistic locking fails and
     *         only the statement is rolled back
     * @throws PersistenceException if an unsupported lock call is made
     * @since 3.2
     */
    <T> T find(EntityGraph<T> entityGraph, Object primaryKey,
               FindOption... options);

    /**
     * Obtain a reference to an instance of the given entity class
     * with the given primary key, whose state may be lazily fetched.
     * <p>If the requested instance does not exist in the database,
     * the {@link EntityNotFoundException} is thrown when the
     * instance state is first accessed.
     * (The persistence provider runtime is permitted but not
     * required to throw the {@code EntityNotFoundException} when
     * {@code getReference()} is called.)
     * <p>This operation allows the application to create an
     * association to an entity without loading its state from the
     * database.
     * <p>The application should not expect the instance state to
     * be available upon detachment, unless it was accessed by the
     * application while the entity manager was open.
     * @param entityClass  entity class
     * @param primaryKey  primary key
     * @return a reference to the entity instance
     * @throws IllegalArgumentException if the first argument does
     *         not denote an entity type or the second argument is
     *         not a valid type for that entity's primary key or
     *         is null
     * @throws EntityNotFoundException if the entity state cannot
     *         be accessed
     */
    <T> T getReference(Class<T> entityClass, Object primaryKey);

    /**
     * Obtain a reference to an instance of the entity class of the
     * given object, with the same primary key as the given object,
     * whose state may be lazily fetched. The given object may be
     * persistent or detached, but may be neither new nor removed.
     * <p>If the requested instance does not exist in the database,
     * the {@link EntityNotFoundException} is thrown when the
     * instance state is first accessed.
     * (The persistence provider runtime is permitted but not
     * required to throw the {@code EntityNotFoundException} when
     * {@code getReference()} is called.)
     * <p>This operation allows the application to create an
     * association to an entity without loading its state from the
     * database.
     * <p>The application should not expect the instance state to
     * be available upon detachment, unless it was accessed by the
     * application while the entity manager was open.
     * @param entity  a persistent or detached entity instance
     * @return a reference to the entity instance
     * @throws IllegalArgumentException if the given object is not
     *         an entity, or if it is neither persistent nor detached
     * @throws EntityNotFoundException if the entity state cannot be
     *         accessed
     * @since 3.2
     */
    <T> T getReference(T entity);

    /**
     * Synchronize changes held in the persistence context to the
     * underlying database.
     * @throws TransactionRequiredException if there is
     *        no transaction or if the entity manager has not been
     *        joined to the current transaction
     * @throws PersistenceException if the flush fails
     */
    void flush();

    /**
     * Set the {@linkplain FlushModeType flush mode} that applies to
     * all objects contained in the persistence context.
     * @param flushMode  flush mode
     */
    void setFlushMode(FlushModeType flushMode);

    /**
     * Get the {@linkplain FlushModeType flush mode} that applies to
     * all objects contained in the persistence context.
     * @return the current {@link FlushModeType}
     */
    FlushModeType getFlushMode();

    /**
     * Lock an entity instance belonging to the persistence context,
     * obtaining the specified {@linkplain LockModeType lock mode}.
     * <p>If a pessimistic lock mode type is specified and the entity
     * contains a version attribute, the persistence provider must 
     * also perform optimistic version checks when obtaining the 
     * database lock. If these checks fail, the
     * {@link OptimisticLockException} is thrown.
     * <p>If the lock mode type is pessimistic and the entity instance
     * is found but cannot be locked:
     * <ul>
     * <li>the {@link PessimisticLockException} is thrown if the
     *     database locking failure causes transaction-level rollback
     * <li>the {@link LockTimeoutException} is thrown if the database
     *     locking failure causes only statement-level rollback
     * </ul>
     * @param entity  a managed entity instance
     * @param lockMode  lock mode
     * @throws IllegalArgumentException if the instance is not an
     *         entity or is a detached entity
     * @throws TransactionRequiredException if there is no 
     *         transaction or if invoked on an entity manager which
     *         has not been joined to the current transaction
     * @throws EntityNotFoundException if the entity does not exist 
     *         in the database when pessimistic locking is
     *         performed
     * @throws OptimisticLockException if the optimistic version check
     *         fails
     * @throws PessimisticLockException if pessimistic locking fails 
     *         and the transaction is rolled back
     * @throws LockTimeoutException if pessimistic locking fails and
     *         only the statement is rolled back
     * @throws PersistenceException if an unsupported lock call is made
     */
    void lock(Object entity, LockModeType lockMode);

    /**
     * Lock an entity instance belonging to the persistence context,
     * obtaining the specified {@linkplain LockModeType lock mode},
     * using the specified properties.
     * <p>If a pessimistic lock mode type is specified and the entity
     * contains a version attribute, the persistence provider must 
     * also perform optimistic version checks when obtaining the 
     * database lock. If these checks fail, the
     * {@link OptimisticLockException} is thrown.
     * <p>If the lock mode type is pessimistic and the entity instance
     * is found but cannot be locked:
     * <ul>
     * <li>the {@link PessimisticLockException} is thrown if the
     *     database locking failure causes transaction-level rollback
     * <li>the {@link LockTimeoutException} ia thrown if the database
     *     locking failure causes only statement-level rollback
     * </ul>
     * <p>If a vendor-specific property or hint is not recognized, 
     * it is silently ignored.  
     * <p>Portable applications should not rely on the standard
     * timeout hint. Depending on the database in use and the locking
     * mechanisms used by the provider, the hint may or may not be
     * observed.
     * @param entity  a managed entity instance
     * @param lockMode  lock mode
     * @param properties  standard and vendor-specific properties
     *        and hints
     * @throws IllegalArgumentException if the instance is not an
     *         entity or is a detached entity
     * @throws TransactionRequiredException if there is no 
     *         transaction or if invoked on an entity manager which
     *         has not been joined to the current transaction
     * @throws EntityNotFoundException if the entity does not exist 
     *         in the database when pessimistic locking is performed
     * @throws OptimisticLockException if the optimistic version check
     *         fails
     * @throws PessimisticLockException if pessimistic locking fails 
     *         and the transaction is rolled back
     * @throws LockTimeoutException if pessimistic locking fails and
     *         only the statement is rolled back
     * @throws PersistenceException if an unsupported lock call is made
     * @since 2.0
     */
    void lock(Object entity, LockModeType lockMode,
              Map<String, Object> properties);

    /**
     * Lock an entity instance belonging to the persistence context,
     * obtaining the specified {@linkplain LockModeType lock mode},
     * using the specified {@linkplain LockOption options}.
     * <p>If a pessimistic lock mode type is specified and the entity
     * contains a version attribute, the persistence provider must
     * also perform optimistic version checks when obtaining the
     * database lock. If these checks fail, the
     * {@link OptimisticLockException} is thrown.
     * <p>If the lock mode type is pessimistic and the entity instance
     * is found but cannot be locked:
     * <ul>
     * <li>the {@link PessimisticLockException} is thrown if the
     *     database locking failure causes transaction-level rollback
     * <li>the {@link LockTimeoutException} is thrown if the database
     *     locking failure causes only statement-level rollback
     * </ul>
     * <p>If a vendor-specific {@link LockOption} is not recognized,
     * it is silently ignored.
     * <p>Portable applications should not rely on the standard
     * {@linkplain Timeout timeout option}. Depending on the database
     * in use and the locking mechanisms used by the provider, the
     * option may or may not be observed.
     * @param entity  a managed entity instance
     * @param lockMode  lock mode
     * @param options  standard and vendor-specific options
     * @throws IllegalArgumentException if the instance is not an
     *         entity or is a detached entity
     * @throws TransactionRequiredException if there is no
     *         transaction or if invoked on an entity manager which
     *         has not been joined to the current transaction
     * @throws EntityNotFoundException if the entity does not exist
     *         in the database when pessimistic locking is
     *         performed
     * @throws OptimisticLockException if the optimistic version
     *         check fails
     * @throws PessimisticLockException if pessimistic locking fails
     *         and the transaction is rolled back
     * @throws LockTimeoutException if pessimistic locking fails and
     *         only the statement is rolled back
     * @throws PersistenceException if an unsupported lock call is made
     * @since 3.2
     */
    void lock(Object entity, LockModeType lockMode,
              LockOption... options);

    /**
     * Refresh the state of the given managed entity instance from
     * the database, overwriting unflushed changes made to the entity,
     * if any. This operation cascades to every entity related by an
     * association marked {@link CascadeType#REFRESH cascade=REFRESH}.
     * @param entity  a managed entity instance
     * @throws IllegalArgumentException if the instance is not
     *         an entity or the entity is not managed
     * @throws TransactionRequiredException if there is no
     *         transaction when invoked on a container-managed
     *         entity manager of type
     *         {@link PersistenceContextType#TRANSACTION}
     * @throws EntityNotFoundException if the entity no longer
     *        exists in the database
     */
    void refresh(Object entity);

    /**
     * Refresh the state of the given managed entity instance from
     * the database, using the specified properties, and overwriting
     * unflushed changes made to the entity, if any. This operation
     * cascades to every entity related by an association marked
     * {@link CascadeType#REFRESH cascade=REFRESH}.
     * <p>If a vendor-specific property or hint is not recognized,
     * it is silently ignored. 
     * @param entity  a managed entity instance
     * @param properties  standard and vendor-specific properties 
     *        and hints
     * @throws IllegalArgumentException if the instance is not 
     *         an entity or the entity is not managed
     * @throws TransactionRequiredException if there is no
     *         transaction when invoked on a container-managed
     *         entity manager of type
     *         {@link PersistenceContextType#TRANSACTION}
     * @throws EntityNotFoundException if the entity no longer 
     *         exists in the database
     * @since 2.0
     */
    void refresh(Object entity,
                 Map<String, Object> properties);

    /**
     * Refresh the state of the given managed entity instance from
     * the database, overwriting unflushed changes made to the entity,
     * if any, and obtain the given {@linkplain LockModeType lock mode}.
     * This operation cascades to every entity related by an association
     * marked {@link CascadeType#REFRESH cascade=REFRESH}.
     * <p>If the lock mode type is pessimistic and the entity instance
     * is found but cannot be locked:
     * <ul>
     * <li>the {@link PessimisticLockException} is thrown if the
     *     database locking failure causes transaction-level rollback
     * <li>the {@link LockTimeoutException} is thrown if the database
     *     locking failure causes only statement-level rollback.
     * </ul>
     * @param entity  a managed entity instance
     * @param lockMode  lock mode
     * @throws IllegalArgumentException if the instance is not an entity
     *         or if the entity is not managed
     * @throws TransactionRequiredException if invoked on a 
     *         container-managed entity manager of type
     *         {@link PersistenceContextType#TRANSACTION} when there is
     *         no transaction; if invoked on an extended entity manager
     *         when there is no transaction and a lock mode other than
     *         {@link LockModeType#NONE} was specified; or if invoked
     *         on an extended entity manager that has not been joined
     *         to the current transaction and any lock mode other than
     *         {@code NONE} was specified
     * @throws EntityNotFoundException if the entity no longer exists
     *         in the database
     * @throws PessimisticLockException if pessimistic locking fails
     *         and the transaction is rolled back
     * @throws LockTimeoutException if pessimistic locking fails and
     *         only the statement is rolled back
     * @throws PersistenceException if an unsupported lock call is made
     * @since 2.0
     */
    void refresh(Object entity, LockModeType lockMode);

    /**
     * Refresh the state of the given managed entity instance from
     * the database, overwriting unflushed changes made to the entity,
     * if any, and obtain the given {@linkplain LockModeType lock mode},
     * using the specified properties. This operation cascades to every
     * entity related by an association marked {@link CascadeType#REFRESH
     * cascade=REFRESH}.
     * <p>If the lock mode type is pessimistic and the entity instance
     * is found but cannot be locked:
     * <ul>
     * <li>the {@link PessimisticLockException} is thrown if the
     *     database locking failure causes transaction-level rollback
     * <li>the {@link LockTimeoutException} is thrown if the database
     *     locking failure causes only statement-level rollback.
     * </ul>
     * <p>If a vendor-specific property or hint is not recognized, 
     * it is silently ignored.
     * <p>Portable applications should not rely on the standard
     * timeout hint. Depending on the database in use and the locking
     * mechanisms used by the provider, the hint may or may not be
     * observed.
     * @param entity  a managed entity instance
     * @param lockMode  lock mode
     * @param properties  standard and vendor-specific properties
     *        and hints
     * @throws IllegalArgumentException if the instance is not an
     *         entity or if the entity is not managed
     * @throws TransactionRequiredException if invoked on a 
     *         container-managed entity manager of type
     *         {@link PersistenceContextType#TRANSACTION} when there is
     *         no transaction; if invoked on an extended entity manager
     *         when there is no transaction and a lock mode other than
     *         {@link LockModeType#NONE} was specified; or if invoked
     *         on an extended entity manager that has not been joined
     *         to the current transaction and any lock mode other than
     *         {@code NONE} was specified
     * @throws EntityNotFoundException if the entity no longer exists
     *        in the database
     * @throws PessimisticLockException if pessimistic locking fails
     *        and the transaction is rolled back
     * @throws LockTimeoutException if pessimistic locking fails and
     *        only the statement is rolled back
     * @throws PersistenceException if an unsupported lock call is made
     * @since 2.0
     */
    void refresh(Object entity, LockModeType lockMode,
                 Map<String, Object> properties);

    /**
     * Refresh the state of the given managed entity instance from the
     * database, using the specified {@linkplain RefreshOption options},
     * overwriting changes made to the entity, if any. If the supplied
     * options include a {@link LockModeType}, lock the given entity,
     * obtaining the given lock mode. This operation cascades to every
     * entity related by an association marked {@link CascadeType#REFRESH
     * cascade=REFRESH}.
     * <p>If the lock mode type is pessimistic and the entity instance is
     * found but cannot be locked:
     * <ul>
     * <li>the {@link PessimisticLockException} is thrown if the
     *     database locking failure causes transaction-level rollback
     * <li>the {@link LockTimeoutException} is thrown if the database
     *     locking failure causes only statement-level rollback.
     * </ul>
     * <p>If a vendor-specific {@link RefreshOption} is not recognized,
     * it is silently ignored.
     * <p>Portable applications should not rely on the standard
     * {@linkplain Timeout timeout option}. Depending on the database in
     * use and the locking mechanisms used by the provider, the hint may
     * or may not be observed.
     * @param entity  a managed entity instance
     * @param options  standard and vendor-specific options
     * @throws IllegalArgumentException if the instance is not an entity
     *         or if the entity is not managed
     * @throws TransactionRequiredException if invoked on a
     *         container-managed entity manager of type
     *         {@link PersistenceContextType#TRANSACTION} when there is
     *         no transaction; if invoked on an extended entity manager
     *         when there is no transaction and a lock mode other than
     *         {@link LockModeType#NONE} was specified; or if invoked
     *         on an extended entity manager that has not been joined
     *         to the current transaction and any lock mode other than
     *         {@code NONE} was specified
     * @throws EntityNotFoundException if the entity no longer exists in
     *         the database
     * @throws PessimisticLockException if pessimistic locking fails and
     *         the transaction is rolled back
     * @throws LockTimeoutException if pessimistic locking fails and only
     *         the statement is rolled back
     * @throws PersistenceException if an unsupported lock call is made
     * @since 3.2
     */
    void refresh(Object entity,
                 RefreshOption... options);

    /**
     * Clear the persistence context, causing all managed entities to
     * become detached. Changes made to entities that have not already
     * been flushed to the database will never be made persistent.
     */
    void clear();

    /**
     * Evict the given managed or removed entity from the persistence
     * context, causing the entity to become immediately detached.
     * Unflushed changes made to the entity, if any, including deletion
     * of the entity, will never be synchronized to the database.
     * Managed entities which reference the given entity continue to
     * reference it. This operation cascades to every entity related by
     * an association marked {@link CascadeType#DETACH cascade=DETACH}.
     * If the given entity instance is new or detached, that is, if it
     * is not associated with this persistence context, it is ignored.
     * @param entity  a managed or removed entity instance
     * @throws IllegalArgumentException if the instance is not an 
     *         entity
     * @since 2.0
     */
    void detach(Object entity);

    /**
     * Determine if the given object is a managed entity instance
     * belonging to the current persistence context.
     * @param entity  entity instance
     * @return boolean value indicating if entity belongs to the
     *         persistence context
     * @throws IllegalArgumentException if not an entity
     */
    boolean contains(Object entity);

    /**
     * Get the current {@linkplain LockModeType lock mode} held by
     * this persistence context on the given managed entity instance.
     * @param entity  a managed entity instance
     * @return the lock mode currently held
     * @throws TransactionRequiredException if there is no active
     *         transaction or if the entity manager has not been
     *         joined to the current transaction
     * @throws IllegalArgumentException if a transaction is active
     *         but the given instance is not a managed entity
     * @since 2.0
     */
    LockModeType getLockMode(Object entity);

    /**
     * Set the default {@linkplain CacheRetrieveMode cache retrieval
     * mode} for this persistence context.
     * @param cacheRetrieveMode cache retrieval mode
     * @since 3.2
     */
    void setCacheRetrieveMode(CacheRetrieveMode cacheRetrieveMode);

    /**
     * Set the default {@linkplain CacheStoreMode cache storage mode}
     * for this persistence context.
     * @param cacheStoreMode cache storage mode
     * @since 3.2
     */
    void setCacheStoreMode(CacheStoreMode cacheStoreMode);

    /**
     * The cache retrieval mode for this persistence context.
     * @since 3.2
     */
    CacheRetrieveMode getCacheRetrieveMode();

    /**
     * The cache storage mode for this persistence context.
     * @since 3.2
     */
    CacheStoreMode getCacheStoreMode();

    /**
     * Set an entity manager property or hint. 
     * If a vendor-specific property or hint is not recognized, it is
     * silently ignored. 
     * @param propertyName name of the property or hint
     * @param value  value for the property or hint
     * @throws IllegalArgumentException if the property or hint name
     *         is recognized by the implementation, but the second
     *         argument is not valid value
     * @since 2.0
     */
    void setProperty(String propertyName, Object value);

    /**
     * The properties and hints and their associated values which are
     * in effect for this entity manager. Modifying the contents of
     * the returned map does not change the configuration in effect.
     * @return a map of properties and hints currently in effect
     * @since 2.0
     */
    Map<String, Object> getProperties();

    /**
     * Create an instance of {@link Query} for executing a
     * Jakarta Persistence query language statement.
     * @param qlString a Jakarta Persistence query string
     * @return the new query instance
     * @throws IllegalArgumentException if the query string is
     *         found to be invalid
     */
    Query createQuery(String qlString);

    /**
     * Create an instance of {@link TypedQuery} for executing a
     * criteria query.
     * @param criteriaQuery  a criteria query object
     * @return the new query instance
     * @throws IllegalArgumentException if the criteria query is
     *         found to be invalid
     * @since 2.0
     */
    <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery);

    /**
     * Create an instance of {@link TypedQuery} for executing a
     * criteria query, which may be a union or intersection of
     * top-level queries.
     * @param selectQuery  a criteria query object
     * @return the new query instance
     * @throws IllegalArgumentException if the criteria query is
     *         found to be invalid
     * @since 3.2
     */
    <T> TypedQuery<T> createQuery(CriteriaSelect<T> selectQuery);

    /**
     * Create an instance of {@link Query} for executing a criteria
     * update query.
     * @param updateQuery  a criteria update query object
     * @return the new query instance
     * @throws IllegalArgumentException if the update query is
     *         found to be invalid
     * @since 2.1
     */
    Query createQuery(CriteriaUpdate<?> updateQuery);

    /**
     * Create an instance of {@link Query} for executing a criteria
     * delete query.
     * @param deleteQuery  a criteria delete query object
     * @return the new query instance
     * @throws IllegalArgumentException if the delete query is
     *         found to be invalid
     * @since 2.1
     */
    Query createQuery(CriteriaDelete<?> deleteQuery);

    /**
     * Create an instance of {@link TypedQuery} for executing a
     * Jakarta Persistence query language statement.
     * The select list of the query must contain only a single
     * item, which must be assignable to the type specified by
     * the {@code resultClass} argument.
     * @param qlString a Jakarta Persistence query string
     * @param resultClass the type of the query result
     * @return the new query instance
     * @throws IllegalArgumentException if the query string is
     *         found to be invalid or if the query result is
     *         found to not be assignable to the specified type
     * @since 2.0
     */
    <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass);

    /**
     * Create an instance of {@link Query} for executing a named
     * query written in the Jakarta Persistence query language or
     * in native SQL.
     * @param name the name of a query defined in metadata
     * @return the new query instance
     * @throws IllegalArgumentException if a query has not been
     *         defined with the given name or if the query string is
     *         found to be invalid
     * @see NamedQuery
     * @see NamedNativeQuery
     */
    Query createNamedQuery(String name);

    /**
     * Create an instance of {@link TypedQuery} for executing a
     * Jakarta Persistence query language named query.
     * The select list of the query must contain only a single
     * item, which must be assignable to the type specified by
     * the {@code resultClass} argument.
     * @param name the name of a query defined in metadata
     * @param resultClass the type of the query result
     * @return the new query instance
     * @throws IllegalArgumentException if a query has not been
     *         defined with the given name or if the query string is
     *         found to be invalid or if the query result is found to
     *         not be assignable to the specified type
     * @since 2.0
     */
    <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass);

    /**
     * Create an instance of {@link TypedQuery} for executing a
     * named query written in the Jakarta Persistence query
     * language or in native SQL.
     * @param reference a reference to the query defined in metadata
     * @return the new query instance
     * @throws IllegalArgumentException if a query has not been
     *         defined, or if the query string is found to be
     *         invalid, or if the query result is found to not be
     *         assignable to the specified type
     * @see EntityManagerFactory#getNamedQueries(Class)
     * @see NamedQuery
     * @see NamedNativeQuery
     */
    <T> TypedQuery<T> createQuery(TypedQueryReference<T> reference);

    /**
     * Create an instance of {@link Query} for executing a native
     * SQL statement, e.g., for update or delete.
     *
     * <p>If the query is not an update or delete query, query
     * execution will result in each row of the SQL result being
     * returned as a result of type {@code Object[]} (or a result
     * of type {@code Object} if there is only one column in the
     * select list.) Column values are returned in the order of
     * their occurrence in the select list and default JDBC type
     * mappings are applied.
     * @param sqlString a native SQL query string
     * @return the new query instance
     */
    Query createNativeQuery(String sqlString);

    /**
     * Create an instance of {@link Query} for executing a native
     * SQL query.
     *
     * <p><em>In the next release of this API, the return type of this
     * method will change to {@code TypedQuery<T>}.</em>
     * @param sqlString a native SQL query string
     * @param resultClass the type of the query result
     * @return the new query instance
     */
    <T> Query createNativeQuery(String sqlString, Class<T> resultClass);

    /**
     * Create an instance of {@link Query} for executing
     * a native SQL query.
     * @param sqlString a native SQL query string
     * @param resultSetMapping the name of the result set mapping
     * @return the new query instance
     */
    Query createNativeQuery(String sqlString, String resultSetMapping);

    /**
     * Create an instance of {@link StoredProcedureQuery} for executing
     * a stored procedure in the database.
     * <p>Parameters must be registered before the stored procedure can
     * be executed.
     * <p>If the stored procedure returns one or more result sets, any
     * result set is returned as a list of type {@code Object[]}.
     * @param name name assigned to the stored procedure query in
     *        metadata
     * @return the new stored procedure query instance
     * @throws IllegalArgumentException if no query has been defined
     *         with the given name
     * @since 2.1
     */
    StoredProcedureQuery createNamedStoredProcedureQuery(String name);

    /**
     * Create an instance of {@link StoredProcedureQuery} for executing a
     * stored procedure in the database.
     * <p>Parameters must be registered before the stored procedure can
     * be executed.
     * <p>If the stored procedure returns one or more result sets, any
     * result set is returned as a list of type {@code Object[]}.
     * @param procedureName name of the stored procedure in the database
     * @return the new stored procedure query instance
     * @throws IllegalArgumentException if a stored procedure of the
     *         given name does not exist (or if query execution will
     *         fail)
     * @since 2.1
     */
    StoredProcedureQuery createStoredProcedureQuery(String procedureName);

    /**
     * Create an instance of {@link StoredProcedureQuery} for executing
     * a stored procedure in the database.
     * <p>Parameters must be registered before the stored procedure can
     * be executed.
     * <p>The {@code resultClass} arguments must be specified in the
     * order in which the result sets is returned by the stored procedure
     * invocation.
     * @param procedureName name of the stored procedure in the database
     * @param resultClasses classes to which the result sets
     *        produced by the stored procedure are to be mapped
     * @return the new stored procedure query instance
     * @throws IllegalArgumentException if a stored procedure of the
     *         given name does not exist (or if query execution will
     *         fail)
     * @since 2.1
     */
    StoredProcedureQuery createStoredProcedureQuery(
            String procedureName, Class<?>... resultClasses);

    /**
     * Create an instance of {@link StoredProcedureQuery} for executing
     * a stored procedure in the database.
     * <p>Parameters must be registered before the stored procedure can
     * be executed.
     * <p>The {@code resultSetMapping} arguments must be specified in
     * the order in which the result sets is returned by the stored
     * procedure invocation.
     * @param procedureName name of the stored procedure in the
     *        database
     * @param resultSetMappings the names of the result set mappings
     *        to be used in mapping result sets
     *        returned by the stored procedure
     * @return the new stored procedure query instance
     * @throws IllegalArgumentException if a stored procedure or
     *         result set mapping of the given name does not exist
     *         (or the query execution will fail)
     */
    StoredProcedureQuery createStoredProcedureQuery(
            String procedureName, String... resultSetMappings);

    /**
     * Indicate to the entity manager that a JTA transaction is
     * active and join the persistence context to it. 
     * <p>This method should be called on a JTA application 
     * managed entity manager that was created outside the scope
     * of the active transaction or on an entity manager of type
     * {@link SynchronizationType#UNSYNCHRONIZED} to associate
     * it with the current JTA transaction.
     * @throws TransactionRequiredException if there is no active
     *         transaction
     */
    void joinTransaction();

    /**
     * Determine whether the entity manager is joined to the
     * current transaction. Returns false if the entity manager
     * is not joined to the current transaction or if no
     * transaction is active.
     * @return boolean
     * @since 2.1
     */
    boolean isJoinedToTransaction();

    /**
     * Return an object of the specified type to allow access to
     * a provider-specific API. If the provider implementation
     * of {@code EntityManager} does not support the given type,
     * the {@link PersistenceException} is thrown.
     * @param cls  the class of the object to be returned.
     *             This is usually either the underlying class
     *             implementing {@code EntityManager} or an
     *             interface it implements.
     * @return an instance of the specified class
     * @throws PersistenceException if the provider does not 
     *         support the given type
     * @since 2.0
     */
    <T> T unwrap(Class<T> cls);

    /**
     * Return the underlying provider object for the
     * {@link EntityManager}, if available. The result of this
     * method is implementation-specific.
     * <p>The {@code unwrap} method is to be preferred for new
     * applications.
     * @return the underlying provider object
     */
    Object getDelegate();

    /**
     * Close an application-managed entity manager.
     * <p>After invocation of {@code close()}, every method of
     * the {@code EntityManager} instance and of any instance
     * of {@link Query}, {@link TypedQuery}, or
     * {@link StoredProcedureQuery} obtained from it throws
     * the {@link IllegalStateException}, except for
     * {@link #getProperties()}, {@link #getTransaction()},
     * and {@link #isOpen()} (which returns false).
     * <p>If this method is called when the entity manager is
     * joined to an active transaction, the persistence context
     * remains managed until the transaction completes.
     * @throws IllegalStateException if the entity manager is
     *         container-managed
     */
    void close();

    /**
     * Determine whether the entity manager is open. 
     * @return true until the entity manager has been closed
     */
    boolean isOpen();

    /**
     * Return the resource-level {@link EntityTransaction} object.
     * The {@code EntityTransaction} instance may be used serially
     * to begin and commit multiple transactions.
     * @return EntityTransaction instance
     * @throws IllegalStateException if invoked on a JTA entity
     *         manager
     */
    EntityTransaction getTransaction();

    /**
     * The {@linkplain EntityManagerFactory entity manager factory}
     * which created this entity manager.
     * @return the {@link EntityManagerFactory}
     * @throws IllegalStateException if the entity manager has 
     *         been closed
     * @since 2.0
     */
    EntityManagerFactory getEntityManagerFactory();

    /**
     * Obtain an instance of {@link CriteriaBuilder} which may be
     * used to construct {@link CriteriaQuery} objects.
     * @return an instance of {@link CriteriaBuilder}
     * @throws IllegalStateException if the entity manager has
     *         been closed
     * @see EntityManagerFactory#getCriteriaBuilder()
     * @since 2.0
     */
    CriteriaBuilder getCriteriaBuilder();

    /**
     * Obtain an instance of the {@link Metamodel} interface which
     * provides access to metamodel objects describing the managed
     * types belonging to the persistence unit.
     * @return an instance of {@link Metamodel}
     * @throws IllegalStateException if the entity manager has
     *         been closed
     * @since 2.0
     */
    Metamodel getMetamodel();

    /**
     * Create a new mutable {@link EntityGraph}, allowing dynamic
     * definition of an entity graph.
     * @param rootType class of entity graph
     * @return entity graph
     * @since 2.1
     */
    <T> EntityGraph<T> createEntityGraph(Class<T> rootType);

    /**
     * Obtain a mutable copy of a named {@link EntityGraph}, or
     * return null if there is no entity graph with the given
     * name.
     * @param graphName name of an entity graph
     * @return entity graph
     * @since 2.1
     */
    EntityGraph<?> createEntityGraph(String graphName);

    /**
     * Obtain a named {@link EntityGraph}. The returned instance
     * of {@code EntityGraph} should be considered immutable.
     * @param graphName  name of an existing entity graph
     * @return named entity graph
     * @throws IllegalArgumentException if there is no entity
     *         of graph with the given name
     * @since 2.1
     */
    EntityGraph<?> getEntityGraph(String graphName);

    /**
     * Return all named {@link EntityGraph}s that are defined for
     * the given entity class type.
     * @param entityClass  entity class
     * @return list of all entity graphs defined for the entity
     * @throws IllegalArgumentException if the class is not an entity
     * @since 2.1
     */
    <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass);

    /**
     * Execute the given action using the database connection underlying this
     * {@code EntityManager}. Usually, the connection is a JDBC connection, but a
     * provider might support some other native connection type, and is not required
     * to support {@code java.sql.Connection}. If this {@code EntityManager} is
     * associated with a transaction, the action is executed in the context of the
     * transaction. The given action should close any resources it creates, but should
     * not close the connection itself, nor commit or roll back the transaction. If
     * the given action throws an exception, the persistence provider must mark the
     * transaction for rollback.
     * @param action the action
     * @param <C> the connection type, usually {@code java.sql.Connection}
     * @throws PersistenceException wrapping the checked {@link Exception} thrown by
     *         {@link ConnectionConsumer#accept}, if any
     * @since 3.2
     */
    <C> void runWithConnection(ConnectionConsumer<C> action);

    /**
     * Call the given function and return its result using the database connection
     * underlying this {@code EntityManager}. Usually, the connection is a JDBC
     * connection, but a provider might support some other native connection type,
     * and is not required to support {@code java.sql.Connection}. If this
     * {@code EntityManager} is associated with a transaction, the function is
     * executed in the context of the transaction. The given function should close
     * any resources it creates, but should not close the connection itself, nor
     * commit or roll back the transaction. If the given action throws an exception,
     * the persistence provider must mark the transaction for rollback.
     * @param function the function
     * @param <C> the connection type, usually {@code java.sql.Connection}
     * @param <T> the type of result returned by the function
     * @return the value returned by {@link ConnectionFunction#apply}.
     * @throws PersistenceException wrapping the checked {@link Exception} thrown by
     *         {@link ConnectionFunction#apply}, if any
     * @since 3.2
     */
    <C,T> T callWithConnection(ConnectionFunction<C, T> function);

}
