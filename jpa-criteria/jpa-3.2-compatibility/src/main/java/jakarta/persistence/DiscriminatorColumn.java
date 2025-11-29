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
import static jakarta.persistence.DiscriminatorType.STRING;

/**
 * Specifies the discriminator column for the 
 * {@link InheritanceType#SINGLE_TABLE SINGLE_TABLE} and
 * {@link InheritanceType#JOINED JOINED} {@linkplain Inheritance
 * inheritance mapping strategies}.
 * 
 * <p>The mapping strategy and discriminator column are only specified
 * for the root of an entity class hierarchy or subhierarchy in which
 * a different inheritance strategy is applied.
 * 
 * <p>If the {@link DiscriminatorColumn} annotation is missing, and a
 * discriminator column is required, the name of the discriminator
 * column defaults to {@code "DTYPE"} and the discriminator type to
 * {@link DiscriminatorType#STRING}.
 *
 * <p>Example:
 * {@snippet :
 * @Entity
 * @Table(name = "CUST")
 * @Inheritance(strategy = SINGLE_TABLE)
 * @DiscriminatorColumn(name = "DISC", discriminatorType = STRING, length = 20)
 * public class Customer { ... }
 *
 * @Entity
 * public class ValuedCustomer extends Customer { ... }
 * }
 *
 * @see DiscriminatorValue
 * @see DiscriminatorType
 *
 * @since 1.0
 */
@Target({TYPE}) 
@Retention(RUNTIME)
public @interface DiscriminatorColumn {

    /**
     * (Optional) The name of column to be used for the discriminator.
     */
    String name() default "DTYPE";

    /**
     * (Optional) The type of object/column to use as a class discriminator.
     * Defaults to {@link DiscriminatorType#STRING DiscriminatorType.STRING}.
     */
    DiscriminatorType discriminatorType() default STRING;

    /**
     * (Optional) The SQL fragment that is used when generating the DDL 
     * for the discriminator column.
     * <p> Defaults to the provider-generated SQL to create a column 
     * of the specified discriminator type.
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
     * (Optional) The column length for String-based discriminator types. 
     * Ignored for other discriminator types.
     */
    int length() default 31;
}
