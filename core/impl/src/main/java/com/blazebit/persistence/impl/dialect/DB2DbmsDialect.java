package com.blazebit.persistence.impl.dialect;

import com.blazebit.persistence.spi.DbmsDialect;

public class DB2DbmsDialect implements DbmsDialect {

	@Override
	public boolean supportWithClause() {
		return true;
	}

	@Override
	public boolean supportNonRecursiveWithClause() {
		return true;
	}

	@Override
	public String getWithClause(boolean recursive) {
		return "with";
	}

}
