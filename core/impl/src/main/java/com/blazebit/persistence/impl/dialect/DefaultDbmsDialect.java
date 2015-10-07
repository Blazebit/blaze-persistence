package com.blazebit.persistence.impl.dialect;

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
    public boolean usesWithClauseAfterInsert() {
        return false;
    }

	@Override
    public boolean supportsWithClauseInModificationQuery() {
        return true;
    }

    @Override
    public boolean supportsModificationQueryInWithClause() {
        return false;
    }

    @Override
	public boolean supportsReturningGeneratedKeys() {
		return true;
	}

    @Override
    public boolean supportsReturningAllGeneratedKeys() {
        return true;
    }

	@Override
	public boolean supportsReturningColumns() {
		return false;
	}

    @Override
    public boolean supportsLimit() {
        return true;
    }

    @Override
    public boolean supportsLimitOffset() {
        return true;
    }

    @Override
    public void appendLimit(StringBuilder sqlSb, boolean isSubquery, String limit, String offset) {
        if (offset == null) {
            sqlSb.append(" limit ").append(limit);
        } else {
            sqlSb.append(" limit ").append(limit).append(" offset ").append(offset);
        }
    }

}
