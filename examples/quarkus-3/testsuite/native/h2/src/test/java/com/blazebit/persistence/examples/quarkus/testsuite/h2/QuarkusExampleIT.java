/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.examples.quarkus.testsuite.h2;

import com.blazebit.persistence.examples.quarkus.testsuite.base.AbstractQuarkusExampleTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@QuarkusTestResource(H2DatabaseTestResource.class)
@QuarkusIntegrationTest
public class QuarkusExampleIT extends AbstractQuarkusExampleTest {
}
