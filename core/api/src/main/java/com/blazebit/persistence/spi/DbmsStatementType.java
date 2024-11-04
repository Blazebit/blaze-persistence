/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spi;


/**
 * Specifies the type of a simple statement irrespective of set operations or CTEs.
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public enum DbmsStatementType {

    SELECT,
    INSERT,
    UPDATE,
    DELETE
    
}
