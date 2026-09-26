/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.dialect;

import org.junit.Assert;
import org.junit.Test;

/**
 * Regression tests for Oracle CTE + pagination (#2091).
 * <p>
 * The limit wrapper must keep a leading {@code WITH} clause outside of the {@code ROWNUM}
 * subselect and must not generate a mismatched number of limit placeholders vs bound values.
 */
public class OracleDbmsLimitHandlerTest {

    private final OracleDbmsLimitHandler handler = new OracleDbmsLimitHandler();

    @Test
    public void limitOnlyKeepsWithOutsideRownumWrapper() {
        String sql = "with cte AS (select 1 as id from dual) select id from t order by id";
        String result = handler.applySqlInlined(sql, false, 3, null);

        Assert.assertTrue("WITH must remain at the start of the statement", result.startsWith("with cte"));
        Assert.assertTrue(result.contains("rownum <= 3") || result.contains("rownum<=3"));
        Assert.assertFalse("WITH must not be nested inside select * from (",
                result.matches("(?is)select \\* from \\(\\s*with.*"));
    }

    @Test
    public void limitAndOffsetKeepsWithOutsideRownumWrapper() {
        String sql = "with GroupCTE(id) AS (select e.id from Entity e) select g.id from Group g join GroupCTE c on g.id = c.id order by g.id";
        String result = handler.applySqlInlined(sql, false, 3, 2);

        Assert.assertTrue("WITH must remain at the start of the statement", startsWithIgnoreCase(result, "with "));
        // Inclusive upper bound is limit + offset = 5; lower bound is offset = 2
        Assert.assertTrue("Upper ROWNUM bound should be limit+offset", result.contains("rownum <= 5") || result.contains("rownum<=5"));
        Assert.assertTrue("Offset filter should use rownum_", result.contains("rownum_ > 2") || result.contains("rownum_>2"));
        Assert.assertFalse("WITH must not be nested inside the outer select * from (",
                result.matches("(?is)select \\* from \\(\\s*with.*")
                        || result.matches("(?is)select .* from \\(\\s*select row_\\.\\*.*from \\(\\s*with.*"));
        // Variable form must not use (?+?) which previously bound limit+offset incorrectly
        Assert.assertFalse(result.contains("(?+?)"));
        Assert.assertFalse(result.contains("(3+2)"));
    }

    @Test
    public void variableLimitAndOffsetPlaceholdersMatchBindCount() {
        String sql = "with cte AS (select 1 as id from dual) select id from t order by id";
        String result = handler.applySql(sql, false, 3, 2);

        Assert.assertTrue(startsWithIgnoreCase(result, "with "));
        // Two placeholders: inclusive max row and offset
        int placeholders = countOccurrences(result, '?');
        Assert.assertEquals("Expected two limit/offset placeholders", 2, placeholders);
        Assert.assertTrue(result.contains("rownum <= ?") || result.contains("rownum<=?"));
        Assert.assertTrue(result.contains("rownum_ > ?") || result.contains("rownum_>?"));
        Assert.assertFalse(result.contains("(?+?)"));
    }

    @Test
    public void limitOnlyWithoutWith() {
        String sql = "select id from t order by id";
        String result = handler.applySqlInlined(sql, false, 10, null);
        Assert.assertTrue(result.startsWith("select * from ("));
        Assert.assertTrue(result.contains("rownum <= 10") || result.contains("rownum<=10"));
    }

    private static boolean startsWithIgnoreCase(String value, String prefix) {
        return value.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    private static int countOccurrences(String value, char c) {
        int count = 0;
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }
}
