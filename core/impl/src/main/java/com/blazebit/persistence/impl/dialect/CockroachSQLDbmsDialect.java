/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.impl.dialect;

import com.blazebit.persistence.spi.OrderByElement;

import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.6.0
 */
public class CockroachSQLDbmsDialect extends PostgreSQLDbmsDialect {

    public CockroachSQLDbmsDialect() {
    }

    public CockroachSQLDbmsDialect(Map<Class<?>, String> childSqlTypes) {
        super(childSqlTypes);
    }

    @Override
    public void appendOrderByElement(StringBuilder sqlSb, OrderByElement element, String[] aliases) {
        if (!element.isNullable()) {
            super.appendOrderByElement(sqlSb, element, aliases);
        } else {
            appendEmulatedOrderByElementWithNulls(sqlSb, element, aliases);
        }
    }
}
