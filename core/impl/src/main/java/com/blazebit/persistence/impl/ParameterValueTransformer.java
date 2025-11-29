/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import jakarta.persistence.Query;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface ParameterValueTransformer {

    public ParameterValueTransformer forQuery(Query query);

    public Object transform(Object originalValue);

}
