/*
 * Copyright (c) 2011, 2023 Oracle and/or its affiliates. All rights reserved.
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

package jakarta.persistence;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static jakarta.persistence.ConstraintMode.CONSTRAINT;

/**
 * Specifies the handling of foreign key constraints when schema
 * generation is in effect. If this annotation is not specified,
 * a default foreign key strategy is selected by the persistence
 * provider.
 *
 * <p>The {@link ConstraintMode} value is used to specify whether
 * foreign key constraints should be generated.
 *
 * <p>The syntax used in the {@link #foreignKeyDefinition} element
 * should follow the SQL syntax used by the target database for
 * foreign key constraint creation. For example, it might be similar
 * to the following:
 * {@snippet :
 * FOREIGN KEY ( <COLUMN expression> {, <COLUMN expression>} ... )
 *     REFERENCES <TABLE identifier> [
 *         (<COLUMN expression> {, <COLUMN expression>} ... ) ]
 *     [ ON UPDATE <referential action> ]
 *     [ ON DELETE <referential action> ]
 * }
 *
 * <p>When the {@link ConstraintMode} value is
 * {@link ConstraintMode#CONSTRAINT CONSTRAINT}, but the
 * {@link #foreignKeyDefinition} element is not specified, the provider
 * will generate foreign key constraints whose update and delete actions
 * it determines most appropriate for the join column(s) to which the
 * foreign key annotation is applied.
 *
 * @see JoinColumn
 * @see JoinColumns
 * @see MapKeyJoinColumn
 * @see MapKeyJoinColumns
 * @see PrimaryKeyJoinColumn
 * @see JoinTable
 * @see CollectionTable
 * @see SecondaryTable
 * @see AssociationOverride
 *
 * @since 2.1
 */
@Target({})
@Retention(RUNTIME)
public @interface ForeignKey {

    /**
     * (Optional) The name of the foreign key constraint.
     * <p> Defaults to a provider-generated name.
     */
    String name() default "";

    /**
     * (Optional) Used to specify whether a foreign key constraint
     * should be generated when schema generation is in effect.
     * <ul>
     * <li>{@link ConstraintMode#CONSTRAINT} specifies that the
     * persistence provider must generate a foreign key constraint.
     * If the {@link #foreignKeyDefinition} element is not specified,
     * the provider will generate a constraint whose update and
     * delete actions it determines most appropriate for the join
     * column or columns to which the foreign key annotation is
     * applied.
     * <li>{@link ConstraintMode#NO_CONSTRAINT} specifies that no
     * constraint should be generated.
     * <li>{@link ConstraintMode#PROVIDER_DEFAULT} selects the
     * default behavior of the provider, which may or may not
     * result in generation of a constraint.
     * </ul>
     */
    ConstraintMode value() default CONSTRAINT;

    /**
     * (Optional) The foreign key constraint definition.  
     */
    String foreignKeyDefinition() default "";

    /**
     * (Optional) A SQL fragment appended to the generated DDL
     * which creates this foreign key. May not be used in
     * conjunction with {@link #foreignKeyDefinition()}.
     *
     * @since 3.2
     */
    String options() default "";
}
