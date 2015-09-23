package com.blazebit.persistence.impl.dialect;

public class MySQLDbmsDialect extends DefaultDbmsDialect {

	@Override
	public boolean supportsWithClause() {
		return false;
	}

	@Override
	public boolean supportsNonRecursiveWithClause() {
		return false;
	}

	@Override
	public String getWithClause(boolean recursive) {
		throw new UnsupportedOperationException("With clause is not supported!");
	}

}
