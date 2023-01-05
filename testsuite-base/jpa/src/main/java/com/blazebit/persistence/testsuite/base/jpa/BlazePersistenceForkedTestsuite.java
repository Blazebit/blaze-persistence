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

package com.blazebit.persistence.testsuite.base.jpa;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;
import org.junit.experimental.categories.Categories;
import org.junit.runner.manipulation.NoTestsRemainException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * We use dynamic test suites to group test executions by the used entity classes in order to minimize schema dropping
 * and entity manager factory recreation in between tests.
 *
 * @author Moritz Becker
 * @since 1.5.0
 */
public abstract class BlazePersistenceForkedTestsuite extends BlazePersistenceTestsuite {

    private static final Logger LOG = Logger.getLogger(BlazePersistenceForkedTestsuite.class.getName());

    private static final float GROUP_SPLIT_THRESHOLD = 0.6f;

    protected static TestSuite suite0(int testsuiteNumber) {
        int forkCount = Integer.parseInt(System.getProperty("forkCount", "1"));
        int fork = Integer.parseInt(System.getProperty("fork", "1"));

        TestSuite suite = new TestSuite();

        if (testsuiteNumber <= forkCount) {
            Class<?>[] excludedGroups = loadExcludedGroups();
            TestClasses testClasses = loadAndGroupTestClasses();

            List<List<JUnit4TestAdapter>> jpaPersistenceTestInstanceGroups = testClasses.groupedJpaPersistenceTests.values().stream()
                    .map(jpaPersistenceTestGroup ->
                        jpaPersistenceTestGroup.stream()
                                .map(testClass -> {
                                    JUnit4TestAdapter jUnit4TestAdapter = new JUnit4TestAdapter(testClass);
                                    try {
                                        jUnit4TestAdapter.filter(Categories.CategoryFilter.exclude(excludedGroups));
                                    } catch (NoTestsRemainException e) {
                                        jUnit4TestAdapter = null;
                                    }
                                    return jUnit4TestAdapter;
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())
                    ).filter(collection -> !collection.isEmpty())
                    .collect(Collectors.toList());

            double numTestCases = jpaPersistenceTestInstanceGroups.stream()
                    .mapToInt(group -> group.stream().mapToInt(JUnit4TestAdapter::countTestCases).sum()).sum();

            jpaPersistenceTestInstanceGroups = jpaPersistenceTestInstanceGroups.stream()
                    .flatMap(group -> {
                        int numTestCasesInGroup = group.stream().mapToInt(JUnit4TestAdapter::countTestCases).sum();
                        if (numTestCasesInGroup / numTestCases > GROUP_SPLIT_THRESHOLD) {
                            int splitFactor = Math.min(forkCount, (int) Math.ceil(numTestCasesInGroup / (numTestCases * GROUP_SPLIT_THRESHOLD)));
                            if (splitFactor > 1) {
                                return splitGroup(group, splitFactor).stream();
                            }
                        }
                        return Stream.of(group);
                    })
                    .sorted(Comparator.comparingInt(List<JUnit4TestAdapter>::size).reversed())
                    .collect(Collectors.toList());

            for (int testIdx = 0; testIdx < jpaPersistenceTestInstanceGroups.size(); testIdx++) {
                if (testIdx % forkCount == fork - 1) {
                    jpaPersistenceTestInstanceGroups.get(testIdx).forEach(suite::addTest);
                }
            }

            List<JUnit4TestAdapter> nonJpaPersistenceTestInstances = testClasses.nonJpaPersistenceTests.stream()
                    .sorted(Comparator.comparing(Class::getCanonicalName))
                    .map(testClass -> {
                        JUnit4TestAdapter jUnit4TestAdapter = new JUnit4TestAdapter(testClass);
                        try {
                            jUnit4TestAdapter.filter(Categories.CategoryFilter.exclude(excludedGroups));
                            return jUnit4TestAdapter;
                        } catch (NoTestsRemainException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            for (int testIdx = 0; testIdx < nonJpaPersistenceTestInstances.size(); testIdx++) {
                if (testIdx % forkCount == fork - 1) {
                    suite.addTest(nonJpaPersistenceTestInstances.get(testIdx));
                }
            }

            LOG.info("Fork " + fork + "/" + forkCount + " is running " + suite.countTestCases() + " test cases");
        }

        return suite;
    }

    private static List<List<JUnit4TestAdapter>> splitGroup(List<JUnit4TestAdapter> group, int splitFactor) {
        List<List<JUnit4TestAdapter>> splitGroup = new ArrayList<>(splitFactor);
        for (int i = 0; i < splitFactor; i++) {
            splitGroup.add(new ArrayList<>());
        }
        group = group.stream().sorted(Comparator.comparing(JUnit4TestAdapter::countTestCases).reversed()).collect(Collectors.toList());
        for (int testClassIdx = 0; testClassIdx < group.size(); testClassIdx++) {
            splitGroup.get(testClassIdx % splitFactor).add(group.get(testClassIdx));
        }
        return splitGroup;
    }
}
