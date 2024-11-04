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

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Identifies the primary key of an entity.
 *
 * <p>The field or property to which the {@code Id} annotation is
 * applied should have one of the following types:
 * any Java primitive type;
 * any primitive wrapper type;
 * {@link String};
 * {@link java.util.UUID};
 * {@link java.util.Date};
 * {@link java.sql.Date};
 * {@link java.math.BigDecimal};
 * {@link java.math.BigInteger}.
 *
 * <p>The mapped column for the primary key of the entity is assumed 
 * to be the primary key of the primary table. If no {@link Column}
 * annotation is specified, the primary key column name is assumed to
 * be the name of the primary key property or field.
 *
 * <p>Example:
 * {@snippet :
 * @Id
 * public Long getId() { return id; }
 * }
 *
 * @see Column
 * @see GeneratedValue
 * @see EmbeddedId
 * @see PersistenceUnitUtil#getIdentifier(Object)
 *
 * @since 1.0
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Id {}
