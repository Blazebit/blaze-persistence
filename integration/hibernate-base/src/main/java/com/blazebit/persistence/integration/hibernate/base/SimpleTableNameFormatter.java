package com.blazebit.persistence.integration.hibernate.base;
/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

import org.hibernate.dialect.Dialect;
import org.hibernate.mapping.Table;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SimpleTableNameFormatter implements TableNameFormatter {

    // copied from org.hibernate.boot.model.relational.QualifiedNameParser.NameParts()
    public String getQualifiedTableName(Dialect dialect, Table table) {
        final String catalogName = table.getCatalog();
        final String schemaName = table.getSchema();
        final String objectName = table.getName();

        StringBuilder buff = new StringBuilder();
        if (catalogName != null) {
            buff.append(catalogName.toString()).append('.');
        }

        if (schemaName != null) {
            buff.append(schemaName.toString()).append('.');
        }

        buff.append(objectName.toString());
        return buff.toString();
    }
}
