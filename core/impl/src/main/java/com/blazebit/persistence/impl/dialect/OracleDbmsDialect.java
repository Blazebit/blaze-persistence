package com.blazebit.persistence.impl.dialect;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class OracleDbmsDialect extends DefaultDbmsDialect {
	
	private static final Method registerReturnParameter;
	private static final Method getReturnResultSet;
	
	static {
		Method registerReturnParameterMethod = null;
		Method getReturnResultSetMethod = null;
		try {
			Class<?> clazz = Class.forName("oracle.jdbc.OraclePreparedStatement");
			registerReturnParameterMethod = clazz.getMethod("registerReturnParameter", int.class, int.class);
			getReturnResultSetMethod = clazz.getMethod("getReturnResultSet");
		} catch (Exception e) {
			// Ignore
		}
		
		registerReturnParameter = registerReturnParameterMethod;
		getReturnResultSet = getReturnResultSetMethod;
	}

	@Override
	public boolean supportsQueryReturning() {
		return true;
	}

	@Override
	public void applyQueryReturning(StringBuilder sqlSb, String[] returningColumns) {
		sqlSb.append(" returning ");
		for (int i = 0; i < returningColumns.length; i++) {
			if (i != 0) {
				sqlSb.append(',');
			}
			sqlSb.append(returningColumns[i]);
		}
		sqlSb.append(" into ");
		for (int i = 0; i < returningColumns.length; i++) {
			if (i != 0) {
				sqlSb.append(',');
			}
			sqlSb.append('?');
		}
	}

	@Override
	public void applyQueryReturning(PreparedStatement ps, int[] returningSqlTypes) throws SQLException {
		if (registerReturnParameter == null) {
			throw new IllegalStateException("Could not apply query returning because the class oracle.jdbc.OraclePreparedStatement could not be loaded!");
		}

		try {
			int offset = (ps.getParameterMetaData().getParameterCount() - returningSqlTypes.length) + 1;
			for (int i = 0; i < returningSqlTypes.length; i++) {
				registerReturnParameter.invoke(ps, offset + i, returningSqlTypes[i]);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<Object[]> getQueryReturning(PreparedStatement ps, int[] returningSqlTypes) throws SQLException {
		List<Object[]> results = new ArrayList<Object[]>(1);
		
		try {
			ResultSet rs = (ResultSet) getReturnResultSet.invoke(ps);
			while (rs.next()) {
				Object[] resultRow = new Object[returningSqlTypes.length];
				for (int i = 0; i < returningSqlTypes.length; i++) {
					switch (returningSqlTypes[i]) {
						case Types.BIT:
							boolean b = rs.getBoolean(i);
							resultRow[i] = rs.wasNull() ? null : b;
							break;
						case Types.TINYINT:
							byte by = rs.getByte(i);
							resultRow[i] = rs.wasNull() ? null : by;
							break;
						case Types.SMALLINT:
							short s = rs.getShort(i);
							resultRow[i] = rs.wasNull() ? null : s;
							break;
						case Types.INTEGER:
							int integer = rs.getInt(i);
							resultRow[i] = rs.wasNull() ? null : integer;
							break;
						case Types.BIGINT:
							long l = rs.getLong(i);
							resultRow[i] = rs.wasNull() ? null : l;
							break;
						case Types.REAL:
							float f = rs.getFloat(i);
							resultRow[i] = rs.wasNull() ? null : f;
							break;
						case Types.FLOAT:
						case Types.DOUBLE:
							double d = rs.getDouble(i);
							resultRow[i] = rs.wasNull() ? null : d;
							break;
						case Types.DECIMAL:
						case Types.NUMERIC:
							resultRow[i] = rs.getBigDecimal(i);
							break;
						case Types.CHAR:
						case Types.VARCHAR:
						case Types.LONGVARCHAR:
							resultRow[i] = rs.getString(i);
							break;
						case Types.DATE:
							resultRow[i] = rs.getDate(i);
							break;
						case Types.TIME:
							resultRow[i] = rs.getTime(i);
							break;
						case Types.TIMESTAMP:
							resultRow[i] = rs.getTimestamp(i);
							break;
						case Types.BINARY:
						case Types.VARBINARY:
						case Types.LONGVARBINARY:
							resultRow[i] = rs.getBytes(i);
							break;
						case Types.ARRAY:
							resultRow[i] = rs.getArray(i);
							break;
						default:
							throw new IllegalArgumentException("Unsupported JDBC returning type: " + returningSqlTypes[i]);
					}
				}
				results.add(resultRow);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return results;
	}

}
