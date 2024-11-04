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
//     Linda DeMichiel - 2.1
//     Linda DeMichiel - 2.0

package jakarta.persistence;

/**
 * Enumerates the kinds of optimistic or pessimistic lock which
 * may be obtained on an entity instance.
 *
 * <p> A specific lock mode may be requested by passing an explicit
 * {@code LockModeType} as an argument to:
 * <ul>
 * <li>one of the methods of {@link EntityManager} which obtains
 *     locks ({@link EntityManager#lock lock()},
 *     {@link EntityManager#find find()}, or
 *     {@link EntityManager#refresh refresh()}), or
 * <li>to {@link Query#setLockMode(LockModeType)} or
 *     {@link TypedQuery#setLockMode(LockModeType)}.
 * </ul>
 * 
 * <p> Optimistic locks are specified using
 * {@link LockModeType#OPTIMISTIC LockModeType.OPTIMISTIC} and
 * {@link LockModeType#OPTIMISTIC_FORCE_INCREMENT}. The lock mode
 * types {@link LockModeType#READ} and {@link LockModeType#WRITE} are
 * synonyms for {@code OPTIMISTIC} and {@code OPTIMISTIC_FORCE_INCREMENT}
 * respectively. The latter are preferred for new applications.
 *
 * <p> The semantics of requesting locks of type
 * {@code LockModeType.OPTIMISTIC} and
 * {@code LockModeType.OPTIMISTIC_FORCE_INCREMENT} are the
 * following.
 *
 * <p> If transaction T1 calls for a lock of type 
 * {@code LockModeType.OPTIMISTIC} on a versioned object, 
 * the entity manager must ensure that neither of the following 
 * phenomena can occur:
 * <ul>
 *   <li> P1 (Dirty read): Transaction T1 modifies a row. 
 * Another transaction T2 then reads that row and obtains the
 * modified value, before T1 has committed or rolled back.
 * Transaction T2 eventually commits successfully; it does not 
 * matter whether T1 commits or rolls back and whether it does 
 * so before or after T2 commits.
 *   </li>
 *   <li> P2 (Non-repeatable read): Transaction T1 reads a row. 
 * Another transaction T2 then modifies or deletes that row, 
 * before T1 has committed. Both transactions eventually commit 
 * successfully.
 *   </li>
 * </ul>
 *
 * <p> Lock modes must always prevent the phenomena P1 and P2.
 *
 * <p> In addition, obtaining a lock of type
 * {@code LockModeType.OPTIMISTIC_FORCE_INCREMENT} on a versioned
 * object, will also force an update (increment) to the entity's
 * version column.
 *
 * <p> The persistence implementation is not required to support
 * the use of optimistic lock modes on non-versioned objects. When
 * it cannot support such a lock request, it must throw the {@link
 * PersistenceException}.
 *
 * <p>The lock modes {@link LockModeType#PESSIMISTIC_READ},
 * {@link LockModeType#PESSIMISTIC_WRITE}, and
 * {@link LockModeType#PESSIMISTIC_FORCE_INCREMENT} are used to
 * immediately obtain long-term database locks.
 *
 * <p> The semantics of requesting locks of type
 * {@code LockModeType.PESSIMISTIC_READ},
 * {@code LockModeType.PESSIMISTIC_WRITE}, and
 * {@code LockModeType.PESSIMISTIC_FORCE_INCREMENT} are the
 * following.
 *
 * <p> If transaction T1 calls for a lock of type
 * {@code LockModeType.PESSIMISTIC_READ} or
 * {@code LockModeType.PESSIMISTIC_WRITE} on an object, the entity
 * manager must ensure that neither of the following phenomena can
 * occur: 
 * <ul> 
 * <li> P1 (Dirty read): Transaction T1 modifies a
 * row. Another transaction T2 then reads that row and obtains the
 * modified value, before T1 has committed or rolled back.
 *
 * <li> P2 (Non-repeatable read): Transaction T1 reads a row.
 * Another transaction T2 then modifies or deletes that row, before
 * T1 has committed or rolled back.
 * </ul>
 *
 * <p> A lock with {@code LockModeType.PESSIMISTIC_WRITE} can be
 * obtained on an entity instance to force serialization among
 * transactions attempting to update the entity data. A lock with
 * {@code LockModeType.PESSIMISTIC_READ} can be used to query data
 * using repeatable-read semantics without the need to reread the
 * data at the end of the transaction to obtain a lock, and without
 * blocking other transactions reading the data. A lock with
 * {@code LockModeType.PESSIMISTIC_WRITE} can be used when querying
 * data and there is a high likelihood of deadlock or update failure
 * among concurrent updating transactions.
 * 
 * <p> The persistence implementation must support the use of locks
 * of type {@code LockModeType.PESSIMISTIC_READ} and
 * {@code LockModeType.PESSIMISTIC_WRITE} with non-versioned entities
 * as well as with versioned entities.
 *
 * <p> When the lock cannot be obtained, and the database locking
 * failure results in transaction-level rollback, the provider must
 * throw the {@link PessimisticLockException} and ensure that the
 * JTA transaction or {@code EntityTransaction} has been marked for
 * rollback.
 * 
 * <p> When the lock cannot be obtained, and the database locking
 * failure results in only statement-level rollback, the provider
 * must throw the {@link LockTimeoutException} (and must not mark
 * the transaction for rollback).
 *
 * @since 1.0
 *
 */
public enum LockModeType implements FindOption, RefreshOption {
    /**
     * Synonymous with {@link #OPTIMISTIC}.
     * <p>
     * {@code OPTIMISTIC} is preferred for new applications.
     *
     */
    READ,

    /**
     * Synonymous with {@link #OPTIMISTIC_FORCE_INCREMENT}.
     * <p>
     * {@code OPTIMISTIC_FORCE_INCREMENT} is preferred for
     * new applications.
     *
     */
    WRITE,

    /**
     * Optimistic lock.
     *
     * @since 2.0
     */
    OPTIMISTIC,

    /**
     * Optimistic lock, with version update.
     *
     * @since 2.0
     */
    OPTIMISTIC_FORCE_INCREMENT,

    /**
     *
     * Pessimistic read lock.
     *
     * @since 2.0
     */
    PESSIMISTIC_READ,

    /**
     * Pessimistic write lock.
     *
     * @since 2.0
     */
    PESSIMISTIC_WRITE,

    /**
     * Pessimistic write lock, with version update.
     *
     * @since 2.0
     */
    PESSIMISTIC_FORCE_INCREMENT,

    /**
     * No lock.
     *
     * @since 2.0
     */
    NONE
}
