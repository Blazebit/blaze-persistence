/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import javax.persistence.Parameter;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface ExtendedParameter<T> extends Parameter<T> {

    public boolean isCollectionValued();

}
