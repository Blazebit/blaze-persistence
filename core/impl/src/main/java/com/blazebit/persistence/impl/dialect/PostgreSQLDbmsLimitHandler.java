/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.dialect;

import com.blazebit.persistence.impl.util.BoyerMooreCaseInsensitiveAsciiLastPatternFinder;
import com.blazebit.persistence.impl.util.PatternFinder;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class PostgreSQLDbmsLimitHandler extends DefaultDbmsLimitHandler {

    private static final PatternFinder RETURNING_FINDER = new BoyerMooreCaseInsensitiveAsciiLastPatternFinder("returning ");

    public PostgreSQLDbmsLimitHandler() {
    }

    public PostgreSQLDbmsLimitHandler(int length) {
        super(length);
    }

    @Override
    public void applySql(StringBuilder sqlSb, boolean isSubquery, String limit, String offset) {
        // The RETURNING clause must be the last, so we find the index at which we should insert
        int returningIndex = RETURNING_FINDER.indexIn(sqlSb);
        returningIndex = returningIndex == -1 ? sqlSb.length() : returningIndex - 1;
        if (sqlSb.indexOf(")", returningIndex) >= 0) {
            returningIndex = sqlSb.length();
        }

        if (offset != null) {
            sqlSb.insert(returningIndex, " offset " + offset);
        }
        if (limit != null) {
            sqlSb.insert(returningIndex, " limit " + limit);
        }
    }

}
