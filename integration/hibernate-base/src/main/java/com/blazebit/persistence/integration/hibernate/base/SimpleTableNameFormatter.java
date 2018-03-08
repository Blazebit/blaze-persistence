package com.blazebit.persistence.integration.hibernate.base;
/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
