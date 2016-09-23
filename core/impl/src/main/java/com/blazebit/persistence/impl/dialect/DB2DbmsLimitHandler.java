package com.blazebit.persistence.impl.dialect;

import com.blazebit.persistence.impl.util.BoyerMooreCaseInsensitiveAsciiFirstPatternFinder;
import com.blazebit.persistence.impl.util.PatternFinder;
import com.blazebit.persistence.impl.util.SqlUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DB2DbmsLimitHandler extends AbstractDbmsLimitHandler {

    public DB2DbmsLimitHandler() {
        super(40);
    }

    @Override
    public boolean supportsVariableLimit() {
        return false;
    }

    @Override
    public void applySql(StringBuilder sqlSb, boolean isSubquery, String limit, String offset) {
        if (offset != null) {
            // Need to extract aliases and place them in the outer select so we don't screw up subqueries
            int selectIndex = SqlUtils.indexOfSelect(sqlSb);
            String[] aliases = SqlUtils.getSelectItemAliases(sqlSb, selectIndex);
            StringBuilder selectClauseSb = new StringBuilder(60 + aliases.length * 20);
            selectClauseSb.append("select ");

            for (int i = 0; i < aliases.length; i++) {
                selectClauseSb.append(aliases[i]);
                selectClauseSb.append(',');
            }
            selectClauseSb.setCharAt(selectClauseSb.length() - 1, ' ');
            selectClauseSb.append("from ( select inner2_.*, rownumber() over(order by order of inner2_) as rownumber_ from ( ");

            // We know we will need some more
            sqlSb.ensureCapacity(sqlSb.length() + 50 + selectClauseSb.length());
            sqlSb.insert(selectIndex, selectClauseSb);
        } else {
            sqlSb.ensureCapacity(sqlSb.length() + 30);
            sqlSb.append(" fetch first ").append(limit).append(" rows only");
        }

        if (offset != null) {
            if (limit != null) {
                sqlSb.append(" fetch first ");
                sqlSb.append(limit);
                sqlSb.append(" rows only ) as inner2_ ) as inner1_ where rownumber_ > ");
                sqlSb.append(offset);
                sqlSb.append(" order by rownumber_");
            } else {
                sqlSb.append(" ) where rownumber_ > ").append(offset);
            }
        }
    }

    @Override
    public int bindLimitParametersAtEndOfQuery(Integer limit, Integer offset, PreparedStatement statement, int index) throws SQLException {
        // No support for parameters
        return 0;
    }

}
