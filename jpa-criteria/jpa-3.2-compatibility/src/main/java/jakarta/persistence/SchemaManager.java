/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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

package jakarta.persistence;

import java.util.Map;

/**
 * Allows programmatic {@linkplain #create schema creation},
 * {@linkplain #validate schema validation},
 * {@linkplain #truncate data cleanup}, and
 * {@linkplain #drop schema cleanup} for entities belonging
 * to a certain persistence unit.
 * 
 * <p>Properties are inherited from the {@link EntityManagerFactory},
 * that is, they may be specified via {@code persistence.xml} or
 * {@link Persistence#createEntityManagerFactory(String, Map)}.
 *
 * @see EntityManagerFactory#getSchemaManager()
 *
 * @since 3.2
 */
public interface SchemaManager {
	/**
	 * Create database objects mapped by entities belonging to the
	 * persistence unit.
	 *
	 * <p>If a DDL operation fails, the behavior is undefined.
	 * A provider may throw an exception, or it may ignore the problem
	 * and continue.
	 *
	 * @param createSchemas if {@code true}, attempt to create schemas,
	 *                      otherwise, assume the schemas already exist
	 */
	void create(boolean createSchemas);

	/**
	 * Drop database objects mapped by entities belonging to the
	 * persistence unit, undoing the effects of the
	 * {@linkplain #create(boolean) previous creation}.
	 *
	 * <p>If a DDL operation fails, the behavior is undefined.
	 * A provider may throw an exception, or it may ignore the problem
	 * and continue.
	 *
	 * @param dropSchemas if {@code true}, drop schemas,
	 *                    otherwise, leave them be
	 */
	void drop(boolean dropSchemas);

	/**
	 * Validate that the database objects mapped by entities belonging
	 * to the persistence unit have the expected definitions.
	 *
	 * <p>The persistence provider is not required to perform
	 * any specific validation, so the semantics of this operation are
	 * entirely provider-specific.
	 *
	 * @throws SchemaValidationException if a database object is missing or
	 * does not have the expected definition
	 */
	void validate() throws SchemaValidationException;

	/**
	 * Truncate the database tables mapped by entities belonging to
	 * the persistence unit, and then re-import initial data from any
	 * configured SQL scripts for data loading.
	 *
	 * <p>If a SQL operation fails, the behavior is undefined.
	 * A provider may throw an exception, or it may ignore the problem
	 * and continue.
	 */
	void truncate();
}
