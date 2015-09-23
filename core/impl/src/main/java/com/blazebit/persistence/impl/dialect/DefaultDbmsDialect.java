package com.blazebit.persistence.impl.dialect;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.blazebit.persistence.spi.DbmsDialect;

public class DefaultDbmsDialect implements DbmsDialect {

	@Override
	public boolean supportsWithClause() {
		return true;
	}

	@Override
	public boolean supportsNonRecursiveWithClause() {
		return true;
	}

	@Override
	public String getWithClause(boolean recursive) {
		if (recursive) {
			return "with recursive";
		} else {
			return "with";
		}
	}

	@Override
	public boolean supportsJdbcReturning() {
		return true;
	}

	@Override
	public boolean supportsQueryReturning() {
		return false;
	}

	@Override
	public void applyQueryReturning(StringBuilder sqlSb, String[] returningColumns) {
	}

	@Override
	public void applyQueryReturning(PreparedStatement ps, int[] returningSqlTypes) throws SQLException {
	}

	@Override
	public List<Object[]> getQueryReturning(PreparedStatement ps, int[] returningSqlTypes) throws SQLException {
		return null;
	}

}
