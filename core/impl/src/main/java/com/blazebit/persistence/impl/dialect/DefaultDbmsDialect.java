package com.blazebit.persistence.impl.dialect;

import com.blazebit.persistence.spi.DbmsDialect;

public class DefaultDbmsDialect implements DbmsDialect {

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
		if (recursive) {
			return "with recursive";
		} else {
			return "with";
		}
	}

}
