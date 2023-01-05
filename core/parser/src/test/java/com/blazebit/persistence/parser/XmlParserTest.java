/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.parser;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 */
public class XmlParserTest {

    @Test
    public void testNormal() {
        assertEquals(new Object[]{ "test", "1" }, XmlParser.parse("<e><name>test</name><age>1</age></e>", "name", "age").get(0));
        assertEquals(new Object[]{ "test", "1" }, XmlParser.parse("<e><age>1</age><name>test</name></e>", "name", "age").get(0));
        assertEquals(new Object[]{ "1", "test" }, XmlParser.parse("<e><name>test</name><age>1</age></e>", "age", "name").get(0));
        assertEquals(new Object[]{ "1", "test" }, XmlParser.parse("<e><age>1</age><name>test</name></e>", "age", "name").get(0));
    }

    @Test
    public void testNormalEscaping() {
        assertEquals(new Object[]{ "test&<>", "1" }, XmlParser.parse("<e><name>test&amp;&lt;&gt;</name><age>1</age></e>", "name", "age").get(0));
        assertEquals(new Object[]{ "test&<>", "1" }, XmlParser.parse("<e><age>1</age><name>test&amp;&lt;&gt;</name></e>", "name", "age").get(0));
        assertEquals(new Object[]{ "1", "test&<>" }, XmlParser.parse("<e><name>test&amp;&lt;&gt;</name><age>1</age></e>", "age", "name").get(0));
        assertEquals(new Object[]{ "1", "test&<>" }, XmlParser.parse("<e><age>1</age><name>test&amp;&lt;&gt;</name></e>", "age", "name").get(0));
    }

    @Test
    public void testNormalMultiple() {
        assertEquals(new Object[]{ "test", "1" }, XmlParser.parse("<e><name>test</name><age>1</age></e><e><name>test</name><age>1</age></e>", "name", "age").get(0));
        assertEquals(new Object[]{ "test", "1" }, XmlParser.parse("<e><name>test</name><age>1</age></e><e><name>test</name><age>1</age></e>", "name", "age").get(1));
        assertEquals(new Object[]{ "test", "1" }, XmlParser.parse("<e><age>1</age><name>test</name></e><e><age>1</age><name>test</name></e>", "name", "age").get(0));
        assertEquals(new Object[]{ "test", "1" }, XmlParser.parse("<e><age>1</age><name>test</name></e><e><age>1</age><name>test</name></e>", "name", "age").get(1));
        assertEquals(new Object[]{ "1", "test" }, XmlParser.parse("<e><name>test</name><age>1</age></e><e><name>test</name><age>1</age></e>", "age", "name").get(0));
        assertEquals(new Object[]{ "1", "test" }, XmlParser.parse("<e><name>test</name><age>1</age></e><e><name>test</name><age>1</age></e>", "age", "name").get(1));
        assertEquals(new Object[]{ "1", "test" }, XmlParser.parse("<e><age>1</age><name>test</name></e><e><age>1</age><name>test</name></e>", "age", "name").get(0));
        assertEquals(new Object[]{ "1", "test" }, XmlParser.parse("<e><age>1</age><name>test</name></e><e><age>1</age><name>test</name></e>", "age", "name").get(1));
    }

    private static void assertEquals(Object[] array1, Object[] array2) {
        Assert.assertEquals(array1.length, array2.length);
        for (int i = 0; i < array1.length; i++) {
            Assert.assertEquals(String.valueOf(array1[i]), String.valueOf(array2[i]));
        }
    }

}
