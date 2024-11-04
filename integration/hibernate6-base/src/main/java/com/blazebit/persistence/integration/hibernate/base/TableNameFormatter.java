/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate.base;

import org.hibernate.dialect.Dialect;
import org.hibernate.mapping.Table;

/**
 *
 * @author Christian Beikov
 * @since 1.6.7
 */
public interface TableNameFormatter  {

    public String getQualifiedTableName(Dialect dialect, Table table);

}
