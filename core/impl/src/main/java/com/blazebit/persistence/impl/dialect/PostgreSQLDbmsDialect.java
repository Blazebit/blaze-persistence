package com.blazebit.persistence.impl.dialect;

public class PostgreSQLDbmsDialect extends DefaultDbmsDialect {

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
		sqlSb.append(";--");
	}

}
