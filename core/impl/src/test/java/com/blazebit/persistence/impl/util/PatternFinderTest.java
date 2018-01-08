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

public class PatternFinderTest {

    private PatternFinder firstFinder;
    private PatternFinder lastFinder;

    @Test
    public void matching() {
        firstFinder = new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder("abc");
        lastFinder = new BoyerMooreCaseInsensitiveAsciiLastPatternFinder("abc");
        Assert.assertEquals(0, firstFinder.indexIn("abc"));
        Assert.assertEquals(0, lastFinder.indexIn("abc"));
    }

    @Test
    public void notMatching() {
        firstFinder = new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder("xxx");
        lastFinder = new BoyerMooreCaseInsensitiveAsciiLastPatternFinder("xxx");
        Assert.assertEquals(-1, firstFinder.indexIn("abc"));
        Assert.assertEquals(-1, lastFinder.indexIn("abc"));
    }

    @Test
    public void partPrefixMatching() {
        firstFinder = new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder("abx");
        lastFinder = new BoyerMooreCaseInsensitiveAsciiLastPatternFinder("abx");
        Assert.assertEquals(-1, firstFinder.indexIn("abc"));
        Assert.assertEquals(-1, lastFinder.indexIn("abc"));
    }

    @Test
    public void partSuffixMatching() {
        firstFinder = new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder("xbc");
        lastFinder = new BoyerMooreCaseInsensitiveAsciiLastPatternFinder("xbc");
        Assert.assertEquals(-1, firstFinder.indexIn("abc"));
        Assert.assertEquals(-1, lastFinder.indexIn("abc"));
    }

    @Test
    public void matchingPrefix() {
        firstFinder = new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder("ab");
        lastFinder = new BoyerMooreCaseInsensitiveAsciiLastPatternFinder("ab");
        Assert.assertEquals(0, firstFinder.indexIn("abc"));
        Assert.assertEquals(0, lastFinder.indexIn("abc"));
    }

    @Test
    public void matchingSuffix() {
        firstFinder = new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder("bc");
        lastFinder = new BoyerMooreCaseInsensitiveAsciiLastPatternFinder("bc");
        Assert.assertEquals(1, firstFinder.indexIn("abc"));
        Assert.assertEquals(1, lastFinder.indexIn("abc"));
    }

    @Test
    public void matchingMultipleFirst() {
        firstFinder = new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder("ab");
        lastFinder = new BoyerMooreCaseInsensitiveAsciiLastPatternFinder("ab");
        Assert.assertEquals(2, firstFinder.indexIn("xxabxxabxx"));
        Assert.assertEquals(6, lastFinder.indexIn("xxabxxabxx"));
    }

    @Test
    public void matchingMultipleLast() {
        firstFinder = new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder("ab");
        lastFinder = new BoyerMooreCaseInsensitiveAsciiLastPatternFinder("ab");
        Assert.assertEquals(6, firstFinder.indexIn("xxabxxabxx", 3));
        Assert.assertEquals(2, lastFinder.indexIn("xxabxxabxx", 0, 6));
    }

    @Test
    public void matchingWithSkipFirst() {
        firstFinder = new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder("abab");
        lastFinder = new BoyerMooreCaseInsensitiveAsciiLastPatternFinder("abab");
        Assert.assertEquals(8, firstFinder.indexIn("xxabacxxababxxabacxxababxxabacxx"));
        Assert.assertEquals(20, lastFinder.indexIn("xxabacxxababxxabacxxababxxabacxx"));
    }

    @Test
    public void matchingWithSkipLast() {
        firstFinder = new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder("abab");
        lastFinder = new BoyerMooreCaseInsensitiveAsciiLastPatternFinder("abab");
        Assert.assertEquals(20, firstFinder.indexIn("xxabacxxababxxabacxxababxxabacxx", 9));
        Assert.assertEquals(8, lastFinder.indexIn("xxabacxxababxxabacxxababxxabacxx", 0, 20));
    }

    @Test
    public void bla() {
        firstFinder = new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder("bbab");
        lastFinder = new BoyerMooreCaseInsensitiveAsciiLastPatternFinder("bbab");
        Assert.assertEquals(0, lastFinder.indexIn("bbabaaab"));
    }

    @Test
    public void bla2() {
        firstFinder = new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder("aaac");
        lastFinder = new BoyerMooreCaseInsensitiveAsciiLastPatternFinder("aaac");
        Assert.assertEquals(0, lastFinder.indexIn("aaacbaaaax"));
    }

    @Test
    public void bla3() {
        firstFinder = new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder("cXXXcXXXbXXXcXXX");
        lastFinder = new BoyerMooreCaseInsensitiveAsciiLastPatternFinder("cXXXcXXXbXXXcXXX");
        Assert.assertEquals(13, lastFinder.indexIn("leading TEXT cXXXcXXXbXXXcXXX rest of the TEXT"));
    }

    @Test
    public void bla4() {
        firstFinder = new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder("AABA");
        lastFinder = new BoyerMooreCaseInsensitiveAsciiLastPatternFinder("AABA");
        Assert.assertEquals(0, firstFinder.indexIn("AABAACAADAABAAABAA"));
        Assert.assertEquals(9, firstFinder.indexIn("AABAACAADAABAAABAA", 1));
        Assert.assertEquals(13, firstFinder.indexIn("AABAACAADAABAAABAA", 10));

        Assert.assertEquals(13, lastFinder.indexIn("AABAACAADAABAAABAA"));
        Assert.assertEquals(9, lastFinder.indexIn("AABAACAADAABAAABAA", 0, 13));
        Assert.assertEquals(0, lastFinder.indexIn("AABAACAADAABAAABAA", 0, 9));
    }

    @Test
    public void expressionExtraction() {
        firstFinder = new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(" as ");
        lastFinder = new BoyerMooreCaseInsensitiveAsciiLastPatternFinder(" as ");
        Assert.assertEquals(15, lastFinder.indexIn(" document0_.age as age2_1_"));
        Assert.assertEquals(15, firstFinder.indexIn(" document0_.age as age2_1_"));
    }
}
