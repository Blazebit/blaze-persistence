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

package com.blazebit.persistence.view.spi;

import javax.persistence.EntityManager;

/**
 * A factory for creating a {@link TransactionAccess}.
 * This is created via the {@link java.util.ServiceLoader} API.
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public interface TransactionAccessFactory {

    /**
     * Creates a transaction access object.
     *
     * @param entityManager The entity manager associated with the transaction.
     * @return The transaction access object
     */
    TransactionAccess createTransactionAccess(EntityManager entityManager);

    /**
     * Returns a priority value that is used to select among multiple implementations.
     * The lower the returned value, the higher the priority.
     *
     * @return the priority value
     */
    int getPriority();
}
