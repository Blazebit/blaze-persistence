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

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Declares a parameter of a named stored procedure query. All
 * parameters of a named stored procedure query must be declared.
 *
 * @see NamedStoredProcedureQuery
 * @see ParameterMode 
 *
 * @since 2.1
 */
@Target({}) 
@Retention(RUNTIME)
public @interface StoredProcedureParameter { 

    /** 
     * The name of the parameter as defined by the stored procedure
     * in the database. If a name is not specified, it is assumed
     * that the stored procedure uses positional parameters.
     */
    String name() default "";

    /**
     * Specifies whether the parameter is an IN, INOUT, OUT, or
     * REF_CURSOR parameter. REF_CURSOR parameters are used by some
     * databases to return result sets from a stored procedure.
     */
    ParameterMode mode() default ParameterMode.IN;

    /**
     * JDBC type of the parameter.
     */
    Class<?> type();

}
