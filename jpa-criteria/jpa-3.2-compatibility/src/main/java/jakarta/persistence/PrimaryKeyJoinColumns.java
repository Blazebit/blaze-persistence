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
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Groups {@link PrimaryKeyJoinColumn} annotations.
 * It is used to map composite foreign keys.
 *
 * <p>Example: {@code ValuedCustomer} subclass
 * {@snippet :
 * @Entity
 * @Table(name = "VCUST")
 * @DiscriminatorValue("VCUST")
 * @PrimaryKeyJoinColumns({
 *     @PrimaryKeyJoinColumn(name = "CUST_ID",
 *                           referencedColumnName = "ID"),
 *     @PrimaryKeyJoinColumn(name = "CUST_TYPE",
 *                           referencedColumnName = "TYPE")})
 * public class ValuedCustomer extends Customer { ... }
 * }
 *
 * @see ForeignKey
 *
 * @since 1.0
 */
@Target({TYPE, METHOD, FIELD})
@Retention(RUNTIME)
public @interface PrimaryKeyJoinColumns {

    /**
     * One or more {@link PrimaryKeyJoinColumn} annotations.
     */
    PrimaryKeyJoinColumn[] value();

    /**
     * (Optional) Used to specify or control the generation of a
     * foreign key constraint when table generation is in effect.
     * If both this element and the {@code foreignKey} element of
     * any of the {@link PrimaryKeyJoinColumn} elements are specified,
     * the behavior is undefined. If no foreign key annotation element
     * is specified in either location, a default foreign key strategy
     * is selected by the persistence provider.
     *
     * @since 2.1
     */
    ForeignKey foreignKey() default @ForeignKey(ConstraintMode.PROVIDER_DEFAULT);

}
