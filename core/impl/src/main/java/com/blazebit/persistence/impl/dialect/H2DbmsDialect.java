package com.blazebit.persistence.impl.dialect;

import com.blazebit.persistence.spi.DbmsDialect;

public class H2DbmsDialect implements DbmsDialect {

	@Override
	public boolean supportWithClause() {
		return true;
	}

	@Override
	public boolean supportNonRecursiveWithClause() {
		return false;
	}

	@Override
	public String getWithClause(boolean recursive) {
		return "with recursive";
	}

}
