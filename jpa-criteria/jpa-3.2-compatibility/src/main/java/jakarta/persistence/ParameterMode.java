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

/**
 * Specifies the mode of a parameter of a stored procedure query.
 *
 * @see StoredProcedureQuery
 * @see StoredProcedureParameter
 *
 * @since 2.1
 */
public enum ParameterMode {

    /**
     * Stored procedure input parameter
     */
    IN,

    /**
     * Stored procedure input/output parameter
     */
    INOUT,

    /**
     * Stored procedure output parameter
     */
    OUT,

    /**
     * Stored procedure reference cursor parameter.
     *
     * <p>Some databases use REF_CURSOR parameters to return result
     * sets from stored procedures.
     */
    REF_CURSOR

}
