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
public interface InitialValueAttributeAccessor extends AttributeAccessor {

    public Object getMutableStateValue(Object view);

    public Object getInitialValue(Object view);

    public void setInitialValue(Object view, Object initialValue);
}
