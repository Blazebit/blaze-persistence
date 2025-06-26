/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate.base;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.hibernate.boot.Metadata;
import org.hibernate.dialect.Dialect;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;
import org.hibernate.type.spi.TypeConfiguration;

/**
 *
 * @author Christian Beikov
 * @since 1.6.7
 */
public class SimpleDatabase implements Database {

    private final Map<String, Table> tables;

    public SimpleDatabase(Iterator<Table> iter, TypeConfiguration typeConfiguration, Dialect dialect, TableNameFormatter formatter, Metadata mapping) {
        Map<String, Table> map = new HashMap<String, Table>();
        while (iter.hasNext()) {
            Table t = iter.next();
            map.put(formatter.getQualifiedTableName(dialect, t), t);
            if (t.getSubselect() != null) {
                map.put("( " + t.getSubselect() + " )", t);
            }
            for (Column column : t.getColumns()) {
                column.getSqlType(mapping);
            }
        }
        this.tables = Collections.unmodifiableMap(map);
    }

    @Override
    public Table getTable(String name) {
        return tables.get(name);
    }
}
