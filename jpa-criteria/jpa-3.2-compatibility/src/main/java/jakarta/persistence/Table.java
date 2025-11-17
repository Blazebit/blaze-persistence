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
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies the primary table mapped by the annotated entity type.
 *
 * <p>Additional tables may be specified using {@link SecondaryTable}
 * or {@link SecondaryTables} annotation.
 *
 * <p>If no {@code Table} annotation is specified for an entity class,
 * the default values apply.
 *
 * <p>Example:
 * {@snippet :
 * @Entity
 * @Table(name = "CUST", schema = "RECORDS")
 * public class Customer { ... }
 * }
 *
 * <p>This annotation may not be applied to a class annotated
 * {@link MappedSuperclass} or {@link Embeddable}.
 *
 * @since 1.0
 */
@Target(TYPE) 
@Retention(RUNTIME)
public @interface Table {

    /**
     * (Optional) The name of the table.
     * <p> Defaults to the entity name.
     */
    String name() default "";

    /**
     * (Optional) The catalog of the table.
     * <p> Defaults to the default catalog.
     */
    String catalog() default "";

    /**
     * (Optional) The schema of the table.
     * <p> Defaults to the default schema for user.
     */
    String schema() default "";

    /**
     * (Optional) Unique constraints to be placed on the table.
     * These are only used if table generation is in effect.
     * These constraints apply in addition to any constraints
     * specified by the {@link Column} and {@link JoinColumn}
     * annotations and constraints entailed by primary key mappings.
     * <p> Defaults to no additional constraints.
     */
    UniqueConstraint[] uniqueConstraints() default {};

    /**
     * (Optional) Indexes for the table.
     * These are only used if table generation is in effect.
     * Note that it is not necessary to specify an index for
     * a primary key, as the primary key index is created
     * automatically.
     *
     * @since 2.1
     */
    Index[] indexes() default {};

    /**
     * (Optional) Check constraints to be applied to the table.
     * These are only used if table generation is in effect.
     *
     * @since 3.2
     */
    CheckConstraint[] check() default {};

    /**
     * (Optional) A comment to be applied to the table.
     * This is only used if table generation is in effect.
     *
     * @since 3.2
     */
    String comment() default "";

    /**
     * (Optional) A SQL fragment appended to the generated DDL
     * statement which creates this table. This is only used if
     * table generation is in effect.
     *
     * @since 3.2
     */
    String options() default "";
}
