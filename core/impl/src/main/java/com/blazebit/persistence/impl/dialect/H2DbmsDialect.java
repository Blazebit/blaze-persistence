package com.blazebit.persistence.impl.dialect;

public class H2DbmsDialect extends DefaultDbmsDialect {

    @Override
    public boolean supportsReturningAllGeneratedKeys() {
        return false;
    }
    
	@Override
	public boolean supportsWithClause() {
		return true;
	}

	@Override
	public boolean supportsNonRecursiveWithClause() {
		return false;
	}

	@Override
	public String getWithClause(boolean recursive) {
		return "with recursive";
	}

    @Override
    public boolean usesWithClauseAfterInsert() {
        return true;
    }

    @Override
    public boolean supportsWithClauseInModificationQuery() {
        return false;
    }

}
