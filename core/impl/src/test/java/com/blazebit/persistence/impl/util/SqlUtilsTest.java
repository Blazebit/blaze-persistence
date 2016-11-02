/*
 * Copyright 2014 - 2016 Blazebit.
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
        Assert.assertEquals(33, SqlUtils.indexOfSelect("with a AS (select 1), (select 1) select 1"));
        Assert.assertEquals(60, SqlUtils.indexOfSelect("with a AS (select 1), (select (select 1 from asd) from asd) select 1"));
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
        assertAliases("with a AS (select 1), (select 1) select abc.one, two from abc", "one", "two");
        assertAliases("with a AS (select 1), (select (select 1 from asd) from asd) select 1 one, two from abc union all select three from asd", "one", "two");
    }

    static void assertAliases(String sql, String... expectedAliases) {
        Assert.assertArrayEquals(expectedAliases, SqlUtils.getSelectItemAliases(sql, SqlUtils.indexOfSelect(sql)));
    }

}
