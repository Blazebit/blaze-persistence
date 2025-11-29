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
//     Petros Splinakis - 2.2
//     Linda DeMichiel - 2.1
//     Linda DeMichiel - 2.0

package jakarta.persistence;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies a secondary table for the annotated entity class.
 * Specifying one or more secondary tables indicates that the
 * data for the entity class is stored across multiple tables.
 *
 * <p> If no {@code SecondaryTable} annotation is specified, 
 * it is assumed that all persistent fields or properties of
 * the entity are mapped to the primary table. If no primary
 * key join columns are specified, the join columns are assumed
 * to reference the primary key columns of the primary table,
 * and have the same names and types as the referenced primary
 * key columns of the primary table.
 *
 * <p>Example 1: Single secondary table with a single primary key column.
 * {@snippet :
 * @Entity
 * @Table(name = "CUSTOMER")
 * @SecondaryTable(name = "CUST_DETAIL",
 *                 pkJoinColumns = @PrimaryKeyJoinColumn(name = "CUST_ID"))
 * public class Customer { ... }
 * }
 *
 * <p>Example 2: Single secondary table with multiple primary key columns.
 * {@snippet :
 * @Entity
 * @Table(name = "CUSTOMER")
 * @SecondaryTable(name = "CUST_DETAIL",
 *                 pkJoinColumns = {
 *                     @PrimaryKeyJoinColumn(name = "CUST_ID"),
 *                     @PrimaryKeyJoinColumn(name = "CUST_TYPE")})
 * public class Customer { ... }
 * }
 *
 * <p>This annotation may not be applied to a class annotated
 * {@link MappedSuperclass} or {@link Embeddable}.
 *
 * @since 1.0
 */
@Repeatable(SecondaryTables.class)
@Target(TYPE) 
@Retention(RUNTIME)
public @interface SecondaryTable {

    /**
     * (Required) The name of the table.
     */
    String name();

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
     * (Optional) The columns that are used to join with the
     * primary table.
     * <p> Defaults to the column(s) of the same name(s) as
     * the primary key column(s) in the primary table.
     */
    PrimaryKeyJoinColumn[] pkJoinColumns() default {};

    /**
     * (Optional) Used to specify or control the generation of a
     * foreign key constraint for the columns corresponding to the
     * {@link #pkJoinColumns} element when table generation is in
     * effect. If both this element and the {@code #foreignKey}
     * element of any of the {@link #pkJoinColumns} elements are
     * specified, the behavior is undefined. If no foreign key
     * annotation element is specified in either location, a default
     * foreign key strategy is selected by the persistence provider.
     *
     * @since 2.1
     */
    ForeignKey foreignKey() default @ForeignKey(ConstraintMode.PROVIDER_DEFAULT);

    /**
     * (Optional) Unique constraints that are to be placed on the
     * table. These are typically only used if table generation is
     * in effect. These constraints apply in addition to any
     * constraints specified by  {@link Column} and {@link JoinColumn}
     * annotations and constraints entailed by primary key mappings.
     * <p> Defaults to no additional constraints.
     */
    UniqueConstraint[] uniqueConstraints() default {};

    /**
     * (Optional) Indexes for the table.
     * These are only used if table generation is in effect.
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
