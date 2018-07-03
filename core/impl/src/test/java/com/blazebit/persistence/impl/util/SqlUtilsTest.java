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

package com.blazebit.persistence.impl.util;

import org.junit.Assert;
import org.junit.Test;

public class SqlUtilsTest {

    @Test
    public void indexOfSelect() {
        Assert.assertEquals(0, SqlUtils.indexOfSelect("select 1"));
        Assert.assertEquals(0, SqlUtils.indexOfSelect("select abc from asd"));
        Assert.assertEquals(0, SqlUtils.indexOfSelect("select (select 1 from asd) from asd"));
        Assert.assertEquals(0, SqlUtils.indexOfSelect("select 1 union all select 1"));
        Assert.assertEquals(21, SqlUtils.indexOfSelect("with a AS (select 1) select 1"));
        Assert.assertEquals(38, SqlUtils.indexOfSelect("with a AS (select 1), b AS (select 1) select 1"));
        Assert.assertEquals(65, SqlUtils.indexOfSelect("with a AS (select 1), b AS (select (select 1 from asd) from asd) select 1"));

        Assert.assertEquals(21, SqlUtils.indexOfSelect("insert into abc(a,b) select 1,2"));
    }

    @Test
    public void indexOfSelectQuoted() {
        Assert.assertEquals(30, SqlUtils.indexOfSelect("insert into \"select tbl\"(a,b) select 1,2"));
        Assert.assertEquals(30, SqlUtils.indexOfSelect("insert into `select tbl`(a,b) select 1,2"));
        Assert.assertEquals(30, SqlUtils.indexOfSelect("insert into [select tbl](a,b) select 1,2"));

        Assert.assertEquals(32, SqlUtils.indexOfSelect("with \"select tbl\" AS (select 1) select 1"));
        Assert.assertEquals(32, SqlUtils.indexOfSelect("with `select tbl` AS (select 1) select 1"));
        Assert.assertEquals(32, SqlUtils.indexOfSelect("with [select tbl] AS (select 1) select 1"));

        Assert.assertEquals(42, SqlUtils.indexOfSelect("with \"select\"\"s select tbl\" AS (select 1) select 1"));
        Assert.assertEquals(42, SqlUtils.indexOfSelect("with `select``s select tbl` AS (select 1) select 1"));

        Assert.assertEquals(30, SqlUtils.indexOfSelect("with a AS (select `)select `) select 1"));
    }

    @Test
    public void selectItemAliases() {
        assertAliases("select 1 as one", "one");
        assertAliases("select 1 one, 2 two", "one", "two");
        assertAliases("select 1 as one, 2 as two", "one", "two");
        assertAliases("select 1 as one, 2 as two from abc", "one", "two");
        assertAliases("select 1 as one, 2 as two from abc where a = (select 1 as one)", "one", "two");
        assertAliases("select one from abc", "one");
        assertAliases("select one, two from abc", "one", "two");
        assertAliases("select abc.one, abc.two from abc", "one", "two");
        assertAliases("select abc.aaa one, abc.bbb two from abc", "one", "two");
        assertAliases("with a AS (select 1) select abc.aaa one, abc.bbb as two from abc", "one", "two");
        assertAliases("with a AS (select 1), b AS (select 1) select abc.one, two from abc", "one", "two");
        assertAliases("with a AS (select 1), b AS (select (select 1 from asd) from asd) select 1 one, two from abc union all select three from asd", "one", "two");

        assertAliases("select (select 1) as one, (select 2) as two", "one", "two");
    }

    @Test
    public void selectItemAliasesQuoted() {
        // Double quoted identifiers
        testQuotedIdentifiers("\"", "\"", "\"\"");

        // MySQL quoted identifiers
        testQuotedIdentifiers("`", "`", "``");

        // MSSQL quoted identifiers
        testQuotedIdentifiers("[", "]", null);

        assertAliases("select valuesenti0_.\"value\" as col_0_0_ from ( select * from ValuesEntity ) valuesenti0_", "col_0_0_");

        assertItems("select valuesenti0_.value as col_0_0_ from ( select * from ValuesEntity ) valuesenti0_", "value");
    }

    @Test
    public void selectItemAliasesQuoted1() {
        assertItems("select valuesenti0_.\"value\" as col_0_0_ from ( select * from ValuesEntity ) valuesenti0_", "\"value\"");
    }

