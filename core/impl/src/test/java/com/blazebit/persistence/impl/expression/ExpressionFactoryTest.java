/*
 * Copyright 2014 Blazebit.
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
package com.blazebit.persistence.impl.expression;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public class ExpressionFactoryTest {

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();
    private ExpressionFactory ef;

    @Before
    public void createFactory() {
        ef = new ExpressionFactoryImpl();
    }

    @BenchmarkOptions(benchmarkRounds = 100, warmupRounds = 0)
    @Test
    public void testCreateSimpleExpressionPerformance() {
        ef.createSimpleExpression(
            "SIZE(Hello.world[:hahaha].criteria[1].api.lsls[a.b.c.d.e]) + SIZE(Hello.world[:hahaha].criteria[1].api.lsls[a.b.c.d.e])");
    }

    @BenchmarkOptions(benchmarkRounds = 100, warmupRounds = 0)
    @Test
    public void testExpressionClone() {
        //TODO: implement
    }
}
