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
 * Specifies the mapping of an association to an intermediate join
 * table. It is applied to the owning side of an association.
 *
 * <p>A join table is typically used in the mapping of many-to-many
 * and unidirectional one-to-many associations. It may also be used
 * to map bidirectional many-to-one/one-to-many associations,
 * unidirectional many-to-one relationships, and one-to-one
 * associations (both bidirectional and unidirectional).
 *
 * <p>When a join table is used in mapping a relationship with an
 * embeddable class on the owning side of the relationship, the
 * containing entity rather than the embeddable class is considered
 * the owner of the relationship.
 *
 * <p>If the {@code JoinTable} annotation is missing, the default
 * values of the annotation elements apply. The name of the join
 * table is assumed to be the table names of the associated primary
 * tables concatenated together (owning side first) using an
 * underscore.
 *
 * <p>Example:
 * {@snippet :
 * @JoinTable(
 *     name = "CUST_PHONE",
 *     joinColumns = @JoinColumn(name = "CUST_ID", referencedColumnName = "ID"),
 *     inverseJoinColumns = @JoinColumn(name = "PHONE_ID", referencedColumnName = "ID"))
 * }
 *
 * <p>This annotation may not be applied to a persistent field or property
 * not annotated {@link ManyToOne}, {@link OneToOne}, {@link ManyToMany},
 * or {@link OneToMany}.
 *
 * @see JoinColumn
 * @see JoinColumns
 *
 * @since 1.0
 */
@Target({METHOD, FIELD}) 
@Retention(RUNTIME)
public @interface JoinTable {

    /**
     * (Optional) The name of the join table. 
     * 
     * <p> Defaults to the concatenated names of the two
     * associated primary entity tables, separated by an
     * underscore.
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
     * (Optional) The foreign key columns of the join table
     * which reference the primary table of the entity owning
     * the association. (I.e. the owning side of the association).
     *
     * <p> Uses the same defaults as for {@link JoinColumn}.
     */
    JoinColumn[] joinColumns() default {};

    /** 
     * (Optional) The foreign key columns of the join table
     * which reference the primary table of the entity that
     * does not own the association.
     * (I.e. the inverse side of the association).
     *
     * <p> Uses the same defaults as for {@link JoinColumn}.
     */
    JoinColumn[] inverseJoinColumns() default {};

    /**
     * (Optional) Used to specify or control the generation of a
     * foreign key constraint for the columns corresponding to the
     * {@link #joinColumns} element when table generation is in
     * effect. If both this element and the {@code foreignKey}
     * element of any of the {@link #joinColumns} elements are
     * specified, the behavior is undefined. If no foreign key
     * annotation element is specified in either location, a
     * default foreign key strategy is selected by the
     * persistence provider.
     *
     * @since 2.1
     */
    ForeignKey foreignKey() default @ForeignKey(ConstraintMode.PROVIDER_DEFAULT);

    /**
     * (Optional) Used to specify or control the generation of a
     * foreign key constraint for the columns corresponding to the
     * {@link #inverseJoinColumns} element when table generation
     * is in effect. If both this element and the {@code foreignKey}
     * element of any of the {@link #inverseJoinColumns} elements
     * are specified, the behavior is undefined. If no foreign key
     * annotation element is specified in either location, a default
     * foreign key strategy is selected by the persistence provider.
     *
     * @since 2.1
     */
    ForeignKey inverseForeignKey() default @ForeignKey(ConstraintMode.PROVIDER_DEFAULT);

    /**
     * (Optional) Unique constraints to be placed on the table.
     * These are only used if table generation is in effect.
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
