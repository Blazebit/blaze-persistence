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

package com.blazebit.persistence.integration.hibernate;

import com.blazebit.persistence.integration.hibernate.base.TableNameFormatter;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.env.spi.QualifiedObjectNameFormatter;
import org.hibernate.mapping.Table;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
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
