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

package com.blazebit.persistence.view;

/**
 * The lock mode types for updatable entity views.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public enum LockMode {

    /**
     * The automatic lock mode will use optimistic locking if possible or no locking.
     */
    AUTO,
    /**
     * The optimistic locking mode will use the version attribute of an entity to for optimistic locking.
     */
    OPTIMISTIC,
    /**
     * The pessimistic read locking mode will acquire a {@link javax.persistence.LockModeType#PESSIMISTIC_READ} for the entity when reading the entity view.
     * This lock mode is only useful within the bounds of a single transaction as the lock is bound to it.
     */
    PESSIMISTIC_READ,
    /**
     * The pessimistic write locking mode will acquire a {@link javax.persistence.LockModeType#PESSIMISTIC_WRITE} for the entity when reading the entity view.
     * This lock mode is only useful within the bounds of a single transaction as the lock is bound to it.
     */
    PESSIMISTIC_WRITE,
    /**
     * No locking at any point is done.
     */
    NONE;
}