    private void testQuotedIdentifiers(String start, String end, String escapeQuote) {
        testQuotedIdentifiersEscaped(start, end, "");
        if (escapeQuote != null) {
            testQuotedIdentifiersEscaped(start, end, "select " + escapeQuote);
            // Assert that brackets in quoted identifiers don't mess up anything
            testQuotedIdentifiersEscaped(start, end, "]");

            // Assert that other quotes don't mess up anything
            if (!escapeQuote.contains("\"")) {
                testQuotedIdentifiersEscaped(start, end, "\"");
            }
            if (!escapeQuote.contains("`")) {
                testQuotedIdentifiersEscaped(start, end, "`");
            }
        }
        // Assert that brackets in quoted identifiers don't mess up anything
        testQuotedIdentifiersEscaped(start, end, ")");
    }

    private void testQuotedIdentifiersEscaped(String start, String end, String escapeQuote) {
        assertAliases(
                "select " + start + escapeQuote + "select tbl" + end + " as one",
                "one");
        assertAliases(
                "select " + start + escapeQuote + "select tbl" + end + " one, " + start + escapeQuote + "select tbl" + end + " two",
                "one", "two");
        assertAliases(
                "select " + start + escapeQuote + "select tbl" + end + " as one, " + start + escapeQuote + "select tbl" + end + " as two",
                "one", "two");
        assertAliases(
                "select " + start + escapeQuote + "select tbl" + end + " as one, " + start + escapeQuote + "select tbl" + end + " as two from abc",
                "one", "two");

        assertAliases(
                "select " + start + escapeQuote + "select tbl" + end + " from abc",
                start + escapeQuote + "select tbl" + end);
        assertAliases(
                "select " + start + escapeQuote + "select tbl1" + end + ", " + start + escapeQuote + "select tbl2" + end + " from abc",
                start + escapeQuote + "select tbl1" + end, start + escapeQuote + "select tbl2" + end);
        assertAliases(
                "select " + start + escapeQuote + "select tbl" + end + "." + start + escapeQuote + "select tbl1" + end + ", " + start + escapeQuote + "select tbl" + end + "." + start + escapeQuote + "select tbl2" + end + " from abc",
                start + escapeQuote + "select tbl1" + end, start + escapeQuote + "select tbl2" + end);
        assertAliases(
                "select " + start + escapeQuote + "select tbl" + end + "." + start + escapeQuote + "select tblX" + end + " " + start + escapeQuote + "select tbl1" + end + ", " + start + escapeQuote + "select tbl" + end + "." + start + escapeQuote + "select tblY" + end + " " + start + escapeQuote + "select tbl2" + end + " from abc",
                start + escapeQuote + "select tbl1" + end, start + escapeQuote + "select tbl2" + end);

        assertAliases(
                "with a AS (select " + start + escapeQuote + "select tbl" + end + ") select " + start + escapeQuote + "select tbl" + end + "." + start + escapeQuote + "select tblX" + end + " " + start + escapeQuote + "select tbl1" + end + ", " + start + escapeQuote + "select tbl" + end + "." + start + escapeQuote + "select tblY" + end + " " + start + escapeQuote + "select tbl2" + end + " from abc",
                start + escapeQuote + "select tbl1" + end, start + escapeQuote + "select tbl2" + end);
        assertAliases(
                "with a AS (select " + start + escapeQuote + "select tbl" + end + "), b AS (select " + start + escapeQuote + "select tbl" + end + ") select " + start + escapeQuote + "select tbl" + end + "." + start + escapeQuote + "select tblX" + end + " " + start + escapeQuote + "select tbl1" + end + ", " + start + escapeQuote + "select tbl" + end + "." + start + escapeQuote + "select tblY" + end + " " + start + escapeQuote + "select tbl2" + end + " from abc",
                start + escapeQuote + "select tbl1" + end, start + escapeQuote + "select tbl2" + end);
        assertAliases(
                "with a AS (select " + start + escapeQuote + "select tbl" + end + "), b AS (select (select " + start + escapeQuote + "select tbl" + end + " from asd) from asd) select " + start + escapeQuote + "select tbl" + end + "." + start + escapeQuote + "select tblX" + end + " " + start + escapeQuote + "select tbl1" + end + ", " + start + escapeQuote + "select tbl" + end + "." + start + escapeQuote + "select tblY" + end + " " + start + escapeQuote + "select tbl2" + end + " from abc",
                start + escapeQuote + "select tbl1" + end, start + escapeQuote + "select tbl2" + end);
    }

    static void assertAliases(String sql, String... expectedAliases) {
        Assert.assertArrayEquals(expectedAliases, SqlUtils.getSelectItemAliases(sql, SqlUtils.indexOfSelect(sql)));
    }

    static void assertItems(String sql, String... expectedItems) {
        Assert.assertArrayEquals(expectedItems, SqlUtils.getSelectItemColumns(sql, SqlUtils.indexOfSelect(sql)));
    }

}
