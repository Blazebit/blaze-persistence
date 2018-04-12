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

package com.blazebit.persistence.parser;

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.expression.ExpressionFactoryImpl;
import com.blazebit.persistence.parser.expression.MacroConfiguration;
import com.blazebit.persistence.parser.expression.MacroFunction;
import com.blazebit.persistence.parser.expression.SimpleCachingExpressionFactory;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.HashSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class SimpleCachingExpressionFactoryPerformanceTest {

    private static Level originalLevel;

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();
    private final ExpressionFactory cachingExpressionFactory = new SimpleCachingExpressionFactory(new ExpressionFactoryImpl(new HashSet<String>(), true, true));
    private final ExpressionFactory nonCachingExpressionFactory = new ExpressionFactoryImpl(new HashSet<String>(), true, true);

    private final MacroConfiguration tenMacros;
    private final MacroConfiguration hundredMacros;

    public SimpleCachingExpressionFactoryPerformanceTest() {
        tenMacros = createMacroConfiguration(10);
        hundredMacros = createMacroConfiguration(100);
    }

    private static MacroConfiguration createMacroConfiguration(int numMacros){
        if (numMacros > 0) {
            NavigableMap<String, MacroFunction> macros = new TreeMap<String, MacroFunction>();
            for (int i = 0; i < numMacros; i++) {
                final String macroName = "MY_MACRO_" + i;
                macros.put(macroName, new MyMacro(macroName));
            }
            return MacroConfiguration.of(macros);
        } else {
            return null;
        }
    }

    private static class MyMacro implements MacroFunction {

        private final String name;

        public MyMacro(String name) {
            this.name = name;
        }

        @Override
        public Expression apply(List<Expression> expressions) {
            return null;
        }

        @Override
        public Object[] getState() {
            return new Object[]{ name };
        }

        @Override
        public boolean supportsCaching() {
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof MyMacro)) {
                return false;
            }

            MyMacro myMacro = (MyMacro) o;
            return name.equals(myMacro.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    @BeforeClass
    public static void beforeClass() {
        Logger log = Logger.getLogger("com.blazebit.persistence.parser");
        originalLevel = log.getLevel();
        log.setLevel(Level.OFF);
    }
    
    @AfterClass
    public static void afterClass() {
        Logger log = Logger.getLogger("com.blazebit.persistence.parser");
        log.setLevel(originalLevel);
    }

    /* Non-caching */

    @BenchmarkOptions(benchmarkRounds = 10000, warmupRounds = 5000, concurrency = 4)
    @Test
    public void testCreateSimpleExpressionPerformanceNonCaching() {
        testCreateSimpleExpressionPerformance(nonCachingExpressionFactory, null);
    }

    @BenchmarkOptions(benchmarkRounds = 10000, warmupRounds = 5000, concurrency = 4)
    @Test
    public void testCreateSimpleExpressionPerformanceWith10MacrosNonCaching() {
        testCreateSimpleExpressionPerformance(nonCachingExpressionFactory, tenMacros);
    }

    @BenchmarkOptions(benchmarkRounds = 10000, warmupRounds = 5000, concurrency = 4)
    @Test
    public void testCreateSimpleExpressionPerformanceWith100MacrosNonCaching() {
        testCreateSimpleExpressionPerformance(nonCachingExpressionFactory, hundredMacros);
    }

    /* Caching */

    @BenchmarkOptions(benchmarkRounds = 10000, warmupRounds = 5000, concurrency = 4)
    @Test
    public void testCreateSimpleExpressionPerformanceCaching() {
        testCreateSimpleExpressionPerformance(cachingExpressionFactory, null);
    }

    @BenchmarkOptions(benchmarkRounds = 10000, warmupRounds = 5000, concurrency = 4)
    @Test
    public void testCreateSimpleExpressionPerformanceWith10MacrosCaching() {
        testCreateSimpleExpressionPerformance(cachingExpressionFactory, tenMacros);
    }

    private void testCreateSimpleExpressionPerformance(ExpressionFactory ef, MacroConfiguration macroConfiguration) {
        String expressionString = "SIZE(Hello.world[:hahaha].criteria[1].api.lsls[a.b.c.d.e]) + SIZE(Hello.world[:hahaha].criteria[1].api.lsls[a.b.c.d.e])";

        Expression expr1 = ef.createSimpleExpression(expressionString, true, macroConfiguration, null);
        Expression expr2 = ef.createSimpleExpression(expressionString, true, macroConfiguration, null);

        Assert.assertFalse(expr1 == expr2);
        Assert.assertEquals(expr1, expr2);
    }
}
