/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.view.impl.EntityViewListenerFactory;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface MappingReader {
    public MetamodelBootContext getContext();

    public ViewMapping readViewMapping(Class<?> entityViewClass);

    public void readViewListenerMapping(Class<?> entityViewListenerClass, EntityViewListenerFactory<?> factory);
}
