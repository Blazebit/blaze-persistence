/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.accessor;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface AttributeAccessor {

    public void setValue(Object object, Object value);

    public Object getOrCreateValue(Object object);

    public Object getValue(Object object);
}
