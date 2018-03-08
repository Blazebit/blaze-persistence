/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

        if (offset != null) {
            sqlSb.insert(returningIndex, " offset " + offset);
        }
        if (limit != null) {
            sqlSb.insert(returningIndex, " limit " + limit);
        }
    }

}
