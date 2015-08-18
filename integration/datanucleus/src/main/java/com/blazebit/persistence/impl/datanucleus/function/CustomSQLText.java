package com.blazebit.persistence.impl.datanucleus.function;

import java.util.List;

import org.datanucleus.store.rdbms.sql.SQLText;
import org.datanucleus.store.rdbms.sql.expression.SQLExpression;

public class CustomSQLText extends SQLText {

	private final String sql;

	public CustomSQLText(String sql, List<SQLExpression> expressions) {
		this.sql = sql;

		for (SQLExpression expression : expressions) {
			append(expression);
		}
	}

	@Override
	public String toSQL() {
		// Call super to trigger parameter handling
		super.toSQL();
		return sql;
	}
	
}
