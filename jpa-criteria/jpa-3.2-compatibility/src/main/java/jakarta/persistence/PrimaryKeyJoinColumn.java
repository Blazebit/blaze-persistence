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
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static jakarta.persistence.ConstraintMode.PROVIDER_DEFAULT;

/**
 * Specifies a primary key column that is used as a foreign key
 * to join to another table.
 *
 * <p>It is used to join the primary table of an entity subclass
 * in the {@link InheritanceType#JOINED JOINED} mapping strategy 
 * to the primary table of its superclass; it is used within a 
 * {@link SecondaryTable} annotation to join a secondary table 
 * to a primary table; and it may be used in a {@link OneToOne} 
 * mapping in which the primary key of the referencing entity 
 * is used as a foreign key to the referenced entity. 
 *
 * <p>If no {@code PrimaryKeyJoinColumn} annotation is specified
 * for a subclass in the {@code JOINED} mapping strategy, the
 * foreign key columns are assumed to have the same names as the
 * primary key columns of the primary table of the superclass.
 *
 * <p>Example: {@code Customer} and {@code ValuedCustomer} subclass
 * {@snippet :
 * @Entity
 * @Table(name = "CUST")
 * @Inheritance(strategy = JOINED)
 * @DiscriminatorValue("CUST")
 * public class Customer { ... }
 *    
 * @Entity
 * @Table(name = "VCUST")
 * @DiscriminatorValue("VCUST")
 * @PrimaryKeyJoinColumn(name = "CUST_ID")
 * public class ValuedCustomer extends Customer { ... }
 * }
 *
 * @see SecondaryTable
 * @see Inheritance
 * @see OneToOne
 * @see ForeignKey
 *
 * @since 1.0
 */
@Repeatable(PrimaryKeyJoinColumns.class)
@Target({TYPE, METHOD, FIELD})
@Retention(RUNTIME)
public @interface PrimaryKeyJoinColumn {

    /** 
     * (Optional) The name of the primary key column of the current
     * table.
     * <p> Defaults to the same name as the primary key column of
     * the primary table of the superclass ({@code JOINED} mapping
     * strategy); the same name as the primary key column of the
     * primary table ({@link SecondaryTable} mapping); or the same
     * name as the primary key column for the table for the
     * referencing entity ({@link OneToOne} mapping).
     */
    String name() default "";

    /** 
     * (Optional) The name of the primary key column of the table
     * being joined to.
     * <p>Defaults to the same name as the primary key column of
     * the primary table of the superclass ({@code JOINED} mapping
     * strategy); the same name as the primary key column of the
     * primary table ({@link SecondaryTable} mapping); or the same
     * name as the primary key column for the table for the
     * referencing entity ({@link OneToOne} mapping).
     */
    String referencedColumnName() default "";

    /**
     * (Optional) The SQL fragment that is used when generating
     * the DDL for the column. This should not be specified for a
     * {@link OneToOne} primary key association.
     * <p> Defaults to the generated SQL to create a column of the 
     * inferred type.
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
     * (Optional) Used to specify or control the generation of a
     * foreign key constraint for the primary key join column 
     * when table generation is in effect. If this element is not
     * specified, the persistence provider's default foreign key
     * strategy will apply.
     *
     * @since 2.1
     */
    ForeignKey foreignKey() default @ForeignKey(PROVIDER_DEFAULT);
}
