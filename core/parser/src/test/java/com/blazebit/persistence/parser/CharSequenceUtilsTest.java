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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 */
public class CharSequenceUtilsTest {

    @Test
    public void testStringOnly() {
        assertEquals(11, new SubSequence("[{},{\"asd\":[{\"x\":null}]},{}]", 11, 23).lastIndexOf(']', 0, 12));
        assertEquals(1, new SubSequence("[{},{\"asd\":[{\"x\":null}]},{}]", 11, 23).indexOf('{', 1, 12));
        assertEquals(11, CharSequenceUtils.lastIndexOf(new SubSequence("[{\"f0\":\"Doc1\",\"f1\":\"0\",\"f2\":[{\"f0\":\"a\"}]},{\"f0\":\"Doc1\",\"f1\":\"0\",\"f2\":[{\"f0\":\"b\"}]},{\"f0\":\"Doc2\",\"f1\":\"0\",\"f2\":[{\"f0\":\"c\"}]}]", 110, 122), ']'));
        assertEquals(6, CharSequenceUtils.lastIndexOf(new SubSequence(new SubSequence("[{x:[{y:[{z:0}]}]}]", 4, 17), 4, 11), ']'));
    }

}
