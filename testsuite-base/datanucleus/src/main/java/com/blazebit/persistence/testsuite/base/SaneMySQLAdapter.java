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

import org.datanucleus.store.connection.ManagedConnection;
import org.datanucleus.store.rdbms.schema.JDBCTypeInfo;
import org.datanucleus.store.rdbms.schema.RDBMSTypesInfo;
import org.datanucleus.store.rdbms.schema.SQLTypeInfo;
import org.datanucleus.store.schema.StoreSchemaHandler;

import java.sql.DatabaseMetaData;
import java.sql.Types;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class SaneMySQLAdapter extends org.datanucleus.store.rdbms.adapter.MySQLAdapter {
    public SaneMySQLAdapter(DatabaseMetaData metadata) {
        super(metadata);
    }

    public void initialiseTypes(StoreSchemaHandler handler, ManagedConnection mconn) {
        super.initialiseTypes(handler, mconn);

        RDBMSTypesInfo typesInfo = (RDBMSTypesInfo)handler.getSchemaData(mconn.getConnection(), "types", null);

        JDBCTypeInfo jdbcType = (JDBCTypeInfo)typesInfo.getChild(Integer.toString(Types.TIMESTAMP));
        if (jdbcType != null && jdbcType.getNumberOfChildren() > 0) {
            SQLTypeInfo dfltTypeInfo = (SQLTypeInfo)jdbcType.getChild("TIMESTAMP");
            dfltTypeInfo.setTypeName("TIMESTAMP(6)");
        }
        jdbcType = (JDBCTypeInfo)typesInfo.getChild(Integer.toString(Types.TIME));
        if (jdbcType != null && jdbcType.getNumberOfChildren() > 0) {
            SQLTypeInfo dfltTypeInfo = (SQLTypeInfo)jdbcType.getChild("TIME");
            dfltTypeInfo.setTypeName("TIME(6)");
        }
    }
}
