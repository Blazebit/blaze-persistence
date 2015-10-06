package com.blazebit.persistence.impl.dialect;

public class DB2DbmsDialect extends DefaultDbmsDialect {

	@Override
	public String getWithClause(boolean recursive) {
		return "with";
	}

	@Override
	public boolean supportsReturningColumns() {
		return true;
	}

    @Override
    public boolean usesWithClauseAfterInsert() {
        return true;
    }

    @Override
    public void appendLimit(StringBuilder sqlSb, String limit, String offset) {
        if (offset == null) {
            sqlSb.append(" fetch first ").append(limit).append(" rows only");
        } else {
            // This requires DB2_COMPATIBILITY_VECTOR=MYS
            // See for reference: https://www.ibm.com/developerworks/community/blogs/SQLTips4DB2LUW/entry/limit_offset?lang=en
            sqlSb.append(" limit ").append(limit).append(" offset ").append(offset);
            // TODO: This is selecting the rownum too...
            // "select * from ( select db2limit2_.*, rownumber() over(order by order of db2limit2_) as rownumber_ from ( ?1 fetch first ?2 rows only ) as db2limit2_ ) as db2limit1_ where rownumber_ > ?3 order by rownumber_"
        }
    }

//	@Override
//	public void applyQueryReturning(StringBuilder sqlSb, String[] returningColumns) {
//		StringBuilder sb = new StringBuilder(100);
//		sb.append("SELECT ");
//		for (int i = 0; i < returningColumns.length; i++) {
//			if (i != 0) {
//				sqlSb.append(',');
//			}
//			sqlSb.append(returningColumns[i]);
//		}
//		sb.append(" FROM FINAL TABLE (");
//		sqlSb.insert(0, sb);
//		sqlSb.append(')');
//	}

}
