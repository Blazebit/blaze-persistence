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
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static jakarta.persistence.ConstraintMode.PROVIDER_DEFAULT;

/**
 * Specifies a column for joining an entity association or element
 * collection. If the {@code JoinColumn} annotation itself is defaulted,
 * a single join column is assumed and the default values apply.
 *
 * <p>Example:
 * {@snippet :
 * @ManyToOne
 * @JoinColumn(name = "ADDR_ID")
 * public Address getAddress() { return address; }
 * }
 *
 * <p>Example: unidirectional one-to-many association using a foreign key mapping
 * {@snippet :
 * // In Customer class
 * @OneToMany
 * @JoinColumn(name = "CUST_ID") // join column is in the table for Order
 * public Set<Order> getOrders() { return orders; }
 * }
 *
 * @see ManyToOne
 * @see OneToMany
 * @see OneToOne
 * @see JoinTable
 * @see CollectionTable
 * @see ForeignKey
 *
 * @since 1.0
 */
@Repeatable(JoinColumns.class)
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface JoinColumn {

    /** 
     * (Optional) The name of the foreign key column.
     *
     * <p>The table in which this column is found depends on the context.
     * <ul>
     * <li>If the join is for a {@link OneToOne} or {@link ManyToOne}
     *    mapping using a foreign key mapping strategy, the foreign
     *    key column is in the table of the source entity or embeddable.
     * <li>If the join is for a unidirectional {@link OneToMany} mapping
     *    using a foreign key mapping strategy, the foreign key is in
     *    the table of the target entity.
     * <li>If the join is for a {@link ManyToMany} mapping or for a
     *    {@link OneToOne} or bidirectional {@code ManyToOne}/{@code OneToMany}
     *    mapping using a join table, the foreign key is in a join table.
     * <li>If the join is for an {@link ElementCollection}, the foreign
     *    key is in a collection table.
     *</ul>
     *
     * <p>Default (only applies if a single join column is used):
     * The concatenation of the following: the name of the referencing
     * relationship property or field of the referencing entity or
     * embeddable class; "{@code _}"; the name of the referenced primary
     * key column. If there is no such referencing relationship property
     * or field in the entity, or if the join is for an element collection,
     * the join column name is formed as the concatenation of the following:
     * the name of the entity; "{@code _}"; the name of the referenced primary
     * key column.
     */
    String name() default "";

    /**
     * (Optional) The name of the column referenced by this foreign key
     * column.
     * <ul>
     * <li>When used with entity relationship mappings other than the
     *    cases described here, the referenced column is in the table
     *    of the target entity.
     * <li>When used with a unidirectional {@link OneToMany} foreign key
     *    mapping, the referenced column is in the table of the source
     *    entity.
     * <li>When used inside a {@link JoinTable} annotation, the referenced
     *    key column is in the entity table of the owning entity, or
     *    inverse entity if the join is part of the inverse join definition.
     * <li>When used in a {@link CollectionTable} mapping, the referenced
     *    column is in the table of the entity containing the collection.
     * </ul>
     *
     * <p>Default (only applies if single join column is being used):
     * The same name as the primary key column of the referenced table.
     */
    String referencedColumnName() default "";

    /**
     * (Optional) Whether the property is a unique key. This is a
     * shortcut for the {@link UniqueConstraint} annotation at the
     * table level and is useful for when the unique key constraint
     * is only a single field. It is not necessary to explicitly
     * specify this for a join column that corresponds to a primary
     * key that is part of a foreign key.
     */
    boolean unique() default false;

    /**
     * (Optional) Whether the foreign key column is nullable.
     */
    boolean nullable() default true;

    /**
     * (Optional) Whether the column is included in SQL INSERT
     * statements generated by the persistence provider.
     */
    boolean insertable() default true;

    /**
     * (Optional) Whether the column is included in SQL UPDATE
     * statements generated by the persistence provider.
     */
    boolean updatable() default true;

    /**
     * (Optional) The SQL fragment that is used when generating
     * the DDL for the column.
     * <p> Defaults to the generated SQL for the column.
     */
    String columnDefinition() default "";

    /**
     * (Optional) A SQL fragment appended to the generated DDL
     * which declares this column. May not be used in
     * conjunction with {@link #columnDefinition()}.
     *
     * @since 3.2
     */
    String options() default "";

    /**
     * (Optional) The name of the table that contains the column.
     * If a table is not specified, the column is assumed to be
     * in the primary table of the applicable entity.
     *
     * <p> Default: 
     * <ul>
     * <li>If the join is for a {@link OneToOne} or {@link ManyToOne}
     *    mapping using a foreign key mapping strategy, the name of
     *    the table of the source entity or embeddable.
     * <li>If the join is for a unidirectional OneToMany mapping using
     *    a foreign key mapping strategy, the name of the table of the
     *    target entity.
     * <li>If the join is for a {@link ManyToMany} mapping or for a
     *    {@link OneToOne} or bidirectional {@code ManyToOne}/{@code OneToMany}
     *    mapping using a join table, the name of the join table.
     * <li>If the join is for an element collection, the name of the
     *    collection table.
     * </ul>
     */
    String table() default "";

    /**
     * (Optional) Used to specify or control the generation of a
     * foreign key constraint when table generation is in effect.
     * If this element is not specified, a default foreign key
     * strategy is selected by the persistence provider.
     *
     * @since 2.1
     */
    ForeignKey foreignKey() default @ForeignKey(PROVIDER_DEFAULT);

    /**
     * (Optional) Check constraints to be applied to the column.
     * These are only used if table generation is in effect.
     *
     * @since 3.2
     */
    CheckConstraint[] check() default {};

    /**
     * (Optional) A comment to be applied to the column.
     * This is only used if table generation is in effect.
     *
     * @since 3.2
     */
    String comment() default "";
}
