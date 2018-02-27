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

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SQL2008DbmsLimitHandler extends AbstractDbmsLimitHandler {

    public SQL2008DbmsLimitHandler() {
        super(40);
    }

    public SQL2008DbmsLimitHandler(int length) {
        super(length);
    }

    @Override
    public boolean supportsVariableLimit() {
        return true;
    }

    @Override
    public void applySql(StringBuilder sqlSb, boolean isSubquery, String limit, String offset) {
        if (offset != null) {
            sqlSb.append(" offset ").append(offset).append(" rows");
            if (limit != null) {
                sqlSb.append(" fetch next ").append(limit).append(" rows only");
            }
        } else if (limit != null) {
            sqlSb.append(" fetch first ").append(limit).append(" rows only");
        }
    }

    @Override
    public int bindLimitParametersAtEndOfQuery(Integer limit, Integer offset, PreparedStatement statement, int index) throws SQLException {
        if (offset != null) {
            statement.setInt(index, offset);

            if (limit != null) {
                statement.setInt(index + 1, limit);
                return 2;
            }
            return 1;
        } else if (limit != null) {
            statement.setInt(index, limit);
            return 1;
        }

        return 0;
    }

}
