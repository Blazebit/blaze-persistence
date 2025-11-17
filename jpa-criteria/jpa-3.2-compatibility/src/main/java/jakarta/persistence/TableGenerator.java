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
//     Lukas Jungmann  - 2.2
//     Linda DeMichiel - 2.1
//     Linda DeMichiel - 2.0

package jakarta.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;

/**
 * Defines a primary key generator that may be referenced
 * by name when a generator element is specified for the
 * {@link GeneratedValue} annotation. A table generator
 * may be specified on the entity class or on the primary
 * key field or property. The scope of the generator name
 * is global to the persistence unit (across all generator
 * types).
 *
 * <p> If no name is explicitly specified, and the annotation
 * occurs on an entity class or primary key attribute of an
 * entity class, then the name defaults to the name of the
 * entity.
 *
 * <p>If no name is explicitly specified, and the annotation occurs
 * on a package descriptor, then the annotation defines a recipe for
 * producing a default generator when a {@link GeneratedValue}
 * annotation of any program element in the annotated package has
 * {@link GeneratedValue#strategy strategy=TABLE} and a defaulted
 * {@linkplain GeneratedValue#generator generator name}. The name of
 * this default generator is the defaulted generator name, and its
 * other properties are determined by the members of the package
 * {@code TableGenerator} annotation.
 *
 * <p>Example 1:
 * {@snippet :
 * @Entity
 * public class Employee {
 *     ...
 *     @TableGenerator(
 *         name = "empGen",
 *         table = "ID_GEN",
 *         pkColumnName = "GEN_KEY",
 *         valueColumnName = "GEN_VALUE",
 *         pkColumnValue = "EMP_ID",
 *         allocationSize = 1)
 *     @Id
 *     @GeneratedValue(strategy = TABLE, generator = "empGen")
 *     int id;
 *     ...
 * }
 * }
 *
 * <p>Example 2:
 * {@snippet :
 * @Entity
 * public class Address {
 *     ...
 *     @TableGenerator(
 *         table = "ID_GEN",
 *         pkColumnName = "GEN_KEY",
 *         valueColumnName = "GEN_VALUE",
 *         pkColumnValue = "ADDR_ID")
 *     @Id
 *     @GeneratedValue(strategy = TABLE)
 *     int id;
 *     ...
 * }
 * }
 *
 * @see GeneratedValue
 *
 * @since 1.0
 */
@Repeatable(TableGenerators.class)
@Target({TYPE, METHOD, FIELD, PACKAGE})
@Retention(RUNTIME)
public @interface TableGenerator {

    /** 
     * (optional) A unique generator name that can be referenced
     * by one or more classes to be the generator for id values.
     * <p> Defaults to the name of the entity when the annotation
     * occurs on an entity class or primary key attribute.
     */
    String name() default "";

    /** 
     * (Optional) Name of the table that stores the generated id
     * values.
     * <p> Defaults to a name chosen by persistence provider.
     */
    String table() default "";

    /**
     * (Optional) The catalog of the table.
     * <p> Defaults to the default catalog.
     */
    String catalog() default "";

    /** (Optional) The schema of the table. 
     * <p> Defaults to the default schema for user.
     */
    String schema() default "";

    /** 
     * (Optional) Name of the primary key column in the table.
     * <p> Defaults to a provider-chosen name.
     */
    String pkColumnName() default "";

    /** 
     * (Optional) Name of the column that stores the last value
     * generated.
     * <p> Defaults to a provider-chosen name.
     */
    String valueColumnName() default "";

    /**
     * (Optional) The primary key value in the generator table 
     * that distinguishes this set of generated values from
     * others that may be stored in the table.
     * <p> Defaults to a provider-chosen value to store in the 
     * primary key column of the generator table
     */
    String pkColumnValue() default "";

    /** 
     * (Optional) The initial value to be used to initialize the
     * column that stores the last value generated.
     */
    int initialValue() default 0;

    /**
     * (Optional) The amount to increment by when allocating id 
     * numbers from the generator.
     */
    int allocationSize() default 50;

    /**
     * (Optional) Unique constraints that are to be placed on
     * the table. These are only used if table generation is in
     * effect. These constraints apply in addition to primary key
     * constraints.
     * <p> Defaults to no additional constraints.
     */
    UniqueConstraint[] uniqueConstraints() default {};

    /**
     * (Optional) Indexes for the table. These are only used if
     * table generation is in effect. Note that it is not necessary
     * to specify an index for a primary key, as the primary key
     * index is created automatically.
     *
     * @since 2.1 
     */
    Index[] indexes() default {};

    /**
     * (Optional) A SQL fragment appended to the generated DDL
     * statement which creates this table.
     *
     * @since 3.2
     */
    String options() default "";
}
