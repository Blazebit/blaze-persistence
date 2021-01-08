/*
 * Copyright 2014 - 2021 Blazebit.
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

import static org.junit.Assert.assertArrayEquals;

/**
 *
 * @author Christian Beikov
 */
public class JsonParserTest {

    @Test
    public void testStringOnly() {
        assertEquals(new Object[]{ "test", "1" }, JsonParser.parseStringOnly("[{\"name\": \"test\", \"age\": \"1\"}]", "name", "age").get(0));
        assertEquals(new Object[]{ "test", "1" }, JsonParser.parseStringOnly("[{\"age\": \"1\", \"name\": \"test\"}]", "name", "age").get(0));
        assertEquals(new Object[]{ "1", "test" }, JsonParser.parseStringOnly("[{\"name\": \"test\", \"age\": \"1\"}]", "age", "name").get(0));
        assertEquals(new Object[]{ "1", "test" }, JsonParser.parseStringOnly("[{\"age\": \"1\", \"name\": \"test\"}]", "age", "name").get(0));
    }

    @Test
    public void testStringOnlyEscaping() {
        assertEquals(new Object[]{ "test\"\\/\b\f\n\r\t\uFFFF", "1" }, JsonParser.parseStringOnly("[{\"name\": \"test\\\"\\\\\\/\\b\\f\\n\\r\\t\\uFFFF\", \"age\": \"1\"}]", "name", "age").get(0));
        assertEquals(new Object[]{ "test\"\\/\b\f\n\r\t\uFFFF", "1" }, JsonParser.parseStringOnly("[{\"age\": \"1\", \"name\": \"test\\\"\\\\\\/\\b\\f\\n\\r\\t\\uFFFF\"}]", "name", "age").get(0));
        assertEquals(new Object[]{ "1", "test\"\\/\b\f\n\r\t\uFFFF" }, JsonParser.parseStringOnly("[{\"name\": \"test\\\"\\\\\\/\\b\\f\\n\\r\\t\\uFFFF\", \"age\": \"1\"}]", "age", "name").get(0));
        assertEquals(new Object[]{ "1", "test\"\\/\b\f\n\r\t\uFFFF" }, JsonParser.parseStringOnly("[{\"age\": \"1\", \"name\": \"test\\\"\\\\\\/\\b\\f\\n\\r\\t\\uFFFF\"}]", "age", "name").get(0));
    }

    @Test
    public void testStringOnlyMultiple() {
        assertEquals(new Object[]{ "test", "1" }, JsonParser.parseStringOnly("[{\"name\": \"test\", \"age\": \"1\"}, {\"name\": \"test\", \"age\": \"1\"}]", "name", "age").get(0));
        assertEquals(new Object[]{ "test", "1" }, JsonParser.parseStringOnly("[{\"name\": \"test\", \"age\": \"1\"}, {\"name\": \"test\", \"age\": \"1\"}]", "name", "age").get(1));
        assertEquals(new Object[]{ "test", "1" }, JsonParser.parseStringOnly("[{\"age\": \"1\", \"name\": \"test\"}, {\"age\": \"1\", \"name\": \"test\"}]", "name", "age").get(0));
        assertEquals(new Object[]{ "test", "1" }, JsonParser.parseStringOnly("[{\"age\": \"1\", \"name\": \"test\"}, {\"age\": \"1\", \"name\": \"test\"}]", "name", "age").get(1));
        assertEquals(new Object[]{ "1", "test" }, JsonParser.parseStringOnly("[{\"name\": \"test\", \"age\": \"1\"}, {\"name\": \"test\", \"age\": \"1\"}]", "age", "name").get(0));
        assertEquals(new Object[]{ "1", "test" }, JsonParser.parseStringOnly("[{\"name\": \"test\", \"age\": \"1\"}, {\"name\": \"test\", \"age\": \"1\"}]", "age", "name").get(1));
        assertEquals(new Object[]{ "1", "test" }, JsonParser.parseStringOnly("[{\"age\": \"1\", \"name\": \"test\"}, {\"age\": \"1\", \"name\": \"test\"}]", "age", "name").get(0));
        assertEquals(new Object[]{ "1", "test" }, JsonParser.parseStringOnly("[{\"age\": \"1\", \"name\": \"test\"}, {\"age\": \"1\", \"name\": \"test\"}]", "age", "name").get(1));
    }

    private static void assertEquals(Object[] array1, Object[] array2) {
        Assert.assertEquals(array1.length, array2.length);
        for (int i = 0; i < array1.length; i++) {
            Assert.assertEquals(String.valueOf(array1[i]), String.valueOf(array2[i]));
        }
    }

}
