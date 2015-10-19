package com.blazebit.persistence.impl.dialect;

import com.blazebit.persistence.impl.function.CyclicUnsignedCounter;
import com.blazebit.persistence.spi.OrderByElement;

public class MySQLDbmsDialect extends DefaultDbmsDialect {
    
    private static final ThreadLocal<CyclicUnsignedCounter> threadLocalCounter = new ThreadLocal<CyclicUnsignedCounter>() {

        @Override
        protected CyclicUnsignedCounter initialValue() {
            return new CyclicUnsignedCounter(-1);
        }

    };

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

    @Override
    public boolean supportsUnion(boolean all) {
        return true;
    }

    @Override
    public boolean supportsIntersect(boolean all) {
        return false;
    }

    @Override
    public boolean supportsExcept(boolean all) {
        return false;
    }

    /**
     * Uses a workaround for limit in IN predicates because of an limitation of MySQL.
     * See http://dev.mysql.com/doc/refman/5.0/en/subquery-restrictions.html for reference.
     *
     */
    @Override
    public void appendLimit(StringBuilder sqlSb, boolean isSubquery, String limit, String offset) {
        if (isSubquery) {
            // Insert it after the open bracket
            sqlSb.insert(1, "SELECT * FROM (");
        }
        
        if (offset == null) {
            sqlSb.append(" limit ").append(limit);
        } else {
            // NOTE: this requires that the parameter value is pulled into the query as literal
            sqlSb.append(" limit ").append(offset).append(',').append(limit);
        }
        
        if (isSubquery) {
            String limitSubqueryAlias = "_tmp_" + threadLocalCounter.get().incrementAndGet();
            sqlSb.append(") as ").append(limitSubqueryAlias);
        }
    }
    
    @Override
    protected void appendOrderByElement(StringBuilder sqlSb, OrderByElement element) {
        appendEmulatedOrderByElementWithNulls(sqlSb, element);
    }

}
