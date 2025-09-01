/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate;

import com.blazebit.persistence.integration.hibernate.base.TableNameFormatter;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.env.spi.QualifiedObjectNameFormatter;
import org.hibernate.mapping.Table;

/**
 *
 * @author Christian Beikov
 * @since 1.6.7
 */
public class NativeTableNameFormatter implements TableNameFormatter {

    private final QualifiedObjectNameFormatter formatter;

    public NativeTableNameFormatter(QualifiedObjectNameFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public String getQualifiedTableName(Dialect dialect, Table table) {
        return formatter.format(table.getQualifiedTableName(), dialect);
    }
}
