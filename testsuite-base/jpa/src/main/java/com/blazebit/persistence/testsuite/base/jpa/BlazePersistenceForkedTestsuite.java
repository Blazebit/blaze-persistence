/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
                    .sorted(Comparator.comparingInt(List<JUnit4TestAdapter>::size).reversed())
                    .collect(Collectors.toList());

            int jpaTestCases = jpaPersistenceTestInstanceGroups.stream()
                    .mapToInt(group -> group.stream().mapToInt(JUnit4TestAdapter::countTestCases).sum()).sum();
            int jpaTestCasesPerFork = jpaTestCases / forkCount;
            int processedTestCases = 0;

            for (List<JUnit4TestAdapter> group : jpaPersistenceTestInstanceGroups) {
                boolean partOfFork = (processedTestCases / jpaTestCasesPerFork) + 1 == fork;
                for (JUnit4TestAdapter jUnit4TestAdapter : group) {
                    processedTestCases += jUnit4TestAdapter.countTestCases();
                    if (partOfFork) {
                        suite.addTest(jUnit4TestAdapter);
                    }
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
            int nonJpaTestCasesPerFork = nonJpaPersistenceTestInstances.stream().mapToInt(JUnit4TestAdapter::countTestCases).sum() / forkCount;

            processedTestCases = 0;

            for (JUnit4TestAdapter jUnit4TestAdapter : nonJpaPersistenceTestInstances) {
                if ((processedTestCases / nonJpaTestCasesPerFork) + 1 == fork) {
                    suite.addTest(jUnit4TestAdapter);
                }
                processedTestCases += jUnit4TestAdapter.countTestCases();
            }

//            jpaPersistenceTestInstanceGroups = jpaPersistenceTestInstanceGroups.stream()
//                    .flatMap(group -> {
//                        int numTestCasesInGroup = group.stream().mapToInt(JUnit4TestAdapter::countTestCases).sum();
//                        if (numTestCasesInGroup / jpaTestCases > GROUP_SPLIT_THRESHOLD) {
//                            int splitFactor = Math.min(forkCount, (int) Math.ceil(numTestCasesInGroup / (jpaTestCases * GROUP_SPLIT_THRESHOLD)));
//                            if (splitFactor > 1) {
//                                return splitGroup(group, splitFactor).stream();
//                            }
//                        }
//                        return Stream.of(group);
//                    })
//                    .sorted(Comparator.comparingInt(List<JUnit4TestAdapter>::size).reversed())
//                    .collect(Collectors.toList());
//
//            for (int testIdx = 0; testIdx < jpaPersistenceTestInstanceGroups.size(); testIdx++) {
//                if (testIdx % forkCount == fork - 1) {
//                    jpaPersistenceTestInstanceGroups.get(testIdx).forEach(suite::addTest);
//                }
//            }
//
//            List<JUnit4TestAdapter> nonJpaPersistenceTestInstances = testClasses.nonJpaPersistenceTests.stream()
//                    .sorted(Comparator.comparing(Class::getCanonicalName))
//                    .map(testClass -> {
//                        JUnit4TestAdapter jUnit4TestAdapter = new JUnit4TestAdapter(testClass);
//                        try {
//                            jUnit4TestAdapter.filter(Categories.CategoryFilter.exclude(excludedGroups));
//                            return jUnit4TestAdapter;
//                        } catch (NoTestsRemainException e) {
//                            return null;
//                        }
//                    })
//                    .filter(Objects::nonNull)
//                    .collect(Collectors.toList());
//
//            for (int testIdx = 0; testIdx < nonJpaPersistenceTestInstances.size(); testIdx++) {
//                if (testIdx % forkCount == fork - 1) {
//                    suite.addTest(nonJpaPersistenceTestInstances.get(testIdx));
//                }
//            }

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
