/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SanePostgreSQLAdapter extends org.datanucleus.store.rdbms.adapter.PostgreSQLAdapter {
    public SanePostgreSQLAdapter(DatabaseMetaData metadata) {
        super(metadata);
    }

    public void initialiseTypes(StoreSchemaHandler handler, ManagedConnection mconn) {
        super.initialiseTypes(handler, mconn);

        RDBMSTypesInfo typesInfo = (RDBMSTypesInfo)handler.getSchemaData(mconn.getConnection(), "types", null);

        // Make use of the normal timestamp type without timezones to have comparable results
        JDBCTypeInfo jdbcType = (JDBCTypeInfo)typesInfo.getChild(Integer.toString(Types.TIMESTAMP));
        if (jdbcType != null && jdbcType.getNumberOfChildren() > 0) {
            SQLTypeInfo dfltTypeInfo = (SQLTypeInfo)jdbcType.getChildren().remove("timestamptz");
            dfltTypeInfo.setTypeName("timestamp");
            jdbcType.addChild(dfltTypeInfo);
        }
    }
}
