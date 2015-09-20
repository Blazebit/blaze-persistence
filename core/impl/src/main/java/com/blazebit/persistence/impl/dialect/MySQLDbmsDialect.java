package com.blazebit.persistence.impl.dialect;

import com.blazebit.persistence.spi.DbmsDialect;

public class MySQLDbmsDialect implements DbmsDialect {

	@Override
	public boolean supportWithClause() {
		return false;
	}

	@Override
	public boolean supportNonRecursiveWithClause() {
		return false;
	}

	@Override
	public String getWithClause(boolean recursive) {
		throw new UnsupportedOperationException("With clause is not supported!");
	}

}
