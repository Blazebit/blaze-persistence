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

import com.blazebit.persistence.impl.util.SqlUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class DB2DbmsLimitHandler extends AbstractDbmsLimitHandler {

    public DB2DbmsLimitHandler() {
        super(40);
    }

    public DB2DbmsLimitHandler(int length) {
        super(length);
    }

    @Override
    public boolean supportsVariableLimit() {
        return false;
    }

    @Override
    public boolean limitIncludesOffset() {
        return true;
    }

    @Override
    public void applySql(StringBuilder sqlSb, boolean isSubquery, String limit, String offset) {
        final int appendIndex;
        if (offset != null) {
            final int selectIndex = SqlUtils.indexOfSelect(sqlSb);

            // Need to extract aliases and place them in the outer select so we don't screw up subqueries
            String[] aliases = SqlUtils.getSelectItemAliases(sqlSb, selectIndex);
            int[] finalTableSubqueryBounds = SqlUtils.indexOfFinalTableSubquery(sqlSb, selectIndex);

            StringBuilder selectClauseSb = new StringBuilder(80 + aliases.length * 20);
            selectClauseSb.append("select ");
            Map<Integer, String> sequences = null;

            for (int i = 0; i < aliases.length; i++) {
                if (aliases[i].regionMatches(true, 0, "next value for ", 0, 15)) {
                    if (sequences == null) {
                        sequences = new LinkedHashMap<Integer, String>();
                    }
                    sequences.put(i, aliases[i]);
                }
                selectClauseSb.append(aliases[i]);
                selectClauseSb.append(',');
            }
            selectClauseSb.setCharAt(selectClauseSb.length() - 1, ' ');
            selectClauseSb.append("from ( select inner2_.*, rownumber() over(order by order of inner2_) as rownumber_ from ( ");

            // We know we will need some more
            sqlSb.ensureCapacity(sqlSb.length() + 120 + selectClauseSb.length());
            sqlSb.insert(Math.max(selectIndex, finalTableSubqueryBounds[0]), selectClauseSb);
            int removedChars = 0;

            // If there were sequences in the original query, we have to remove them as they are placed on the top level select now
            if (sequences != null) {
                int startIndex = selectIndex + selectClauseSb.length();
                for (Map.Entry<Integer, String> sequenceEntry : sequences.entrySet()) {
                    String sequence = sequenceEntry.getValue();
                    int index = sqlSb.indexOf(sequence, startIndex);

                    if (sequenceEntry.getKey().intValue() == 0) {
                        // If it's the first, seek to the next non-whitespace
                        int currentIdx = index + sequence.length();
                        while (Character.isWhitespace(sqlSb.charAt(currentIdx))) {
                            currentIdx++;
                        }
                        currentIdx++;
                        int diff = currentIdx - index;
                        sqlSb.replace(index, currentIdx, "");
                        startIndex -= diff;
                        removedChars += diff;
                    } else {
                        // If it's not, seek to the previous non-whitespace
                        int currentIdx = index - 1;
                        while (Character.isWhitespace(sqlSb.charAt(currentIdx))) {
                            currentIdx--;
                        }
                        int end = index + sequence.length();
                        int diff = end - currentIdx;
                        sqlSb.replace(currentIdx, end, "");
                        startIndex -= end - currentIdx;
                        removedChars += diff;
                    }
                }
            }

            appendIndex = (finalTableSubqueryBounds[1] + selectClauseSb.length()) - removedChars;
        } else {
            appendIndex = SqlUtils.indexOfFinalTableSubquery(sqlSb, 0)[1];
            StringBuilder limitSb = new StringBuilder(40);
            limitSb.append(" fetch first ").append(limit).append(" rows only");
            sqlSb.insert(appendIndex, limitSb);
        }

        if (offset != null) {
            if (limit != null) {
                StringBuilder limitSb = new StringBuilder(120);
                limitSb.append(" fetch first ");
                limitSb.append(limit);
                limitSb.append(" rows only ) as inner2_ ) as inner1_ where rownumber_ > ");
                limitSb.append(offset);
                limitSb.append(" order by rownumber_");
                sqlSb.insert(appendIndex, limitSb);
            } else {
                StringBuilder limitSb = new StringBuilder(30);
                limitSb.append(" ) where rownumber_ > ").append(offset);
                sqlSb.insert(appendIndex, limitSb);
            }
        }
    }

    @Override
    public int bindLimitParametersAtEndOfQuery(Integer limit, Integer offset, PreparedStatement statement, int index) throws SQLException {
        // No support for parameters
        return 0;
    }

}
