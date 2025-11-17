/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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

package jakarta.persistence;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used to specify a SQL check constraint on a column or table
 * when schema generation is in effect.
 *
 * @see Table#check()
 * @see Column#check()
 *
 * @since 3.2
 */
@Target({})
@Retention(RUNTIME)
public @interface CheckConstraint {

    /**
     * (Optional) The name of the constraint.
     * <p> Defaults to a provider-generated name.
     */
    String name() default "";

    /**
     * (Required) The native SQL expression to be checked.
     */
    String constraint();

    /**
     * (Optional) A SQL fragment appended to the generated DDL
     * which creates this constraint.
     *
     * @since 3.2
     */
    String options() default "";
}
