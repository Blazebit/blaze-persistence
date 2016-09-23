package com.blazebit.persistence.impl.dialect;

import com.blazebit.persistence.spi.DbmsLimitHandler;
import com.blazebit.persistence.spi.OrderByElement;
import com.blazebit.persistence.spi.ValuesStrategy;

import java.util.HashMap;
import java.util.Map;

public class MySQLDbmsDialect extends DefaultDbmsDialect {

    public MySQLDbmsDialect() {
        super(getSqlTypes());
    }

    private static Map<Class<?>, String> getSqlTypes() {
        Map<Class<?>, String> types = new HashMap<Class<?>, String>();

        types.put(String.class, "longtext");

        return types;
    }

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

    @Override
    public DbmsLimitHandler createLimitHandler() {
        return new MySQLDbmsLimitHandler();
    }

    @Override
    protected void appendOrderByElement(StringBuilder sqlSb, OrderByElement element) {
        appendEmulatedOrderByElementWithNulls(sqlSb, element);
    }

    @Override
    public ValuesStrategy getValuesStrategy() {
        return ValuesStrategy.SELECT_UNION;
    }

    @Override
    public boolean needsCastParameters() {
        return false;
    }

}
