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

package com.blazebit.persistence.testsuite.base;

import org.hibernate.dialect.MySQL5InnoDBDialect;

import java.sql.Types;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class SaneMySQLDialect extends MySQL5InnoDBDialect {

    public SaneMySQLDialect() {
        registerColumnType( Types.TIME, "time(6)" );
        registerColumnType( Types.TIMESTAMP, "datetime(6)" );
    }

    @Override
    public String getTableTypeString() {
        return " ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE utf8mb3_bin";
    }
}
