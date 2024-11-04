/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate.base;

import org.hibernate.mapping.Table;
import org.hibernate.service.Service;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface Database extends Service {

    public Table getTable(String name);

}
