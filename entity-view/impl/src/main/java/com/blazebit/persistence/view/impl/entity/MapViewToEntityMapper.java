/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.entity;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface MapViewToEntityMapper {

    public ViewToEntityMapper getKeyMapper();

    public ViewToEntityMapper getValueMapper();
    
}
