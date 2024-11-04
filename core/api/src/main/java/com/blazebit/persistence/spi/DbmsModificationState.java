/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spi;


/**
 * Specifies the whether entity data in a statement should be before or after a modification CTE ran.
 *
 * @author Christian Beikov
 * @since 1.1.0
 */
public enum DbmsModificationState {

    OLD,
    NEW
    
}
